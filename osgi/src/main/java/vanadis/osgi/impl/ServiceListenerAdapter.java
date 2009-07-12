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

import org.osgi.framework.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vanadis.core.lang.EqHc;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;
import vanadis.osgi.*;
import vanadis.osgi.Filter;

final class ServiceListenerAdapter<T> implements ServiceListener {

    private static final Logger log = LoggerFactory.getLogger(ServiceListenerAdapter.class);

    private static final ServiceReference[] NO_SERVICE_REFERENCES = new ServiceReference[]{};

    private final BundleContext bundleContext;

    private final ContextListener<T> contextListener;

    private final Class<T> serviceInterface;

    private final Filter filter;

    private final boolean rewind;

    private final String bundleString;

    ServiceListenerAdapter(BundleContext bundleContext,
                           Class<T> serviceInterface,
                           ContextListener<T> contextListener, Filter filter,
                           boolean rewind) {
        this.filter = filter;
        this.rewind = rewind;
        this.bundleContext = Not.nil(bundleContext, "bundle context");
        this.contextListener = Not.nil(contextListener, "context listener");
        this.serviceInterface = serviceInterface;
        this.bundleString = this.bundleContext.getBundle().toString();
    }

    @Override
    public void serviceChanged(ServiceEvent event) {
        ContextListener<T> listener = contextListener;
        ServiceReference serviceReference = event.getServiceReference();
        OSGiReference<T> reference = toReference(serviceReference);
        if (reference != null) {
            try {
                forward(event, listener, reference);
            } catch (Throwable e) {
                log.error(this + " detected failure when forwarding " + event +
                        " about " + reference +
                        " to " + listener, e);
            }
        }
    }

    private void forward(ServiceEvent event, ContextListener<T> listener, OSGiReference<T> reference) {
        int type = event.getType();
        if (type == ServiceEvent.REGISTERED) {
            listener.serviceRegistered(reference);
        } else if (type == ServiceEvent.UNREGISTERING) {
            listener.serviceUnregistering(reference);
        } else if (type == ServiceEvent.MODIFIED) {
            listener.serviceModified(reference);
        }
    }

    private Class<T> serviceInterface(ServiceReference reference) {
        Bundle bundle;
        try {
            bundle = bundleContext.getBundle();
        } catch (IllegalStateException e) {
            logStaleBundle(e);
            return null;
        }
        if (!OSGiUtils.isActive(bundle)) {
            return null;
        }
        Object service;
        try {
            service = bundleContext.getService(reference);
        } catch (IllegalStateException e) {
            logStaleBundle(e);
            return null;
        }
        if (service == null) {
            return null;
        }
        try {
            Object objectClassValue = reference.getProperty(Constants.OBJECTCLASS);
            if (objectClassValue instanceof String[]) {
                String[] objectClasses = (String[]) objectClassValue;
                return loadUnchecked(service.getClass().getClassLoader(), objectClasses[0], service);
            } else if (objectClassValue instanceof String) {
                return loadUnchecked(service.getClass().getClassLoader(), (String) objectClassValue, service);
            }
            return this.serviceInterface;
        } finally {
            bundleContext.ungetService(reference);
        }
    }

    private void logStaleBundle(IllegalStateException e) {
        if (log.isDebugEnabled()) {
            log.debug(this + " experienced stale bundle, returning null", e);
        }
    }

    @SuppressWarnings({"unchecked"})
    private static <T> Class<T> loadUnchecked(ClassLoader loader, String objectClass, Object service) {
        try {
            return (Class<T>) Class.forName(objectClass, false, loader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException
                    (service + " with class loader " + loader + " should be able to load " +
                            objectClass, e);
        }
    }

    public ContextListener<T> getContextListener() {
        return contextListener;
    }

    public void activate() {
        try {
            bundleContext.addServiceListener(this, listeningFilter());
        } catch (InvalidSyntaxException e) {
            throw new OSGiException("Invalid filter: " + filter, e);
        }
        if (rewind) {
            ServiceReference[] serviceReferences = existingServiceReferences();
            for (ServiceReference serviceReference : serviceReferences) {
                contextListener.serviceRegistered(toReference(serviceReference));
            }
        }
    }

    private OSGiReference<T> toReference(ServiceReference reference) {
        Class<T> serviceInterface = serviceInterface(reference);
        return serviceInterface == null ? null
                : new OSGiReference<T>(bundleContext, serviceInterface, reference);
    }

    private String listeningFilter() {
        return getListeningFilter().toFilterString();
    }

    public Filter getListeningFilter() {
        Filter typeFilter = serviceInterface == null ? Filters.NULL : Filters.typeFilter(serviceInterface);
        return typeFilter.and(this.filter);
    }

    public void deactivate() {
        bundleContext.removeServiceListener(this);
    }

    @Override
    public int hashCode() {
        return EqHc.hc(contextListener, bundleContext.getBundle().getSymbolicName());
    }

    @Override
    public boolean equals(Object o) {
        ServiceListenerAdapter<T> adapter = EqHc.retyped(this, o);
        return adapter == this || adapter != null && EqHc.eq
                (contextListener, adapter.contextListener,
                 bundleContext.getBundle().getSymbolicName(), adapter.bundleContext.getBundle().getSymbolicName());
    }

    @Override
    public String toString() {
        return ToString.of(this, "listener", contextListener, "bundle", bundleString);
    }

    public ServiceReference[] existingServiceReferences() {
        try {
            ServiceReference[] serviceReferences = bundleContext.getServiceReferences
                    (serviceInterface == null ? null : serviceInterface.getName(), listeningFilter());
            return serviceReferences == null ? NO_SERVICE_REFERENCES : serviceReferences;
        } catch (InvalidSyntaxException e) {
            throw new OSGiException("Invalid filter: " + filter, e);
        }
    }
}
