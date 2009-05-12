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
package vanadis.osgi;

import vanadis.core.collections.Generic;
import vanadis.core.lang.EqHc;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;
import vanadis.core.properties.PropertySet;
import vanadis.core.properties.PropertySets;
import org.osgi.framework.Constants;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <P>Service Properties aggregate a regular {@link vanadis.core.properties.PropertySet} object with a
 * service interface.  Unlike the Properties class, instances of ServiceProperties
 * are always immutable, and protect their Properties objects.
 * Any changes will return a new instance.</P>
 *
 * <P>ServiceProperties instances are used for {@link vanadis.osgi.Context}
 * {@link vanadis.osgi.Registration registrations}, and have a
 * {@link #getMainClass() main class} property that reflects the service interface
 * for the registration, e.g. {@link vanadis.osgi.Context#register(Object, ServiceProperties)}</P>
 *
 * <P>ServiceProperties are serializable, but they are sensitive to class loading
 * issues at deserialization.  As such, a deserialized instance will have lost its
 * {@link #getMainClass() main class} reference, but a full instance can always be
 * acquired by telling it to {@link #reinflate(ClassLoader) reinflate}.
 */
public class ServiceProperties<T> implements Serializable {

    private static final long serialVersionUID = -4301632484086192749L;

    private static final String[] NO_OBJECT_CLASSES = new String[]{};

    /**
     * Create a new ServiceProperties object, with optional, additional classes.
     *
     * @param mainClass   The main class
     * @param propertySet Properties, may be null
     * @param classes     Optional, additional classes
     * @return A new ServiceProperties object
     */
    public static <T> ServiceProperties<T> create(Class<T> mainClass,
                                                  PropertySet propertySet,
                                                  Class<?>... classes) {
        return new ServiceProperties<T>(validMain(mainClass), propertySet, classes, null);
    }

    /**
     * Create a new ServiceProperties object, with optional, additional classes.
     *
     * @param mainClass The main class
     * @param classes   Optional, additional classes
     * @return A new ServiceProperties object
     */
    public static <T> ServiceProperties<T> create(Class<T> mainClass,
                                                  Class<?>... classes) {
        return new ServiceProperties<T>(validMain(mainClass), null, classes, null);
    }

    /**
     * Create a new ServiceProperties object, with optional, additional classes.
     *
     * @param mainClass The main class
     * @param map       Properties given as a map, may be null
     * @param classes   Optional, additional classes
     * @return A new ServiceProperties object
     */
    public static <T> ServiceProperties<T> create(Class<T> mainClass,
                                                  Map<String, ?> map,
                                                  Class<?>... classes) {
        return new ServiceProperties<T>(validMain(mainClass), PropertySets.immutableFrom(map), classes, null);
    }

    private static <T> Class<T> validMain(Class<T> mainClass) {
        return Not.nil(mainClass, "main service interface");
    }

    private final PropertySet propertySet;

    private final int hashCode;

    private final transient Class<T> mainClass;

    private final String mainClassName;

    private Filter filter;

    private ServiceProperties(Class<T> mainClass, PropertySet propertySet, Class<?>[] objectClasses,
                              PropertySet additional) {
        this(mainClass, propertySet, objectClasses, additional, false);
    }

    private ServiceProperties(Class<T> mainClass, PropertySet propertySet) {
        this(mainClass, propertySet, null, null, true);
    }

    private ServiceProperties(Class<T> mainClass, PropertySet propertySet, Class<?>[] objectClasses,
                              PropertySet additional,
                              boolean inflation) {
        this.propertySet = inflation ? (propertySet == null ? PropertySets.EMPTY : propertySet)
                : objectClasses(mainClass, propertySet, objectClasses, additional);
        this.mainClass = mainClass;
        this.mainClassName = mainClass.getName();
        this.hashCode = EqHc.hc(this.mainClassName, this.propertySet);
    }

    private PropertySet objectClasses(Class<T> mainClass,
                                      PropertySet propertySet,
                                      Class<?>[] objectClasses,
                                      PropertySet additional) {
        String[] classes = combinedObjectClasses(mainClass, objectClasses);
        PropertySet compiledPropertySet = additional == null
                ? consolidateWithProperties(propertySet, classes)
                : consolidatedAndUnified(propertySet, additional);
        return compiledPropertySet.copy(false);
    }

    private static String[] unifiedObjectClasses(PropertySet propertySet, PropertySet additional) {
        List<String> objectClasses = Generic.list((String[]) propertySet.get(Constants.OBJECTCLASS));
        if (additional.has(Constants.OBJECTCLASS)) {
            Object additionalObject = additional.get(Constants.OBJECTCLASS);
            if (additionalObject instanceof String[]) {
                List<String> additionalObjectClasses = Generic.list((String[]) additionalObject);
                validateExistingIsSubsetOfAdditional(objectClasses, additionalObjectClasses);
                appendAdditionalObjectClasses(objectClasses, additionalObjectClasses);
            }
        }
        return objectClasses.toArray(new String[objectClasses.size()]);
    }

    private static void appendAdditionalObjectClasses(List<String> objectClasses,
                                                      List<String> additionalObjectClasses) {
        List<String> newObjectClasses = Generic.list();
        for (String additionalObjectClass : additionalObjectClasses) {
            if (!objectClasses.contains(additionalObjectClass)) {
                newObjectClasses.add(additionalObjectClass);
            }
        }
        objectClasses.addAll(newObjectClasses);
    }

    private static void validateExistingIsSubsetOfAdditional(List<String> existingObjectClasses,
                                                             List<String> additionalObjectClasses) {
        for (String existingObjectClass : existingObjectClasses) {
            if (!additionalObjectClasses.contains(existingObjectClass)) {
                throw new IllegalArgumentException
                        ("Could not consolidate existing object classes " + existingObjectClasses +
                                " with additional " + additionalObjectClasses);
            }
        }
    }

    private static PropertySet consolidatedAndUnified(PropertySet propertySet, PropertySet additional) {
        PropertySet overlaid = propertySet == null ? additional : propertySet.with(additional, true);
        String[] unifiedObjectClasses = unifiedObjectClasses(propertySet, additional);
        return overlaid.set(Constants.OBJECTCLASS, unifiedObjectClasses);
    }

    private static PropertySet consolidateWithProperties(PropertySet propertySet, String[] objectClasses) {
        Object objectClassProperty = propertySet == null ? null
                : propertySet.get(Constants.OBJECTCLASS);
        if (objectClassProperty == null) {
            PropertySet base = propertySet == null ? PropertySets.create() : propertySet.copy(true);
            return base.set(Constants.OBJECTCLASS, objectClasses);
        }
        if (objectClassProperty instanceof String[]) {
            String[] propertyObjectClasses = (String[]) objectClassProperty;
            if (Arrays.equals(propertyObjectClasses, objectClasses)) {
                return propertySet;
            }
        }
        throw new IllegalArgumentException
                ("Cannot consolidate conflicting object classes, propertySet contained " +
                        objectClassesToString(propertySet) + ", construction argument was: " +
                        Arrays.toString(objectClasses));
    }

    private static String objectClassesToString(PropertySet propertySet) {
        Object objectClassProperty = propertySet.get(Constants.OBJECTCLASS);
        return objectClassProperty instanceof String[] ? Arrays.toString((String[]) objectClassProperty) :
                String.valueOf(objectClassProperty);
    }

    /**
     * Create a filter that will match this ServiceProperties instance.
     *
     * @return Matching filter
     */
    public Filter toFilter() {
        if (filter == null) {
            filter = createFilter();
        }
        return filter;
    }

    private Filter createFilter() {
        Filter filter = Filters.objectClasses(getObjectClasses());
        for (String property : propertySet) {
            if (!property.equals(Constants.OBJECTCLASS)) {
                filter = filter.and(Filters.eq(property, propertySet.get(property)));
            }
        }
        return filter;
    }

    /**
     * Return a new ServiceProperties instance which is a copy of this ServiceProperties,
     * only with a different class.
     *
     * @param retypeClass A different class
     * @return A different ServiceProperties - but similar!
     */
    public <R> ServiceProperties<R> retypedTo(Class<R> retypeClass) {
        return new ServiceProperties<R>(retypeClass, propertySet);
    }

    /**
     * Create a new ServiceProperties which restores its reference to the
     * {@link #getMainClass() main class} using the given class loader.
     *
     * @param classLoader A class loader which should find the {@link #getMainClassName() main class by name}
     * @return A new, ServiceProperties instance with the real {@link #getMainClass() main class}
     */
    @SuppressWarnings({"unchecked"})
    public ServiceProperties<T> reinflate(ClassLoader classLoader) {
        if (mainClass != null) {
            return this;
        }
        Class<T> mainClass;
        try {
            mainClass = (Class<T>) classLoader.loadClass(mainClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException
                    (this + " failed to inflate with " + classLoader, e);
        }
        return new ServiceProperties<T>(mainClass, propertySet);
    }

    /**
     * The main class.  If this instance has been deserialized and not
     * {@link #reinflate(ClassLoader) reinflated}, a {@link IllegalStateException} will be thrown.
     *
     * @return The main class
     * @throws IllegalStateException If this instance is not inflated
     */
    public Class<T> getMainClass() {
        if (mainClass == null) {
            throw new IllegalStateException
                    (this + " has no main class, needs to be inflated with the proper class loader!");
        }
        return mainClass;
    }

    /**
     * Get the name of mainc class.
     *
     * @return Main class name
     */
    public String getMainClassName() {
        String[] classes = getObjectClasses();
        return classes == null || classes.length == 0 ? null : classes[0];
    }

    /**
     * Return a new ServiceProperties containing the existing Properties overlaid
     * by the argument properties.  If the argument is null, it returns itself.
     *
     * @param additional Properties that will overlay/replace existing properties, may be null
     * @return A new ServiceProperties with the additional properties overlaid
     */
    public ServiceProperties<T> with(PropertySet additional) {
        return additional == null || additional.isEmpty()
                ? this
                : new ServiceProperties<T>(getMainClass(), propertySet, null, additional);
    }

    /**
     * Get service Id.  It is found in the {@link #getPropertySet() properties}.
     *
     * @return Service id
     */
    public Long getServiceId() {
        return propertySet.getLong(Constants.SERVICE_ID);
    }

    public String[] getObjectClasses() {
        Object object = propertySet.get(Constants.OBJECTCLASS);
        if (object instanceof String[]) {
            return (String[]) object;
        }
        return NO_OBJECT_CLASSES;
    }

    public String getServicePid() {
        return propertySet.getString(Constants.SERVICE_PID);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        ServiceProperties<T> serviceProperties = EqHc.retyped(this, o);
        return serviceProperties != null && EqHc.eq
                (mainClassName, serviceProperties.mainClassName,
                 propertySet, serviceProperties.propertySet);
    }

    @Override
    public String toString() {
        return ToString.of(this, Arrays.toString(getObjectClasses()), propertySet);
    }

    public boolean isTyped(Class<?> serviceInterface) {
        return isTyped(serviceInterface.getName());
    }

    /**
     * Return true if this ServiceRegistration covers the named type.
     *
     * @param serviceInterfaceName Name of service interface
     * @return True if this is the main class or an additional registration class
     */
    public boolean isTyped(String serviceInterfaceName) {
        return Arrays.asList(getObjectClasses()).contains(serviceInterfaceName);
    }

    /**
     * The {@link vanadis.core.properties.PropertySet} that make up this
     * service registration.
     *
     * @return Properties
     */
    public PropertySet getPropertySet() {
        return propertySet;
    }

    private static String[] combinedObjectClasses(Class<?> mainClass, Class<?>... classes) {
        String mainClassName = mainClass.getName();
        List<String> classNames = Generic.list();
        if (!mainClassName.equals(Object.class.getName())) {
            classNames.add(mainClassName);
        }
        if (classes != null) {
            for (Class<?> clazz : classes) {
                if (clazz != null) {
                    classNames.add(clazz.getName());
                }
            }
        }
        return classNames.toArray(new String[classNames.size()]);
    }

    /**
     * Convenience method to retrieve the package name of the main service interface.
     *
     * @return Package name
     */
    public String getPackageName() {
        int idx = mainClassName.lastIndexOf(".");
        return mainClassName.substring(0, idx);
    }
}
