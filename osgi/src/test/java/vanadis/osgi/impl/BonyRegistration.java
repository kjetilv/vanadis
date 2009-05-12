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

import vanadis.core.lang.ToString;
import vanadis.osgi.Filter;
import vanadis.osgi.Registration;
import vanadis.osgi.ServiceProperties;

public class BonyRegistration<T> implements Registration<T> {

    private final T instance;

    private final ServiceProperties<T> serviceProperties;

    private final BareBonesContext context;

    private BonyReference<T> reference;

    public BonyRegistration(T instance, ServiceProperties<T> serviceProperties,
                            BareBonesContext context) {
        this.instance = instance;
        this.serviceProperties = serviceProperties;
        this.context = context;
        this.reference =
                new BonyReference<T>(serviceProperties.getMainClass(), instance);
    }

    @Override
    public boolean unregister() {
        context.unregister(this);
        return true;
    }

    @Override
    public Throwable unregisterSafely() {
        unregister();
        return null;
    }

    public boolean isMatchedBy(Class<?> serviceInterface, Filter filter) {
        return isMatchedBy(serviceInterface.getName(), filter);
    }

    public boolean isMatchedBy(String serviceInterfaceName, Filter filter) {
        return serviceProperties.isTyped(serviceInterfaceName) &&
                filter.matches(serviceProperties);
    }

    @Override
    public T getInstance() {
        return instance;
    }

    @Override
    public long getServiceId() {
        return 0;
    }

    @Override
    public String getServicePid() {
        return null;
    }

    @Override
    public ServiceProperties<T> getServiceProperties() {
        return serviceProperties;
    }

    public BonyReference<T> getReference() {
        return reference;
    }

    @Override
    public String toString() {
        return ToString.of(this, instance, serviceProperties.getPropertySet());
    }
}
