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
package vanadis.osgi;

import vanadis.core.io.Location;
import vanadis.core.properties.PropertySet;

import java.net.URI;
import java.util.Collection;

/**
 * Adapter class for implementing contexts.
 */
public class ContextAdapter implements Context {

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getHome() {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getRepo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Location getLocation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Registration<T> register(T service) {
        return register(service, (Class<T>) service.getClass());
    }

    @Override
    public <T, S extends T> Registration<T> register(S service, Class<T> serviceInterface) {
        return register(service, ServiceProperties.create(serviceInterface));
    }

    @Override
    public <T> Registration<T> register(T service, ServiceProperties<T> serviceProperties) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void addContextListener(Class<T> serviceInterface, ContextListener<T> listener, Filter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void addContextListener(Class<T> serviceInterface,
                                       ContextListener<T> listener,
                                       Filter filter,
                                       boolean rewind) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void removeContextListener(ContextListener<T> contextListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reference<?> getSingleReference(String serviceInterfaceName, Filter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Reference<T> getSingleReference(Class<T> serviceInterface, Filter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reference<?> getReference(String serviceInterfaceName, Filter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Reference<T> getReference(Class<T> serviceInterface, Filter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Reference<T> getReference(Class<T> serviceInterface) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reference<?> getReference(String serviceInterfaceName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Collection<Reference<T>> getReferences(Class<T> serviceInterface) {
        return getReferences(serviceInterface, null);
    }

    @Override
    public <T> Collection<Reference<T>> getReferences(Class<T> serviceInterface, Filter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Reference<?>> getReferences(String serviceInterfaceName) {
        return getReferences(serviceInterfaceName, null);
    }

    @Override
    public Collection<Reference<?>> getReferences(String serviceInterfaceName, Filter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getProperty(String property) {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getResource(String location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getEntry(String location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PropertySet getPropertySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final <T> Mediator<T> createMediator(Class<T> serviceInterface, MediatorListener<T> listener) {
        return createMediator(serviceInterface, null, listener);
    }

    @Override
    public <T> Mediator<T> createMediator(Class<T> serviceInterface, Filter filter, MediatorListener<T> listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BundleMediator createBundleMediator(BundleMediatorListener bundleMediatorListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getPersistentServiceProxy(Class<T> serviceInterface) {
        return getPersistentServiceProxy(serviceInterface, null);
    }

    @Override
    public <T> T getPersistentServiceProxy(Class<T> serviceInterface, Filter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServiceProxyFactory getServiceProxyFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void closePersistentServiceProxy(Object service) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getServiceProxy(Class<T> serviceInterface) {
        return getServiceProxy(serviceInterface, null);
    }

    @Override
    public <T> T getServiceProxy(Class<T> serviceInterface, Filter filter) {
        throw new UnsupportedOperationException();
    }
}
