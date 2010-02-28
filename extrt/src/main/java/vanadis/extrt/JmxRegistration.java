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

import vanadis.core.lang.Not;
import vanadis.core.properties.PropertySet;
import vanadis.ext.CoreProperty;
import vanadis.osgi.Context;
import vanadis.osgi.Registration;
import vanadis.osgi.ServiceProperties;

import javax.management.DynamicMBean;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

final class JmxRegistration<T> {

    private final Registration<T> registration;

    static <T> JmxRegistration<T> create(Context context, Class<T> type, T object, PropertySet propertySet) {
        return create(context, type, object, null, propertySet);
    }

    static JmxRegistration<?> create(Context context, DynamicMBean dynamicMBean, String domain,
                                     PropertySet propertySet) {
        return create(context, null, dynamicMBean, domain, propertySet);
    }

    static <T> JmxRegistration<T> create(Context context, Class<T> type, T object, String domain,
                                         PropertySet propertySet) {
        ObjectName objectName = createObjectName(type, domain, propertySet);
        return create(context, type, object, objectName);
    }

    private static <T> JmxRegistration<T> create(Context context, Class<T> type, T object, ObjectName objectName) {
        return new JmxRegistration<T>(context, type, object, objectName);
    }

    private JmxRegistration(Context context, Class<T> type, T object, ObjectName objectName) {
        ServiceProperties<T> properties = type == null
                ? serviceProperties(object, objectName)
                : serviceProperties(type, objectName);
        registration = context.register(object, properties);
    }

    void unregister() {
        registration.unregister();
    }

    private static <T> ObjectName createObjectName(Class<T> type, String name, PropertySet propertySet) {
        StringBuilder sb = new StringBuilder();
        for (String key : Not.nil(propertySet, "property set")) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(key).append("=").append(propertySet.getString(key).replace(":", "_"));
        }
        String domain = name == null ? Not.nil(type, "type").getName() : name;
        try {
            return new ObjectName(domain + ":" + sb.toString());
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("Invalid object name input: " + propertySet, e);
        }
    }

    @SuppressWarnings({"unchecked"})
    private static <T> ServiceProperties<T> serviceProperties(T instance, ObjectName objectName) {
        Class<T> type = (Class<T>) instance.getClass();
        return ServiceProperties.create(type, properties(objectName));
    }

    private static <T> ServiceProperties<T> serviceProperties(Class<T> type, ObjectName objectName) {
        return ServiceProperties.create(type, properties(objectName));
    }

    private static PropertySet properties(ObjectName objectName) {
        return CoreProperty.OBJECTNAME.set(objectName);
    }
}
