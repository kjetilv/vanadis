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
package vanadis.ext;

import vanadis.annopro.AnnotationDatum;
import vanadis.annopro.AnnotationsDigest;
import vanadis.annopro.AnnotationsDigests;
import vanadis.core.collections.Generic;
import vanadis.core.collections.Pair;
import vanadis.core.lang.AccessibleHelper;
import vanadis.core.lang.Not;
import vanadis.core.lang.Strings;
import vanadis.core.lang.ToString;
import vanadis.core.reflection.ArgumentTypeMismatchException;
import vanadis.core.reflection.GetNSet;
import vanadis.core.reflection.Invoker;

import javax.management.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ManagedDynamicMBean implements DynamicMBean, MBeanRegistration {

    public static DynamicMBean create(Object target) {
        AnnotationsDigest digest = AnnotationsDigests.createFromInstance(target);
        if (digest.hasClassData(Manageable.class) ||
                digest.hasMethodData(Attribute.class, Operation.class) ||
                digest.hasFieldData(Attribute.class)) {
            return new ManagedDynamicMBean(digest, target);
        }
        return null;
    }

    private final ObjectName objectName;

    private final MBeanAttributeInfo[] attributeInfos;

    private final MBeanOperationInfo[] operationInfos;

    private final Map<String, Pair<AnnotationDatum<Method>, AnnotationDatum<Method>>> attributeMethods;

    private final Map<String, AnnotationDatum<Method>> operations;

    private final Map<String, AnnotationDatum<Field>> attributeFields;

    private final MBeanInfo mBeanInfo;

    private final Object target;

    private ManagedDynamicMBean(AnnotationsDigest digest, Object target) {
        Not.nil(digest, "digest");
        this.target = Not.nil(target, "target");
        Manageable manageable = manageable(digest);
        objectName = objectName(manageable);
        attributeMethods = organizeAttributes(digest.getMethodData(Attribute.class));
        attributeFields = digest.getFieldDataIndex(Attribute.class);
        operations = digest.getMethodDataIndex(Operation.class);
        attributeInfos = attributeInfos();
        operationInfos = operationInfos();
        mBeanInfo = info(target, manageable);
    }


    @Override
    public Object getAttribute(String name)
            throws AttributeNotFoundException, ReflectionException {
        return get(name, true);
    }

    @Override
    public void setAttribute(javax.management.Attribute attribute)
            throws AttributeNotFoundException, InvalidAttributeValueException {
        set(attribute, true);
    }

    @Override
    public AttributeList setAttributes(AttributeList attributeList) {
        for (javax.management.Attribute attribute : attributeList.asList()) {
            try {
                set(attribute, false);
            } catch (Exception ignore) {
            }
        }
        return attributeList;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature)
            throws MBeanException, ReflectionException {
        if (operations.containsKey(actionName)) {
            AnnotationDatum<Method> datum = operations.get(actionName);
            Method method = AccessibleHelper.openSesame(datum.getElement());
            return Invoker.invoke(this, target, method, params);
        } else {
            throw new MBeanException(null, "No such operation: " + actionName + Arrays.toString(signature));
        }
    }

    @Override
    public AttributeList getAttributes(String[] names) {
        List<javax.management.Attribute> attributes = Generic.list();
        for (String name : names) {
            Object object = getIfExists(name);
            attributes.add(new javax.management.Attribute(name, object));
        }
        return new AttributeList(attributes);
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return mBeanInfo;
    }

    @Override
    public ObjectName preRegister(MBeanServer mBeanServer, ObjectName objectName) {
        return this.objectName == null ? objectName : this.objectName;
    }

    @Override
    public void postRegister(Boolean ignore) {
    }

    @Override
    public void preDeregister() {
    }

    @Override
    public void postDeregister() {
    }

    @SuppressWarnings({"ThrowInsideCatchBlockWhichIgnoresCaughtException"})
    private void set(javax.management.Attribute attribute, boolean required)
            throws AttributeNotFoundException, InvalidAttributeValueException {
        String name = attribute.getName();
        if (attributeMethods.containsKey(name)) {
            Pair<AnnotationDatum<Method>, AnnotationDatum<Method>> datumPair = attributeMethods.get(name);
            Method setter = AccessibleHelper.openSesame(datumPair.getTwo().getElement());
            try {
                Invoker.invoke(this, target, setter, attribute.getValue());
            } catch (ArgumentTypeMismatchException e) {
                throw new InvalidAttributeValueException(e.toString());
            }
        } else if (required) {
            throw new AttributeNotFoundException(name);
        }
    }

    private ObjectName objectName(Manageable manageable) {
        if (manageable == null) {
            return null;
        }
        String objectName = manageable.objectName();
        if (Strings.isEmpty(objectName)) {
            return null;
        }
        try {
            return new ObjectName(objectName);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("Invalid object name on " + target.getClass(), e);
        }
    }

    private MBeanInfo info(Object target, Manageable manageable) {
        String desc = manageable == null ? target.getClass().getName() : manageable.desc();
        return new MBeanInfo(target.getClass().getName(), desc,
                             attributeInfos, null, operationInfos, null);
    }

    private MBeanOperationInfo[] operationInfos() {
        List<MBeanOperationInfo> infos = Generic.list();
        for (Map.Entry<String, AnnotationDatum<Method>> entry : operations.entrySet()) {
            AnnotationDatum<Method> datum = entry.getValue();
            Method method = datum.getElement();
            Operation annotation = oper(datum);
            infos.add(new MBeanOperationInfo
                    (method.getName(),
                     annotation.desc(),
                     parameters(annotation, method),
                     annotation.string() ? STRING : type(method.getReturnType()),
                     annotation.impact()));
        }
        return infos.toArray(new MBeanOperationInfo[infos.size()]);
    }

    private MBeanAttributeInfo[] attributeInfos() {
        List<MBeanAttributeInfo> infos = Generic.list();
        for (Map.Entry<String, Pair<AnnotationDatum<Method>, AnnotationDatum<Method>>> entry : attributeMethods.entrySet()) {
            Pair<AnnotationDatum<Method>, AnnotationDatum<Method>> pair = entry.getValue();
            AnnotationDatum<Method> getDatum = pair.getOne();
            AnnotationDatum<Method> setDatum = pair.getTwo();
            Attribute annotation = getDatum != null ? attr(getDatum) : attr(setDatum);
            infos.add(new MBeanAttributeInfo
                    (entry.getKey(),
                     annotation.string() ? STRING : type(getDatum.getElement().getReturnType()),
                     annotation.desc(),
                     getDatum != null,
                     setDatum != null,
                     getDatum.getElement().getName().startsWith("is")));
        }
        for (Map.Entry<String, AnnotationDatum<Field>> entry : attributeFields.entrySet()) {
            Field field = entry.getValue().getElement();
            vanadis.ext.Attribute annotation = field.getAnnotation(Attribute.class);
            infos.add(new MBeanAttributeInfo
                    (entry.getKey(),
                     annotation.string() ? STRING : type(field.getType()),
                     annotation.desc(),
                     true,
                     false,
                     false));
        }
        return infos.toArray(new MBeanAttributeInfo[infos.size()]);
    }

    private Object getIfExists(String name) {
        Object object = null;
        try {
            object = get(name, false);
        } catch (AttributeNotFoundException e) {
            assert false : "Damn those checked exceptions: " + e;
        } catch (ReflectionException e) {
            assert false : "Damn those checked exceptions: " + e;
        }
        return object;
    }

    private static Attribute attr(AnnotationDatum<?> datum) {
        return datum.createProxy(ManagedDynamicMBean.class.getClassLoader(),
                                 Attribute.class);
    }

    private static Operation oper(AnnotationDatum<Method> datum) {
        return datum.createProxy(ManagedDynamicMBean.class.getClassLoader(),
                                 Operation.class);
    }

    private static Manageable mng(AnnotationDatum<Class<?>> datum) {
        return datum.createProxy(ManagedDynamicMBean.class.getClassLoader(),
                                 Manageable.class);
    }

    private static Manageable manageable(AnnotationsDigest digest) {
        AnnotationDatum<Class<?>> datum = digest.getClassDatum(Manageable.class);
        return datum == null ? null : mng(datum);
    }

    private static final String STRING = String.class.getName();

    private static MBeanParameterInfo[] parameters(Operation operation, Method method) {
        List<MBeanParameterInfo> infos = Generic.list();
        Param[] params = operation.params();
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            Param param = i < params.length ? params[i] : null;
            String name = param == null ? "param" + i : param.name();
            String desc = param == null ? "param" + i : param.desc();
            Class<?> parameter = method.getParameterTypes()[i];
            infos.add(new MBeanParameterInfo(name, type(parameter), desc));
        }
        return infos.toArray(new MBeanParameterInfo[infos.size()]);
    }

    private static String type(Class<?> returnType) {
        Class<?> type = returnType.isArray() ? returnType.getComponentType() : returnType;
        String name = type.getName();
        return returnType.isArray() ? "[L" + name + ";" : name;
    }

    private static Map<String, Pair<AnnotationDatum<Method>, AnnotationDatum<Method>>> organizeAttributes(List<AnnotationDatum<Method>> data) {
        Map<String, AnnotationDatum<Method>> getters = Generic.map();
        Map<String, AnnotationDatum<Method>> setters = Generic.map();
        Set<String> attributeNames = Generic.set();
        for (AnnotationDatum<Method> datum : data) {
            Method method = datum.getElement();
            String methodName = method.getName();
            Class<?> returnType = method.getReturnType();
            String get = GetNSet.getProperty(methodName);
            int pars = method.getParameterTypes().length;
            if (get != null && pars == 0 && returnsValue(returnType)) {
                getters.put(get, datum);
                attributeNames.add(get);
                continue;
            }
            String set = GetNSet.setProperty(methodName);
            if (set != null && pars == 1) {
                setters.put(set, datum);
                attributeNames.add(set);
                continue;
            }
            throw new IllegalArgumentException
                    ("Managed attribute method " + method + " is not a valid setter or getter");
        }
        Map<String, Pair<AnnotationDatum<Method>, AnnotationDatum<Method>>> attributes = Generic.map();
        for (String name : attributeNames) {
            AnnotationDatum<Method> getDatum = getters.get(name);
            AnnotationDatum<Method> setDatum = setters.get(name);
            if (nonMatching(getDatum, setDatum)) {
                throw new IllegalArgumentException
                        ("Managed read/write property " + name + " has non-matching getter/setter: " +
                                getDatum.getElement() + "/" + setDatum.getElement());
            }
            attributes.put(name, Pair.of(getDatum, setDatum));
        }
        return attributes;
    }

    private static boolean nonMatching(AnnotationDatum<Method> getDatum, AnnotationDatum<Method> setDatum) {
        return getDatum != null &&
                setDatum != null &&
                !getDatum.getElement().getReturnType().equals(setDatum.getElement().getParameterTypes()[0]);
    }

    private static boolean returnsValue(Class<?> returnType) {
        return returnType != null &&
                returnType != void.class &&
                returnType != Void.class;
    }

    private Object get(String name, boolean required)
            throws AttributeNotFoundException, ReflectionException {
        if (attributeMethods.containsKey(name)) {
            Pair<AnnotationDatum<Method>, AnnotationDatum<Method>> datumPair = attributeMethods.get(name);
            Method getter = accessible(datumPair.getOne().getElement());
            return Invoker.invoke(this, target, getter);
        }
        if (attributeFields.containsKey(name)) {
            AnnotationDatum<Field> datum = attributeFields.get(name);
            Field field = AccessibleHelper.openSesame(datum.getElement());
            return Invoker.get(this, target, field);
        }
        if (required) {
            throw new AttributeNotFoundException(name);
        }
        return null;
    }

    private static Method accessible(Method getter) throws ReflectionException {
        try {
            return AccessibleHelper.openSesame(getter);
        } catch (SecurityException e) {
            throw new ReflectionException(e, "Failed to access " + getter);
        }
    }

    @Override
    public String toString() {
        return ToString.of(this, "objectName", objectName, "target", target);
    }
}
