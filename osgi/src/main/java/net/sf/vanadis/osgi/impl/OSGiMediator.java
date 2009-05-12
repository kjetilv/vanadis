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
import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.osgi.*;
import net.sf.vanadis.util.log.Log;
import net.sf.vanadis.util.log.Logs;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

final class OSGiMediator<T> implements Mediator<T>, ServiceTrackerCustomizer {

    private static final Log log = Logs.get(OSGiMediator.class);

    private static final Object[] NO_SERVICES = new Object[]{};

    private final BundleContext bundleContext;

    private final Class<T> serviceInterface;

    private final ServiceTracker tracker;

    private final Set<MediatorListener<T>> mediatorListeners;

    OSGiMediator(BundleContext bundleContext,
                 Class<T> serviceInterface,
                 Filter filter,
                 MediatorListener<T>... mediatorListeners) {
        this.bundleContext = bundleContext;
        this.serviceInterface = serviceInterface;
        this.mediatorListeners = Generic.set(mediatorListeners);
        this.tracker = new ServiceTracker
                (this.bundleContext,
                 createFilter(this.bundleContext, serviceInterface, filter),
                 this);
        this.tracker.open(false);
    }

    private static <T> org.osgi.framework.Filter createFilter(BundleContext bundleContext,
                                                              Class<T> serviceInterface,
                                                              Filter filter) {
        String string = Filters.typeFilter(serviceInterface).and(filter).toFilterString();
        try {
            return bundleContext.createFilter(string);
        } catch (InvalidSyntaxException e) {
            throw new OSGiException("Did not parse to a filter : " + string, e);
        }
    }

    @Override
    public void close() {
        try {
            tracker.close();
        } catch (IllegalStateException e) {
            if (OSGiUtils.bundleNoLongerValid(e)) {
                log.debug(this + " failed to close, bundle no longer valid", e);
            } else {
                throw e;
            }
        }
    }

    public Class<T> getServiceInterface() {
        return serviceInterface;
    }

    @Override
    public T getService() {
        return toAT(tracker.getService());
    }

    @Override
    public Reference<T> getReference() {
        return new OSGiReference<T>(bundleContext, serviceInterface, tracker.getServiceReference());
    }

    private T toAT(Object service) {
        return serviceInterface.cast(service);
    }

    @Override
    public Set<T> getServices() {
        Object[] objects = services();
        Set<T> services = Generic.set(objects.length);
        for (Object object : objects) {
            services.add(toAT(object));
        }
        return services;
    }

    @Override
    public Collection<Reference<T>> getReferences() {
        ServiceReference[] serviceReferences = tracker.getServiceReferences();
        Collection<Reference<T>> references = Generic.list(serviceReferences.length);
        for (ServiceReference serviceReference : serviceReferences) {
            references.add(new OSGiReference<T>(bundleContext, serviceInterface, serviceReference));
        }
        return references;
    }

    @Override
    public boolean isEmpty() {
        ServiceReference[] serviceReferences = tracker.getServiceReferences();
        return serviceReferences == null || serviceReferences.length == 0;
    }

    private Object[] services() {
        Object[] services = tracker.getServices();
        return services == null ? NO_SERVICES : services;
    }

    @Override
    public Object addingService(ServiceReference serviceReference) {
        Object object;
        try {
            object = bundleContext.getService(serviceReference);
        } catch (IllegalStateException e) {
            if (log.isDebug()) {
                log.debug(this + " skips add, bundle context invalid", e);
            }
            return null;
        }
        if (object == null) {
            return null;
        }
        T service = serviceInterface.cast(object);
        for (MediatorListener<T> mediatorListener : mediatorListeners) {
            mediatorListener.added(reference(serviceReference), service);
        }
        log.debug(this + " forwarded " + service + " from " + serviceReference);
        return service;
    }

    @Override
    public void modifiedService(ServiceReference serviceReference, Object service) {
        log.info(this + " notified of modified service: " + service + ", reference: " + serviceReference);
    }

    @Override
    public void removedService(ServiceReference serviceReference, Object object) {
        T service = serviceInterface.cast(object);
        for (MediatorListener<T> mediatorListener : mediatorListeners) {
            OSGiReference<T> reference = reference(serviceReference);
            mediatorListener.removed(reference, service);
            reference.unget();
        }
        log.debug(this + " removed " + service + " from " + serviceReference);
    }

    private OSGiReference<T> reference(ServiceReference serviceReference) {
        return new OSGiReference<T>(bundleContext,
                                    serviceInterface,
                                    serviceReference);
    }

    @Override
    public Iterator<T> iterator() {
        return getServices().iterator();
    }

    @Override
    public String toString() {
        return ToString.of(this, serviceInterface, "tracker", tracker.getTrackingCount());
    }
}
