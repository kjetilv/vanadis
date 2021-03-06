/*
 * Copyright 2009 Kjetil Valstadsve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vanadis.extrt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vanadis.annopro.AnnotationDatum;
import vanadis.annopro.AnnotationsDigest;
import vanadis.annopro.AnnotationsDigests;
import vanadis.core.lang.AccessibleHelper;
import vanadis.core.properties.PropertySet;
import vanadis.ext.*;
import vanadis.osgi.ServiceProperties;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

class ValidAnnotations {

    static AnnotationsDigest read(Class<?> managedType) {
        AnnotationsDigest digest = AnnotationsDigests.createFullFromType(managedType);
        for (AnnotationDatum<Method> datum : digest.methodData()) {
            validateMethods(digest, datum);
        }
        for (AnnotationDatum<Field> datum : digest.fieldData()) {
            validateField(digest, datum);
        }
        return digest;
    }

    private static final Logger log = LoggerFactory.getLogger(ValidAnnotations.class);

    private static final Class<PropertySet> PROPERTIES_CLASS = PropertySet.class;

    @SuppressWarnings({"RawUseOfParameterizedType"})
    // Doggonit
    private static final Class<ServiceProperties> SERVICE_PROPERTIES_CLASS = ServiceProperties.class;

    private static AccessibleObject validateMethods(AnnotationsDigest digest, AnnotationDatum<Method> datum) {
        for (AnnotationDatum<Method> annotation : digest.methodData(datum.getElement())) {
            if (annotation.isType(Inject.class)) {
                return validInjectionMethod(digest, annotation.getElement());
            }
            if (annotation.isType(Expose.class)) {
                return validExposureMethod(annotation.getElement());
            }
            if (annotation.isType(Retract.class)) {
                return validRetractMethod(annotation.getElement());
            }
            if (annotation.isType(Track.class)) {
                return validTrackingMethod(annotation.getElement());
            }
            if (annotation.isType(Configure.class)) {
                return validConfigParameters(annotation.getElement());
            }
        }
        return null;
    }


    private static AccessibleObject validateField(AnnotationsDigest digest, AnnotationDatum<Field> datum) {
        for (AnnotationDatum<Field> annotation : digest.getFieldData(datum.annotationType())) {
            if (annotation.isType(Expose.class)) {
                return validExposedField(annotation.getElement());
            }
            if (annotation.isType(Configure.class)) {
                return validConfigField(annotation.getElement());
            }
        }
        return null;
    }

    private static AccessibleObject validExposedField(Field field) {
        if (!Modifier.isFinal(field.getModifiers())) {
            throw new ModuleSystemException
                    ("Non-final field is not allowed for exposure: " + field);
        }
        return field;
    }

    private static Method validRetractMethod(Method method) {
        return accessibleFor("Retraction", method);
    }

    private static Method validTrackingMethod(Method method) {
        validateTrackingSignature(method);
        return accessibleFor("Tracking", method);
    }

    private static AccessibleObject validConfigField(Field field) {
        return accessibleFor("Configuration", field);
    }

    private static void validateTrackingSignature(Method method) {
        Class<?>[] params = method.getParameterTypes();
        if (params != null && params.length > 0) {
            throw new ModuleSystemException
                    ("Tracking method cannot take arguments: " + method);
        }
        if (!Collection.class.isAssignableFrom(method.getReturnType())) {
            throw new ModuleSystemException
                    ("Tracking method must return " + Collection.class + " or subclass thereof: " + method);
        }
    }

    private static Method validConfigParameters(Method method) {
        Class<?>[] classes = method.getParameterTypes();
        if (classes == null || classes.length != 1) {
            throw new ModuleSystemException
                    ("Configuration method should have one parameter: " + method);
        }
        return method;
    }

    private static Method validInjectionMethod(AnnotationsDigest digest, Method method) {
        validateInjectionParameters(digest, method, 1, "one argument");
        return accessibleFor("Injection", method);
    }

    private static Method validExposureMethod(Method method) {
        validateExposureParameters(method);
        return accessibleFor("Exposure", method);
    }

    private static <T extends AccessibleObject> T accessibleFor(String type, T object) {
        int modifiers = object instanceof Method ? ((Method)object).getModifiers()
                : ((Field) object).getModifiers();
        if (object instanceof Field && Modifier.isFinal(modifiers)) {
            throw new ModuleSystemException
                    (type + " point is final, refusing service!  " +
                            "Setting final fields reflectively is too unpredictable!");
        }
        if (!Modifier.isPublic(modifiers)) {
            try {
                AccessibleHelper.openSesame(object);
            } catch (SecurityException e) {
                throw new ModuleSystemException
                        (type + " point is not accessible under current security manager", e);
            }
            if (log.isDebugEnabled()) {
                log.debug(type + " point set to accessible: " + object);
            }
        }
        return object;
    }

    private static void validateInjectionParameters(AnnotationsDigest digest, Method method,
                                                    int arguments,
                                                    String expected) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        int length = parameterTypes.length;
        if (length == 1) {
            return;
        }
        if (length == 2) {
            if (notServiceProperties(parameterTypes[arguments])) {
                throw new ModuleSystemException(injectUsage(method, expected));
            } else {
                return;
            }
        }
        if (length == 0) {
            String getter = method.getName();
            Map<String, AnnotationDatum<Method>> index = digest.getMethodDataIndex(Inject.class);
            AnnotationDatum<Method> scalaSetter = index.get(getter + SCALASET_SUFFIX);
            if (scalaSetter == null) {
                throw new ModuleSystemException(injectUsage(method, expected));
            }
            return;
        }
        throw new ModuleSystemException(injectUsage(method, expected));
    }

    private static String exposeUsage(Method method) {
        return method + " should have zero arguments or a single argment of " +
                PROPERTIES_CLASS + "!";
    }

    private static String injectUsage(Method method, String expected) {
        return method + " should have " + expected +
                ", with an optional argument of " + PROPERTIES_CLASS + "!";
    }

    private static boolean notProperties(Class<?> parameterType) {
        return !PROPERTIES_CLASS.isAssignableFrom(parameterType);
    }

    private static boolean notServiceProperties(Class<?> parameterType) {
        return !SERVICE_PROPERTIES_CLASS.isAssignableFrom(parameterType);
    }

    private static void validateExposureParameters(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length > 1) {
            throw new ModuleSystemException(exposeUsage(method));
        } else if (parameterTypes.length == 1) {
            if (notProperties(parameterTypes[0])) {
                throw new ModuleSystemException(exposeUsage(method));
            }
        }
    }

    private static final String SCALASET_SUFFIX = "_$eq";
}
