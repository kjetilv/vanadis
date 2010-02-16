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
package vanadis.osgi.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vanadis.core.collections.Generic;
import vanadis.common.io.Location;
import vanadis.core.lang.EqHc;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;
import vanadis.core.properties.PropertySet;
import vanadis.osgi.*;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Map;

public final class OSGiContext extends ContextAdapter {

    private final Map<ContextListener<?>, ServiceListenerAdapter<?>> contextListeners = Generic.map();

    private final BundleContext bundleContext;

    private final URI home;

    private final Location location;

    private final String symbolicName;

    private final ServiceProxyFactory serviceProxyFactory;

    private final URI repo;

    public OSGiContext(BundleContext bundleContext, URI home, Location location) {
        this(bundleContext, home, location, null);
    }

    public OSGiContext(BundleContext bundleContext, URI home, Location location,
                       URI repo) {
        this.bundleContext = Not.nil(bundleContext, "bundle context");
        this.home = Anchors.resolveHome(bundleContext, home);
        this.location = Anchors.resolveLocation(bundleContext, location);
        this.symbolicName = bundleContext.getBundle().getSymbolicName();
        this.serviceProxyFactory = new OSGiServiceProxyFactory(this);
        this.repo = repo;
    }

    @Override
    public String getName() {
        return bundleContext.getBundle().getSymbolicName();
    }

    @Override
    public URI getHome() {
        return home;
    }

    @Override
    public URI getRepo() {
        return repo;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public <T> Registration<T> register(T service, ServiceProperties<T> serviceProperties) {
        String[] classes = serviceProperties.getObjectClasses();
        PropertySet propertySet = serviceProperties.getPropertySet();
        Dictionary<String, Object> objectDictionary = propertySet.toDictionary("");
        ServiceRegistration serviceRegistration =
                bundleContext.registerService(classes, service, objectDictionary);
        OSGiRegistration<T> registration =
                new OSGiRegistration<T>(service, serviceProperties, serviceRegistration);
        log.info(this + " registered " + registration);
        return registration;
    }

    @Override
    public <T> void addContextListener(Class<T> serviceInterface,
                                       ContextListener<T> listener,
                                       Filter filter) {
        addContextListener(serviceInterface, listener, filter, true);
    }

    @Override
    public <T> void addContextListener(Class<T> serviceInterface,
                                       ContextListener<T> listener,
                                       Filter filter,
                                       boolean rewind) {
        if (contextListeners.containsKey(listener)) {
            contextListeners.remove(listener);
        }
        Class<T> type = serviceInterface == Object.class ? null : serviceInterface;
        ServiceListenerAdapter<T> adapter =
                new ServiceListenerAdapter<T>(bundleContext, type, listener, filter, rewind);
        contextListeners.put(listener, adapter);
        adapter.activate();
    }

    @Override
    public <T> void removeContextListener(ContextListener<T> contextListener) {
        ServiceListenerAdapter<?> adapter = contextListeners.remove(contextListener);
        if (adapter != null) {
            adapter.deactivate();
        }
    }

    @Override
    public Reference<?> getSingleReference(String serviceInterfaceName, Filter filter) {
        return lookupReference(null, serviceInterfaceName, filter, false);
    }

    @Override
    public <T> Reference<T> getSingleReference(Class<T> serviceInterface, Filter filter) {
        return lookupReference(serviceInterface, null, filter, false);
    }

    @Override
    public Reference<?> getReference(String serviceInterfaceName, Filter filter) {
        return lookupReference(null, serviceInterfaceName, filter, true);
    }

    @Override
    public <T> Reference<T> getReference(Class<T> serviceInterface, Filter filter) {
        return lookupReference(serviceInterface, null, filter, true);
    }

    @Override
    public <T> Reference<T> getReference(Class<T> serviceInterface) {
        return lookupReference(serviceInterface, null, null, true);
    }

    @Override
    public Reference<?> getReference(String serviceInterfaceName) {
        return lookupReference(null, serviceInterfaceName, null, true);
    }

    @Override
    public <T> Collection<Reference<T>> getReferences(Class<T> serviceInterface, Filter filter) {
        return getReferences(serviceInterface, null, filter);
    }

    @Override
    public <T> Mediator<T> createMediator(Class<T> serviceInterface, Filter filter,
                                          MediatorListener<T> listener) {
        return new OSGiMediator<T>(bundleContext, serviceInterface, filter, listener);
    }

    @Override
    public BundleMediator createBundleMediator(BundleMediatorListener bundleMediatorListener) {
        return new OSGiBundleMediator(bundleContext, bundleMediatorListener);
    }

    @Override
    public <T> T getServiceProxy(Class<T> serviceInterface, Filter filter) {
        return serviceProxyFactory.get(serviceInterface, filter);
    }

    @Override
    public <T> T getPersistentServiceProxy(Class<T> serviceInterface, Filter filter) {
        return serviceProxyFactory.getPersistent(serviceInterface, filter);
    }

    @Override
    public void closePersistentServiceProxy(Object service) {
        serviceProxyFactory.closePersistent(service);
    }

    @Override
    public Collection<Reference<?>> getReferences(String serviceInterfaceName, Filter filter) {
        ServiceReference[] serviceReferences = lookupReferences(serviceInterfaceName, filter);
        return toReferences(serviceReferences, serviceInterfaceName);
    }

    @Override
    public String getProperty(String property) {
        return bundleContext.getProperty(property);
    }

    @Override
    public URI getResource(String location) {
        return toURI(location, bundleContext.getBundle().getResource(location));
    }

    @Override
    public URI getEntry(String location) {
        return toURI(location, bundleContext.getBundle().getEntry(location));
    }

    @Override
    public PropertySet getPropertySet() {
        return new OSGiContextPropertySet(this);
    }

    private <T> ServiceReference[] lookupReferences(Class<T> serviceInterface,
                                                    String serviceInterfaceName,
                                                    Filter filter) {
        return lookupReferences(resolveServiceInterfaceName(serviceInterface, serviceInterfaceName), filter);
    }

    private ServiceReference[] lookupReferences(String name, Filter filter) {
        String filterString = filter == null ? null : filter.toFilterString();
        try {
            ServiceReference[] serviceReferences = bundleContext.getServiceReferences(name, filterString);
            return serviceReferences == null ? NO_SERVICE_REFERENCES
                    : serviceReferences;
        } catch (InvalidSyntaxException e) {
            throw new OSGiException("Invalid filter: " + filterString, e);
        }
    }

    private <T> Reference<T> lookupReference(Class<T> serviceInterface,
                                             String serviceInterfaceName,
                                             Filter filter,
                                             boolean justPickOne) {
        Collection<Reference<T>> references = getReferences(serviceInterface, serviceInterfaceName, filter);
        if (references == null || references.isEmpty()) {
            return null;
        }
        if (references.size() == 1 || justPickOne) {
            return references.iterator().next();
        }
        throw new IllegalArgumentException
                (this + " found " + references.size() + " hits for " + serviceInterface +
                        " and " + filter + ": " + references);
    }

    private <T> Collection<Reference<T>> getReferences(Class<T> serviceInterface,
                                                       String serviceInterfaceName,
                                                       Filter filter) {
        ServiceReference[] serviceReferences =
                lookupReferences(serviceInterface, serviceInterfaceName, filter);
        if (serviceReferences == null || serviceReferences.length == 0) {
            return Collections.emptySet();
        }
        return toReferences(serviceReferences, serviceInterface, serviceInterfaceName);
    }

    private <T> Reference<T> toReference(Class<T> serviceInterface,
                                         String serviceInterfaceName,
                                         ServiceReference serviceReference) {
        if (serviceInterface != null) {
            return new OSGiReference<T>(bundleContext, serviceInterface, serviceReference);
        }
        Not.nil(serviceInterfaceName, "service interface name");
        return OSGiReference.create
                (bundleContext, serviceInterfaceName, serviceReference);
    }

    private Reference<?> toReference(String serviceInterfaceName, ServiceReference serviceReference) {
        return OSGiReference.create(bundleContext, serviceInterfaceName, serviceReference);
    }

    private <T> Collection<Reference<T>> toReferences(ServiceReference[] serviceReferences,
                                                      Class<T> serviceInterface,
                                                      String serviceInterfaceName) {
        Collection<Reference<T>> references = Generic.set();
        for (ServiceReference reference : serviceReferences) {
            references.add(toReference(serviceInterface, serviceInterfaceName, reference));
        }
        return references;
    }

    private Collection<Reference<?>> toReferences(ServiceReference[] serviceReferences,
                                                  String serviceInterfaceName) {
        Collection<Reference<?>> references = Generic.set();
        for (ServiceReference reference : serviceReferences) {
            Reference<?> typedReference = toReference(serviceInterfaceName, reference);
            if (typedReference != null) {
                references.add(typedReference);
            }
        }
        return references;
    }

    private static <T> String resolveServiceInterfaceName(Class<T> serviceInterface, String serviceInterfaceName) {
        if (serviceInterface == null || serviceInterface == Object.class) {
            return serviceInterfaceName == null || serviceInterfaceName.equals(OBJECT_NAME) ? null
                    : serviceInterfaceName;
        }
        return serviceInterface.getName();
    }

    private static URI toURI(String location, URL resource) {
        if (resource == null) {
            return null;
        }
        try {
            return URI.create(resource.toExternalForm());
        } catch (Exception e) {
            throw new IllegalStateException
                    ("Failed to create URI from " + resource + ", found at location " + location, e);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(OSGiContext.class);

    private static final ServiceReference[] NO_SERVICE_REFERENCES = new ServiceReference[]{};

    private static final String OBJECT_NAME = Object.class.getName();

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object == null) {
            return false;
        }
        OSGiContext context = EqHc.retyped(this, object);
        return context != null && EqHc.eq(context.bundleContext, bundleContext);
    }

    @Override
    public int hashCode() {
        return EqHc.hc(bundleContext);
    }

    @Override
    public String toString() {
        return ToString.of(this, "name", symbolicName, "home", home, "@", location.toLocationString());
    }
}
