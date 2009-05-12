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

import vanadis.core.lang.EqHc;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;
import vanadis.core.properties.PropertySet;
import vanadis.core.properties.PropertySets;
import vanadis.osgi.AbstractReference;
import vanadis.osgi.ServiceProperties;
import vanadis.util.log.Log;
import vanadis.util.log.Logs;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

final class OSGiReference<T> extends AbstractReference<T> {

    private static final Log log = Logs.get(OSGiReference.class);

    private final BundleContext bundleContext;

    private final ServiceReference serviceReference;

    private final ServiceProperties<T> serviceProperties;

    private final String originalReference;

    private final int hashCode;

    static <T> OSGiReference<T> create(BundleContext bundleContext,
                                       Class<T> serviceInterfaceName,
                                       ServiceReference serviceReference) {
        return new OSGiReference<T>(bundleContext, serviceInterfaceName, serviceReference);
    }

    static <T> OSGiReference<T> create(BundleContext bundleContext,
                                       String serviceInterfaceName,
                                       ServiceReference serviceReference) {
        Class<T> type = Reflect.resolveServiceInterface(bundleContext, serviceInterfaceName, serviceReference);
        return type == null ? null
                : OSGiReference.create(bundleContext, type, serviceReference);
    }

    OSGiReference(BundleContext bundleContext,
                  Class<T> serviceInterface,
                  ServiceReference serviceReference) {
        super(serviceInterface);
        this.bundleContext = Not.nil(bundleContext, "bundle context");
        this.serviceReference = Not.nil(serviceReference, "service reference");
        this.serviceProperties = ServiceProperties.create(serviceInterface).with
                (propertieSetOf(serviceReference));
        this.originalReference = this.serviceReference.toString();
        this.hashCode = EqHc.hc(serviceReference);
    }

    @Override
    public ServiceProperties<T> getServiceProperties() {
        return serviceProperties;
    }

    @Override
    public boolean unget() {
        try {
            bundleContext.ungetService(serviceReference);
            return true;
        } catch (IllegalStateException e) {
            if (log.isDebug()) {
                log.debug(this + " got stale bundle", e);
            }
            return false;
        }
    }

    @Override
    public String getServicePid() {
        return ServiceReferences.getServicePid(serviceReference);
    }

    @Override
    public long getServiceId() {
        return ServiceReferences.getServiceId(serviceReference);
    }

    @Override
    public void close() {
        unget();
    }

    @Override
    protected Object get() {
        try {
            return bundleContext.getService(serviceReference);
        } catch (IllegalStateException e) {
            if (log.isDebug()) {
                log.debug(this + " got stale bundle, returning null", e);
            }
        }
        return null;
    }

    private static PropertySet propertieSetOf(ServiceReference serviceReference) {
        PropertySet propertySet = PropertySets.create();
        for (String key : serviceReference.getPropertyKeys()) {
            propertySet.set(key, serviceReference.getProperty(key));
        }
        return propertySet;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        OSGiReference<T> reference = EqHc.retyped(this, o);
        return reference == this || reference != null && EqHc.eq
                (serviceReference, reference.serviceReference);
    }

    @Override
    public String toString() {
        return ToString.of(this, getServiceInterface(), originalReference);
    }
}
