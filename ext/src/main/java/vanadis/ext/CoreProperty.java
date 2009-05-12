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

import vanadis.core.properties.PropertySet;
import vanadis.core.properties.PropertySets;
import vanadis.core.reflection.Retyper;
import vanadis.osgi.Filter;
import vanadis.osgi.Filters;
import org.osgi.framework.Constants;

import javax.management.ObjectName;

/**
 * The CoreProperty class models service registration properties
 * that are essential to Vanadis operation.
 *
 * @param <T> Type of property
 */
public final class CoreProperty<T> {

    /**
     * @see #SERVICE_ID
     */
    public static final String SERVICE_ID_NAME = Constants.SERVICE_ID;

    /**
     * @see #SERVICE_PID
     */
    public static final String SERVICE_PID_NAME = Constants.SERVICE_PID;

    /**
     * @see #REMOTABLE
     */
    public static final String REMOTABLE_NAME = "vanadis.remotable";

    /**
     * @see #REMOTE_PROXY
     */
    public static final String REMOTE_PROXY_NAME = "vanadis.remoteproxy";

    /**
     * @see #HOSTOBJECT_NAME
     */
    public static final String HOSTOBJECT_NAME_NAME = "vanadis.hostobject.name";

    /**
     * @see #HOSTOBJECT_TYPE
     */
    public static final String HOSTOBJECT_TYPE_NAME = "vanadis.hostobject.type";

    /**
     * @see #OBJECTNAME
     */
    public static final String OBJECTNAME_NAME = "vanadis.objectname";

    /**
     * @see #SERVICENAME
     */
    public static final String SERVICENAME_NAME = "vanadis.servicename";

    /**
     * @see #OBJECTMANAGER_NAME
     */
    public static final String OBJECTMANAGER_NAME_NAME = "vanadis.objectmanager.name";

    /**
     * @see #OBJECTMANAGER_TYPE
     */
    public static final String OBJECTMANAGER_TYPE_NAME = "vanadis.objectmanager.type";

    /**
     * @see #OBJECTCLASSES
     */
    public static final String OBJECTCLASSES_NAME = "objectClass";

    /**
     * service.id
     */
    public static final CoreProperty<Integer> SERVICE_ID =
            new CoreProperty<Integer>(SERVICE_ID_NAME, Integer.class);

    /**
     * service.pid
     */
    public static final CoreProperty<String> SERVICE_PID =
            new CoreProperty<String>(SERVICE_PID_NAME, String.class);

    /**
     * Is registered service remotable?
     */
    public static final CoreProperty<Boolean> REMOTABLE =
            new CoreProperty<Boolean>(REMOTABLE_NAME, Boolean.class);

    /**
     * Is registered service a remote proxy?
     */
    public static final CoreProperty<Boolean> REMOTE_PROXY =
            new CoreProperty<Boolean>(REMOTE_PROXY_NAME, Boolean.class);

    /**
     * Name of host module, for an exposed service.
     */
    public static final CoreProperty<String> HOSTOBJECT_NAME =
            new CoreProperty<String>(HOSTOBJECT_NAME_NAME, String.class);

    /**
     * Type of host module, for an exposed service.
     */
    public static final CoreProperty<String> HOSTOBJECT_TYPE =
            new CoreProperty<String>(HOSTOBJECT_TYPE_NAME, String.class);

    /**
     * Object name for an exposed jmx service.
     */
    public static final CoreProperty<ObjectName> OBJECTNAME =
            new CoreProperty<ObjectName>(OBJECTNAME_NAME, ObjectName.class);

    /**
     * Service name for an exposed jmx service.
     */
    public static final CoreProperty<String> SERVICENAME =
            new CoreProperty<String>(SERVICENAME_NAME, String.class);

    /**
     * Object manager name, for object manager registration.
     */
    public static final CoreProperty<String> OBJECTMANAGER_NAME =
            new CoreProperty<String>(OBJECTMANAGER_NAME_NAME, String.class);

    /**
     * Object manager type, for object manager registration.
     */
    public static final CoreProperty<String> OBJECTMANAGER_TYPE =
            new CoreProperty<String>(OBJECTMANAGER_TYPE_NAME, String.class);

    /**
     * Object manager type, for object manager registration.
     */
    public static final CoreProperty<String[]> OBJECTCLASSES =
            new CoreProperty<String[]>(OBJECTCLASSES_NAME, String[].class);

    private final String name;

    private final Class<T> type;

    public Class<T> getType() {
        return type;
    }

    private CoreProperty(String name, Class<T> type) {
        this.name = name;
        this.type = type;
        assert name.endsWith((name())) : this + " has wrong name " + name;
    }

    public Object coerce(Object object) {
        return Retyper.coerce(type, object);
    }

    public String name() {
        return name;
    }

    @SuppressWarnings({"unchecked"})
    public T lookupIn(PropertySet propertySet) {
        Object value = typedLookup(propertySet);
        return value == null ? null : getType().cast(value);
    }

    public PropertySet set(T value) {
        return set(PropertySets.create(), value);
    }

    public PropertySet set(PropertySet propertySet, T object) {
        propertySet.set(name(), coerce(object));
        return propertySet;
    }

    public PropertySet setIf(boolean condition, PropertySet propertySet, T object) {
        propertySet.setIf(condition, name(), coerce(object));
        return propertySet;
    }

    public Filter filter(T value) {
        return Filters.eq(name, coerce(value));
    }

    public boolean isSetIn(PropertySet propertySet) {
        return propertySet.has(name);
    }

    private Object typedLookup(PropertySet propertySet) {
        Object value = propertySet.get(name());
        return value == null ? null : Retyper.coerce(getType(), value);
    }
}
