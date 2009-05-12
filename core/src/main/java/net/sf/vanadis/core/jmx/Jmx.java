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

package net.sf.vanadis.core.jmx;

import net.sf.vanadis.core.lang.Not;
import net.sf.vanadis.core.system.VM;

import javax.management.*;

public class Jmx {

    public static void registerJmx(ObjectName objectName, DynamicMBean dynamicMBean) {
        registerJmx(objectName, dynamicMBean, null);
    }

    public static <T> void registerJmx(ObjectName objectName, T mbean, Class<T> beanClass) {
        verify(objectName, mbean);
        try {
            Object mBean = mbean instanceof DynamicMBean ? mbean
                    : new StandardMBean(mbean, Not.nil(beanClass, "bean class"));
            VM.JMX.registerMBean(mBean, objectName);
        } catch (InstanceAlreadyExistsException e) {
            throw new JmxException
                    ("Failed to reregister " + describeArgs(objectName, mbean, beanClass), e);
        } catch (RuntimeOperationsException e) {
            throw new JmxException
                    ("Runtime operations exception " + describeArgs(objectName, mbean, beanClass), e);
        } catch (Exception e) {
            throw new JmxException
                    ("Failed to register " + describeArgs(objectName, mbean, beanClass), e);
        }
    }

    private static <T> void verify(Object name, T mbean) {
        Not.nil(name, "name");
        Not.nil(mbean, "mBean");
    }

    private static <T> String describeArgs(Object name, T mbean, Class<T> beanClass) {
        return mbean + ":" + beanClass + " @ " + name;
    }

    public static <T> void registerJmx(String name, T mbean, Class<T> beanClass) {
        verify(name, mbean);
        registerJmx(objectName(name), mbean, beanClass);
    }

    public static void unregisterJmx(String name) {
        unregisterJmx(objectName(name));
    }

    private static ObjectName objectName(String name) {
        Not.nil(name, "name");
        try {
            return new ObjectName(name);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse object name " + name, e);
        }
    }

    public static void unregisterJmx(ObjectName objectName) {
        try {
            VM.JMX.unregisterMBean(Not.nil(objectName, "object name"));
        } catch (InstanceNotFoundException e) {
            throw new JmxException("Registration not found: " + objectName, e);
        } catch (Exception e) {
            throw new JmxException("Failed to unregister " + objectName, e);
        }
    }
}
