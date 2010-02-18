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
import vanadis.core.collections.Generic;
import vanadis.core.collections.Pair;
import vanadis.core.lang.AccessibleHelper;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;
import vanadis.core.reflection.ArgumentTypeMismatchException;
import vanadis.core.reflection.Invoker;

import javax.management.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class ManagedDynamicMBean implements DynamicMBean, MBeanRegistration {

    private static final String STRING = String.class.getName();

    private final ObjectName objectName;

    private final MBeanInfo mBeanInfo;

    private final ManagedDynamicMBeanType type;

    private final Object target;

    ManagedDynamicMBean(MBeanInfo mBeanInfo, ObjectName objectName,
                        ManagedDynamicMBeanType type,
                        Object target) {
        this.mBeanInfo = mBeanInfo;
        this.objectName = objectName;
        this.type = type;
        this.target = Not.nil(target, "target");
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
                // ?
            }
        }
        return attributeList;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature)
        throws MBeanException, ReflectionException {
        if (type.getOperations().containsKey(actionName)) {
            AnnotationDatum<Method> datum = type.getOperations().get(actionName);
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
        if (type.getAttributeMethods().containsKey(name)) {
            Pair<AnnotationDatum<Method>, AnnotationDatum<Method>> datumPair = type.getAttributeMethods().get(name);
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
        if (type.getAttributeMethods().containsKey(name)) {
            Pair<AnnotationDatum<Method>, AnnotationDatum<Method>> datumPair = type.getAttributeMethods().get(name);
            Method getter = accessible(datumPair.getOne().getElement());
            return typed(name, Invoker.invoke(this, target, getter));
        }
        if (type.getAttributeFields().containsKey(name)) {
            AnnotationDatum<Field> datum = type.getAttributeFields().get(name);
            Field field = AccessibleHelper.openSesame(datum.getElement());
            return typed(name, Invoker.get(this, target, field));
        }
        if (required) {
            throw new AttributeNotFoundException(name);
        }
        return null;
    }

    private Object typed(String name, Object value) {
        String type = this.type.getAttributeInfos().get(name).getType();
        return value == null ? null
            : type.equals(STRING) ? String.valueOf(value)
                : value;
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
