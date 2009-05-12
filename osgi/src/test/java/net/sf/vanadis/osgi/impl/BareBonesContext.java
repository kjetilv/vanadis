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
package net.sf.vanadis.osgi.impl;

import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.io.Location;
import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.core.properties.PropertySet;
import net.sf.vanadis.core.properties.PropertySets;
import net.sf.vanadis.osgi.*;
import org.osgi.framework.Constants;

import java.net.URI;
import java.util.*;

@SuppressWarnings({"unchecked"})
public class BareBonesContext extends ContextAdapter
        implements Iterable<BonyRegistration<?>> {

    private long counter;

    private static final Location DEF_LOC = new Location(8000);

    private Location location;

    @Override
    public String getName() {
        return "barebones";
    }

    private final Map<Long, BonyRegistration<?>> registry = Generic.map();

    private final Map<ContextListener<?>, Filter> listenerFilters = Generic.map();

    private final Map<ContextListener<?>, Class<?>> typedListeners = Generic.map();

    private final PropertySet propertySet;

    public BareBonesContext() {
        this(null, DEF_LOC);
    }

    public <T> List<BonyRegistration<T>> registrations(Class<T> serviceInterface) {
        List<BonyRegistration<T>> registrations = Generic.list();
        for (BonyRegistration<?> registration : registry.values()) {
            if (registration.getServiceProperties().getMainClass().equals(serviceInterface)) {
                registrations.add((BonyRegistration<T>) registration);
            }
        }
        return registrations;
    }

    @Override
    public String toString() {
        return ToString.of(this, "listeners", listenerFilters.keySet(), "registry", registry.keySet());
    }

    public BareBonesContext(PropertySet propertySet) {
        this(propertySet, DEF_LOC);
    }

    public BareBonesContext(Location location) {
        this(null, location);
    }

    public BareBonesContext(PropertySet propertySet,
                            Location location) {
        this.propertySet = propertySet == null ? PropertySets.create() : propertySet;
        this.location = location;
    }

    @Override
    public PropertySet getPropertySet() {
        return new OSGiContextPropertySet(this);
    }

    public Map<ContextListener<?>, Filter> getListenerFilters() {
        return listenerFilters;
    }

    public void setProperty(String property, String string) {
        propertySet.set(property, string);
    }

    class BonyMediator<T> extends AbstractContextListener<T>
            implements Mediator<T> {

        private final Map<Reference<T>, T> map = Generic.map();

        private final MediatorListener<T> listener;

        BonyMediator(MediatorListener<T> listener) {
            this.listener = listener;
        }

        @Override
        public void serviceUnregistering(Reference<T> reference) {
            for (Reference<T> key : map.keySet()) {
                if (key.equals(reference)) {
                    key.unget();
                    T t = map.remove(key);
                    listener.removed(key, t);
                    return;
                }
            }
        }

        @Override
        public void serviceRegistered(Reference<T> reference) {
            T service = reference.getService();
            map.put(reference, service);
            listener.added(reference, service);
        }

        @Override
        public T getService() {
            return map.isEmpty() ? null : map.values().iterator().next();
        }

        @Override
        public Reference<T> getReference() {
            return map.isEmpty() ? null : map.keySet().iterator().next();
        }

        @Override
        public Collection<T> getServices() {
            return map.values();
        }

        @Override
        public Collection<Reference<T>> getReferences() {
            return map.keySet();
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public void close() {
            for (Reference<T> ref : map.keySet()) {
                ref.unget();
            }
            map.clear();
        }

        @Override
        public Iterator<T> iterator() {
            return null;
        }
    }

    @Override
    public <T> Mediator<T> createMediator(Class<T> serviceInterface,
                                          Filter filter,
                                          MediatorListener<T> listener) {
        BonyMediator<T> bonyMediator = new BonyMediator<T>(listener);
        addContextListener(serviceInterface, bonyMediator, filter);
        return bonyMediator;
    }

    @Override
    public String getProperty(String property) {
        return propertySet.withParent(PropertySets.systemProperties()).getString
                (property, PropertySets.systemProperties());
    }

    @Override
    public <T> Reference<T> getReference(Class<T> serviceInterface, Filter filter) {
        for (BonyRegistration<?> registration : registry.values()) {
            if (registration.isMatchedBy(serviceInterface.getName(), filter)) {
                return (Reference<T>) registration.getReference();
            }
        }
        return null;
    }

    public int registrationCount() {
        return registry.size();
    }

    public int listenerCount() {
        return listenerFilters.size();
    }

    public List<BonyRegistration<?>> registrations() {
        return Generic.list(registry.values());
    }

    public BonyRegistration<?> registration(long l) {
        return registry.get(l);
    }

    public <T> Set<ContextListener<T>> listeners(Class<T> serviceInterface) {
        Set<ContextListener<T>> set = Generic.set();
        for (Map.Entry<ContextListener<?>, Class<?>> entry : typedListeners.entrySet()) {
            if (entry.getValue().equals(serviceInterface)) {
                set.add((ContextListener<T>) entry.getKey());
            }
        }
        return set;
    }

    @Override
    public Collection<Reference<?>> getReferences(String serviceInterfaceName, Filter filter) {
        Set<Reference<?>> refs = Generic.set();
        for (BonyRegistration<?> registration : registry.values()) {
            if (registration.isMatchedBy(serviceInterfaceName, filter)) {
                refs.add(registration.getReference());
            }
        }
        return refs;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> Collection<Reference<T>> getReferences(Class<T> serviceInterface, Filter filter) {
        Set<Reference<T>> refs = Generic.set();
        for (BonyRegistration<?> registration : registry.values()) {
            if (registration.isMatchedBy(serviceInterface.getName(), filter)) {
                refs.add((Reference<T>) registration.getReference());
            }
        }
        return refs;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> Reference<T> getReference(Class<T> serviceInterface) {
        for (BonyRegistration<?> registration : registry.values()) {
            if (registration.getServiceProperties().getMainClass().isAssignableFrom(serviceInterface)) {
                return ((BonyRegistration<T>) registration).getReference();
            }
        }
        return null;
    }

    @Override
    public <T> void addContextListener(Class<T> serviceInterface, ContextListener<T> listener, Filter filter) {
        addContextListener(serviceInterface, listener, filter, true);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> void addContextListener(Class<T> serviceInterface,
                                       ContextListener<T> listener,
                                       Filter filter,
                                       boolean rewind) {
        Filter typeFilter = serviceInterface == null || serviceInterface == Object.class
                ? Filters.NULL
                : Filters.typeFilter(serviceInterface);
        listenerFilters.put(listener, typeFilter.and(filter));
        typedListeners.put(listener, serviceInterface);
        if (rewind) {
            for (BonyRegistration<?> registration : registry.values()) {
                if (typeFilter.and(filter).matches(registration.getServiceProperties())) {
                    listener.serviceRegistered
                            ((Reference<T>) registration.getReference());
                }
            }
        }
    }

    @Override
    public <T> void removeContextListener(ContextListener<T> contextListener) {
        listenerFilters.remove(contextListener);
    }

    @Override
    public <T> Reference<T> getSingleReference(Class<T> serviceInterface,
                                               Filter filter) {
        return getReference(serviceInterface, filter);
    }

    @SuppressWarnings({"unchecked"})
    <T> Registration<T> unregister(BonyRegistration<T> registration) {
        Long id = registration.getServiceProperties().getServiceId();
        if (registration != registry.get(id)) {
            throw new IllegalStateException(this + " could not unregister " + registration);
        }
        registry.remove(id);
        for (Map.Entry<ContextListener<?>, Filter> entry : listenerFilters.entrySet()) {
            if (entry.getValue().matches(registration.getServiceProperties())) {
                ((ContextListener<T>) entry.getKey()).serviceUnregistering(registration.getReference());
            }
        }
        return registration;
    }

    @Override
    public URI getRepo() {
        return null;
    }

    @Override
    public URI getHome() {
        return null;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public BareBonesContext setLocation(Location location) {
        this.location = location;
        return this;
    }

    @Override
    public <T> Registration<T> register(T service, ServiceProperties<T> serviceProperties) {
        counter++;
        long id = counter;
        BonyRegistration<T> registration =
                new BonyRegistration<T>(service, serviceProperties.with
                        (PropertySets.create(Constants.SERVICE_ID, id)), this);
        registry.put(id, registration);
        for (Map.Entry<ContextListener<?>, Filter> entry : listenerFilters.entrySet()) {
            if (entry.getValue().matches(registration.getServiceProperties())) {
                ContextListener<T> listener = (ContextListener<T>) entry.getKey();
                listener.serviceRegistered(registration.getReference());
            }
        }
        return registration;
    }

    @Override
    public Iterator<BonyRegistration<?>> iterator() {
        return registry.values().iterator();
    }
}
