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
package vanadis.jmx;

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
import vanadis.core.reflection.Invoker;

import javax.management.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static vanadis.jmx.JmxFiddly.*;

public class ManagedDynamicMBean implements DynamicMBean, MBeanRegistration {

    public static DynamicMBean create(Object target) {
        AnnotationsDigest digest = AnnotationsDigests.createFromInstance(target);
        if (digest.hasClassData(Managed.class) ||
            digest.hasMethodData(Attr.class, Operation.class) ||
            digest.hasFieldData(Attr.class)) {
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
        Managed manageable = managed(digest);
        objectName = objectName(manageable);
        attributeMethods = organizeAttributes(digest.getMethodData(Attr.class));
        attributeFields = digest.getFieldDataIndex(Attr.class);
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
    public void setAttribute(Attribute attribute)
        throws AttributeNotFoundException, InvalidAttributeValueException {
        set(attribute, true);
    }

    @Override
    public AttributeList setAttributes(AttributeList attributeList) {
        for (Attribute attribute : attributeList.asList()) {
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
        List<Attribute> attributes = Generic.list();
        for (String name : names) {
            Object object = getIfExists(name);
            attributes.add(new Attribute(name, object));
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

    private void set(Attribute attribute, boolean required)
        throws AttributeNotFoundException, InvalidAttributeValueException {
        String name = attribute.getName();
        if (attributeMethods.containsKey(name)) {
            Pair<AnnotationDatum<Method>, AnnotationDatum<Method>> datumPair = attributeMethods.get(name);
            Method setter = AccessibleHelper.openSesame(datumPair.getTwo().getElement());
            try {
                Invoker.invoke(this, target, setter, attribute.getValue());
            } catch (ArgumentTypeMismatchException e) {
                //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw new InvalidAttributeValueException(e.toString());
            }
        } else if (required) {
            throw new AttributeNotFoundException(name);
        }
    }

    private ObjectName objectName(Managed managedAnnotation) {
        if (managedAnnotation == null) {
            return null;
        }
        String objectName = managedAnnotation.objectName();
        if (Strings.isEmpty(objectName)) {
            return null;
        }
        try {
            return new ObjectName(objectName);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("Invalid object name on " + target.getClass(), e);
        }
    }

    private MBeanInfo info(Object target, Managed managedAnnotation) {
        String desc = managedAnnotation == null ? target.getClass().getName() : managedAnnotation.desc();
        return new MBeanInfo(target.getClass().getName(), desc,
                             attributeInfos, null, operationInfos, null);
    }

    private MBeanOperationInfo[] operationInfos() {
        List<MBeanOperationInfo> infos = Generic.list();
        for (Map.Entry<String, AnnotationDatum<Method>> entry : operations.entrySet()) {
            AnnotationDatum<Method> datum = entry.getValue();
            infos.add(beanOperationInfo(datum));
        }
        return infos.toArray(new MBeanOperationInfo[infos.size()]);
    }

    private MBeanAttributeInfo[] attributeInfos() {
        List<MBeanAttributeInfo> infos = Generic.list();
        for (Map.Entry<String, Pair<AnnotationDatum<Method>, AnnotationDatum<Method>>> entry : attributeMethods.entrySet()) {
            String key = entry.getKey();
            Pair<AnnotationDatum<Method>, AnnotationDatum<Method>> pair = entry.getValue();
            infos.add(beanAttributeInfo(key, pair));
        }
        for (Map.Entry<String, AnnotationDatum<Field>> entry : attributeFields.entrySet()) {
            String key = entry.getKey();
            Field field = entry.getValue().getElement();
            infos.add(beanAttributeInfo(key, field));
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

    private static Managed mng(AnnotationDatum<Class<?>> datum) {
        return datum.createProxy(ManagedDynamicMBean.class.getClassLoader(), Managed.class);
    }

    private static Managed managed(AnnotationsDigest digest) {
        AnnotationDatum<Class<?>> datum = digest.getClassDatum(Managed.class);
        return datum == null ? null : mng(datum);
    }

    @Override
    public String toString() {
        return ToString.of(this, "objectName", objectName, "target", target);
    }
}
