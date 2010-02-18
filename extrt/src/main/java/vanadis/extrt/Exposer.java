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

import org.osgi.framework.Constants;
import vanadis.core.collections.Generic;
import vanadis.core.collections.Pair;
import vanadis.core.lang.Not;
import vanadis.core.lang.Strings;
import vanadis.core.lang.ToString;
import vanadis.core.properties.PropertySet;
import vanadis.core.properties.PropertySets;
import vanadis.core.reflection.Retyper;
import vanadis.ext.*;
import vanadis.jmx.ManagedDynamicMBeans;
import vanadis.objectmanagers.ObjectManagerExposePointMBean;
import vanadis.osgi.Context;
import vanadis.osgi.Registration;
import vanadis.osgi.ServiceProperties;

import javax.management.DynamicMBean;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.reflect.Array;
import java.util.*;

abstract class Exposer<T> extends ManagedFeature<T, ObjectManagerExposePointMBean> implements Iterable<Registration<T>> {

    private final List<String> requiredDependencies;

    private final boolean persistent;

    private final ServiceProperties<T> properties;

    private final boolean remotable;

    private final List<Registration<T>> registrations = Generic.list();

    private final ObjectName objectName;

    private final List<JmxRegistration<?>> jmxRegistrations = Generic.list();

    private final ManagedDynamicMBeans mbeans;

    protected Exposer(FeatureAnchor<T> featureAnchor, Expose annotation, ManagedDynamicMBeans mbeans) {
        super(featureAnchor.asRequired(isRequired(annotation)), ObjectManagerExposePointMBean.class);
        this.mbeans = mbeans;
        this.requiredDependencies = Arrays.asList(annotation.requiredDependencies());
        this.remotable = annotation.remotable();
        this.persistent = annotation.persistent();
        this.properties = properties(annotation, featureAnchor.getPropertySet());
        this.objectName = objectName(annotation);
    }

    @Override
    protected ObjectManagerExposePointMBean mbean() {
        return new ObjectManagerExposePointMBeanImpl(this);
    }

    boolean isReadyToGo(Collection<String> names) {
        return names.containsAll(requiredDependencies);
    }

    @Override
    void activate() {
        if (!isComplete()) {
            Pair<Object, PropertySet> serviceAndProperties = getServiceAndProperties();
            if (serviceAndProperties != null) {
                register(getContext(), serviceAndProperties);
            }
        } else {
            throw new IllegalStateException(this + " is active");
        }
    }

    @Override
    public void performDeactivate() {
        try {
            for (Registration<T> registration : Generic.list(registrations)) {
                registration.unregister();
            }
        } finally {
            registrations.clear();
        }
    }

    @Override
    boolean isComplete() {
        return !registrations.isEmpty();
    }

    @Override
    public Iterator<Registration<T>> iterator() {
        return Generic.list(registrations).iterator();
    }

    ServiceProperties<T> getProperties() {
        return properties;
    }

    @Override
    boolean isMulti() {
        return registrations.size() > 1;
    }

    boolean isPersistent() {
        return persistent;
    }

    private ObjectName objectName(Expose annotation) {
        String name = Strings.neitherNullNorEmpty(annotation.objectName());
        if (name != null) {
            return fullySpecifiedObjectName(name);
        }
        String simpleName = annotation.simpleObjectName();
        if (!Strings.isBlank(simpleName)) {
            return synthesizedObjectName(simpleName);
        }
        if (annotation.managed()) {
            return synthesizedObjectName(null);
        }
        return null;
    }

    private ObjectName synthesizedObjectName(String name) {
        Hashtable<String, String> ht = hashtable(name);
        return toObjectName(ht);
    }

    private ObjectName toObjectName(Hashtable<String, String> ht) {
        String name = this.properties.getMainClass().getPackage().getName();
        try {
            return new ObjectName(name, ht);
        } catch (MalformedObjectNameException e) {
            throw new ModuleSystemException
                    (name + " was not accepted as an object name: " + ht, e);
        }
    }

    private Hashtable<String, String> hashtable(String name) {
        Hashtable<String, String> ht = new Hashtable<String, String>();
        PropertySet propertySet = this.properties.getPropertySet();
        for (String key : propertySet) {
            addToHash(ht, key, propertySet.get(key));
        }
        addMissing(ht, "name", name == null ? getObjectManager().getName() : name);
        addMissing(ht, "type", getObjectManager().getType());
        return ht;
    }

    private Pair<Object, PropertySet> getServiceAndProperties() {
        PropertySet runtimePropertySet = runtimeProperties();
        Object object = resolveExposedObject(runtimePropertySet);
        if (object == null) {
            if (isRequired()) {
                throw new ModuleSystemException(this + " retrieved null, non-optional service!");
            }
            return null;
        }
        if (object.getClass().isArray() && Array.getLength(object) == 0) {
            failRequiredArray();
            return null;
        }
        return Pair.of(object, runtimePropertySet);
    }

    private void failRequiredArray() {
        if (isRequired()) {
            throw new ModuleSystemException
                    (this + " retrieved empty array for non-optional service!");
        }
    }

    protected abstract Object resolveExposedObject(PropertySet runtimePropertySet);

    protected PropertySet runtimeProperties() {
        return null;
    }

    private void register(Context context, Pair<Object, PropertySet> serviceAndProperties) {
        PropertySet runtimePropertySet = setJmxProperty(serviceAndProperties.getTwo());
        Object object = serviceAndProperties.getOne();
        if (object.getClass().isArray()) {
            for (Object realObject : (Object[]) object) {
                registerSingleObject(context, realObject, runtimePropertySet);
            }
        } else {
            registerSingleObject(context, object, runtimePropertySet);
        }
    }

    private void registerSingleObject(Context context, Object object, PropertySet runtimePropertySet) {
        T service = getServiceInterface().cast(object);
        ServiceProperties<T> registerProperties = runtimePropertySet == null
                ? properties
                : properties.with(runtimePropertySet);
        this.registrations.add(context.register(service, registerProperties));
        registerJmx(service);
    }

    private void registerJmx(T service) {
        DynamicMBean mBean = mbeans.create(service);
        if (mBean != null) {
            jmxRegistrations.add(JmxRegistration.create
                    (getContext(), mBean, getObjectManager().getManagedClass().getName(),
                     PropertySets.create("type", getObjectManager().getType() + "-annotated-exposure",
                                         "name", getObjectManager().getName(),
                                         "feature", getFeatureName())));
        }
    }

    private ServiceProperties<T> properties(Expose annotation, PropertySet configuredProperties) {
        ServiceProperties<T> baseProperties =
                ServiceProperties.create(getServiceInterface(), annotation.objectClasses());
        PropertySet standardPropertySet = withStandards(harvestPropertySet(annotation));
        return baseProperties.with(standardPropertySet.with(configuredProperties));
    }

    private PropertySet withStandards(PropertySet propertySet) {
        propertySet.setIf(remotable, CoreProperty.REMOTABLE.name(), true);
        propertySet.set(CoreProperty.HOSTOBJECT_NAME.name(), getObjectManager().getName());
        propertySet.set(CoreProperty.HOSTOBJECT_TYPE.name(), getObjectManager().getType());
        propertySet.set(CoreProperty.SERVICENAME.name(), getObjectManager().getName() + ":" + getFeatureName());
        return propertySet;
    }

    private PropertySet setJmxProperty(PropertySet propertySet) {
        if (objectName == null) {
            return propertySet;
        }
        return propertySet == null
                ? CoreProperty.OBJECTNAME.set(objectName)
                : propertySet.set(CoreProperty.OBJECTNAME.name(), objectName);
    }

    private static PropertySet harvestPropertySet(Expose annotation) {
        PropertySet propertySet = PropertySets.create();
        int ranking = annotation.ranking();
        if (ranking != 0) {
            propertySet.set(Constants.SERVICE_RANKING, ranking);
        }
        String description = annotation.description();
        if (!Strings.isBlank(description)) {
            propertySet.set(Constants.SERVICE_DESCRIPTION, description);
        }
        String pid = annotation.pid();
        if (!Strings.isBlank(pid)) {
            propertySet.set(Constants.SERVICE_PID, pid);
        }
        Property[] propertyArray = annotation.properties();
        PropertyUtils.transferProperties(propertyArray, propertySet);
        return propertySet;
    }

    private static boolean isRequired(Expose annotation) {
        return !Not.nil(annotation, "annotation").optional();
    }

    private static void addToHash(Hashtable<String, String> ht, String key, Object value) {
        if (value == null) {
            return;
        }
        if (value.getClass().isArray()) {
            if (Array.getLength(value) == 1) {
                addToHash(ht, key, Array.get(value, 0));
            }
        } else {
            ht.put(key, jmxFriendly(Retyper.toString(value)));
        }
    }

    private static void addMissing(Hashtable<String, String> ht, String key, String value) {
        if (!ht.containsKey(key)) {
            ht.put(key, jmxFriendly(value));
        }
    }

    private static String jmxFriendly(String value) {
        return value.contains(":") ? value.replace(":", "_") : value;
    }

    private static ObjectName fullySpecifiedObjectName(String name) {
        try {
            return new ObjectName(name);
        } catch (MalformedObjectNameException e) {
            throw new ModuleSystemException(name + " was not accepted as an object name", e);
        }
    }

    @Override
    public String toString() {
        return ToString.of(this, getFeatureName(), "managed:", getObjectManager().getManagedObject());
    }

    @Override
    long[] getServiceIds() {
        List<Registration<T>> registrations = Generic.list(this.registrations);
        long[] ids = new long[registrations.size()];
        int i = 0;
        for (Registration<T> registration : registrations) {
            ids[i++] = registration.getServiceId();
        }
        return ids;
    }
}
