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
import vanadis.osgi.OSGiUtils;
import vanadis.osgi.Registration;
import vanadis.osgi.ServiceProperties;
import vanadis.util.log.Log;
import vanadis.util.log.Logs;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

final class OSGiRegistration<T> implements Registration<T> {

    private static final Log log = Logs.get(OSGiRegistration.class);

    private final ServiceRegistration osgiRegistration;

    private final T instance;

    private final ServiceProperties<T> properties;

    private final String originalRegistration;

    private final int hashCode;

    <C extends T> OSGiRegistration(Class<T> serviceInterface, C instance) {
        this(instance, ServiceProperties.create(serviceInterface));
    }

    <C extends T> OSGiRegistration(C instance, ServiceProperties<T> properties) {
        this(instance, properties, null);
    }

    <C extends T> OSGiRegistration(C instance, ServiceProperties<T> properties,
                                   ServiceRegistration osgiRegistration) {
        this.instance = Not.nil(instance, "instance");
        this.properties = Not.nil(properties, "properties");
        this.osgiRegistration = Not.nil(osgiRegistration, "osgi registration");
        this.originalRegistration = this.osgiRegistration.toString();
        this.hashCode = EqHc.hc(osgiRegistration);
    }

    @Override
    public String getServicePid() {
        return ServiceReferences.getServicePid(osgiRegistration);
    }

    @Override
    public long getServiceId() {
        return ServiceReferences.getServiceId(osgiRegistration);
    }

    @Override
    public T getInstance() {
        return instance;
    }

    @Override
    public ServiceProperties<T> getServiceProperties() {
        return properties;
    }

    @Override
    public boolean unregister() {
        Bundle bundle = getBundle();
        if (bundle != null) {
            boolean active = OSGiUtils.isActive(bundle);
            if (active) {
                return attemptUnregister();
            } else if (log.isDebug()) {
                log.debug(this + " skipped unregister, bundle not active, in state " + bundle.getState());
            }
        } else {
            log.info(this + " skipped unregister, registration returned null bundle");
        }
        return false;
    }

    @Override
    public Throwable unregisterSafely() {
        try {
            unregister();
        } catch (Throwable e) {
            return e;
        }
        return null;
    }

    ServiceRegistration getOsgiRegistration() {
        return osgiRegistration;
    }

    private boolean attemptUnregister() {
        try {
            osgiRegistration.unregister();
            return true;
        } catch (IllegalStateException e) {
            if (e.getMessage().toLowerCase().contains("already unregistered")) {
                log.debug(this + " failed to unregister, " +
                        "registration already unregistered: " + e);
            }
            return false;
        }
    }

    private Bundle getBundle() {
        try {
            return osgiRegistration.getReference().getBundle();
        } catch (IllegalStateException e) {
            log.debug(this + " failed get bundle, already unregistered: " + e);
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        OSGiRegistration<T> reg = EqHc.retyped(this, o);
        return reg != null && eq(reg);
    }

    private boolean eq(OSGiRegistration<T> reg) {
        try {
            return EqHc.eq(osgiRegistration, reg.osgiRegistration);
        } catch (IllegalStateException ignore) {
            return EqHc.eq(instance, reg.instance,
                           properties, reg.properties);
        }
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return ToString.of
                (this, instance, "prop", properties, "reg", originalRegistration);
    }
}
