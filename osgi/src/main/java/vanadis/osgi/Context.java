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

import vanadis.common.io.Location;
import vanadis.core.properties.PropertySet;

import java.net.URI;
import java.util.Collection;

/**
 * A simplified, typed, and generics-friendly view of the BundleContext.  In particular,
 * this interface encourages you to use Class objects instead of class names, and pays
 * you back with typed references.
 */
public interface Context {

    /**
     * Bundle symbolic name.
     *
     * @return Symbolic name
     */
    String getName();

    /**
     * Root directory of the runtime.
     *
     * @return Root directory
     */
    URI getHome();

    /**
     * Repo directory of the runtime.
     *
     * @return Repo directory
     */
    URI getRepo();

    /**
     * Network location of the runtime.  This serves as a base location.  Services should
     * choose their network locations using this as an offset.
     *
     * @return Network location
     */
    Location getLocation();

    /**
     * Register a service with no service properties except its service interfaces.
     *
     * @param service          Service
     * @return A registration
     */
    <T> Registration<T> register(T service);

    /**
     * Register a service with no service properties except its service interfaces.
     *
     * @param service          Service
     * @param serviceInterface Servce
     * @return A registration
     */
    <T, S extends T> Registration<T> register(S service, Class<T> serviceInterface);

    /**
     * Register a service with service properties.  The service interface is given
     * by the service properties.
     *
     * @param service           Service
     * @param serviceProperties service properties
     * @return A registration
     */
    <T> Registration<T> register(T service, ServiceProperties<T> serviceProperties);

    /**
     * Register a context listener.  The context listener will be hit with all future
     * registrations.  Use {@link #addContextListener(Class, ContextListener, Filter, boolean)}
     * to get hit with all existing registrations as well.
     *
     * @param serviceInterface Service interface to listen for.  May be null or {@link Object}.class to
     *                         denote any interface
     * @param listener         Listener The listener
     * @param filter           A filter to limit callbacks, may be null or the {@link Filters#NULL null filter}.
     */
    <T> void addContextListener(Class<T> serviceInterface, ContextListener<T> listener, Filter filter);

    /**
     * Register a context listener.  The context listener will be hit with all future
     * registrations.  Optionally, it may <em>also</em> be immediately hit with all matching,
     * existing registrations.
     *
     * @param serviceInterface Service interface to listen for.  May be null or {@link Object}.class to
     *                         denote any interface
     * @param listener         Listener The listener
     * @param filter           A filter to limit callbacks, may be null or the {@link Filters#NULL null filter}.
     * @param rewind           If true, immediately hit the listener with all existing registrations
     */
    <T> void addContextListener(Class<T> serviceInterface, ContextListener<T> listener, Filter filter,
                                boolean rewind);

    /**
     * Remove a context listener.
     *
     * @param contextListener Context listener
     */
    <T> void removeContextListener(ContextListener<T> contextListener);

    /**
     * Get a reference to a typed service, or null if none are available.
     *
     * @param serviceInterface Service interface.  May be null or {@link Object}.class to
     *                         denote any interface
     * @return Any matching registration, null
     */
    <T> Reference<T> getReference(Class<T> serviceInterface);

    /**
     * Get a reference to a typed service, or null if none are available.
     * Passing the interface name is mainly useful in reflective programming,
     * where the caller may not see to the actual service interface.
     *
     * @param serviceInterfaceName Service May be null or {@link Object}.class to
     *                             denote any interface
     * @return Any matching registration, or null
     */
    Reference<?> getReference(String serviceInterfaceName);

    /**
     * Get a reference to a typed service with a filter, or null if none are match.
     *
     * @param serviceInterface Service interface.  May be null or {@link Object}.class to
     *                         denote any interface
     * @param filter           Filter
     * @return Any matching registration, null
     */
    <T> Reference<T> getReference(Class<T> serviceInterface, Filter filter);

    /**
     * Get a reference to a typed service with a filter, or null if none are match.
     * Passing the interface name is mainly useful in reflective programming,
     * where the caller may not see to the actual service interface.
     *
     * @param serviceInterfaceName Service interface name.  May be null or {@link Object}.class to
     *                             denote any interface
     * @param filter               Filter
     * @return Any matching registration, null
     */
    Reference<?> getReference(String serviceInterfaceName, Filter filter);

    /**
     * Get a reference to a typed service with a filter, or null if none are match.
     * Expects a single match, fails if there are are more.
     *
     * @param serviceInterface Service interface.  May be null or {@link Object}.class to
     *                         denote any interface
     * @param filter           Filter
     * @return Single reference, or null if no match
     * @throws IllegalArgumentException If there are multiple hits.
     */
    <T> Reference<T> getSingleReference(Class<T> serviceInterface, Filter filter);

    /**
     * Get a reference to a typed service with a filter, or null if none are match.
     * Expects a single match, fails if there are are more.
     * Passing the interface name is mainly useful in reflective programming,
     * where the caller may not see to the actual service interface.
     *
     * @param serviceInterfaceName Service interface name
     * @param filter               Filter
     * @return Single reference, or null if no match
     * @throws IllegalArgumentException If there are multiple hits.
     */
    Reference<?> getSingleReference(String serviceInterfaceName, Filter filter);

    /**
     * Get all references matching the service interface and the filter.  Returns
     * the empty set if there are no matches.
     *
     * @param serviceInterface Service interface.  May be null or {@link Object}.class to
     *                         denote any interface
     * @param filter           Filter
     * @return Non-null collection of matching references
     */
    <T> Collection<Reference<T>> getReferences(Class<T> serviceInterface, Filter filter);

    /**
     * Get all references matching the service interface.  Returns
     * the empty set if there are no matches.
     *
     * @param serviceInterface Service interface.  May be null or {@link Object}.class to
     *                         denote any interface
     * @return Non-null collection of matching references
     */
    <T> Collection<Reference<T>> getReferences(Class<T> serviceInterface);

    /**
     * @param serviceInterfaceName Service interface name.  May be null or {@link Object}.class to
     *                             denote any interface
     * @param filter               Filter
     * @return Non-null collection of matching references
     */
    Collection<Reference<?>> getReferences(String serviceInterfaceName, Filter filter);

    /**
     * @param serviceInterfaceName Service interface name.  May be null or {@link Object}.class to
     *                             denote any interface
     * @return Non-null collection of matching references
     */
    Collection<Reference<?>> getReferences(String serviceInterfaceName);

    /**
     * Create a mediator.  A mediator wraps an OSGi ServiceTracker.
     *
     * @param serviceInterface Service interface to listen for.  May be null or {@link Object}.class to
     *                         denote any interface
     * @param listener         Listener, may be null
     * @return A mediator
     */
    <T> Mediator<T> createMediator(Class<T> serviceInterface, MediatorListener<T> listener);

    /**
     * @param serviceInterface Service interface to listen for.  May be null or {@link Object}.class to
     *                         denote any interface
     * @param filter           Filter
     * @param listener         Mediator listener, may be null
     * @return A mediator
     */
    <T> Mediator<T> createMediator(Class<T> serviceInterface, Filter filter, MediatorListener<T> listener);

    /**
     * <P>Create a bundle mediator.  Passing a bundle mediator listener is optional.  If it
     * is passed, it will immediately be
     * {@link vanadis.osgi.BundleMediatorListener#activated(org.osgi.framework.Bundle) added}
     * with all existing bundles.</P>
     *
     * @param bundleMediatorListener Bundle mediator listener
     * @return Mediator
     */
    BundleMediator createBundleMediator(BundleMediatorListener bundleMediatorListener);

    /**
     * Creates a service proxy.  The proxy will look up the service on
     * each call, throwing an {@link IllegalStateException} whenever
     * a target service is not found.
     *
     * @param serviceInterface Service interface to listen for.  May be null or {@link Object}.class to
     *                         denote any interface
     * @param filter           Filter
     * @return A service proxy
     */
    <T> T getServiceProxy(Class<T> serviceInterface, Filter filter);

    /**
     * Creates a service proxy.  The proxy will keep a reference until it is
     * {@link #closePersistentServiceProxy(Object) closed}.
     *
     * @param serviceInterface Service interface to listen for.  May be null or {@link Object}.class to
     *                         denote any interface
     * @param filter           Filter
     * @return A service proxy
     */
    <T> T getPersistentServiceProxy(Class<T> serviceInterface, Filter filter);

    /**
     * Creates a service proxy.  The proxy will look up the service on
     * each call, throwing an {@link IllegalStateException} whenever
     * a target service is not found.
     *
     * @param serviceInterface Service interface to listen for.  May be null or {@link Object}.class to
     *                         denote any interface
     * @return A service proxy
     */
    <T> T getServiceProxy(Class<T> serviceInterface);

    /**
     * Creates a service proxy.  The proxy will keep a reference until it is
     * {@link #closePersistentServiceProxy(Object) closed}.
     *
     * @param serviceInterface Service interface to listen for.  May be null or {@link Object}.class to
     *                         denote any interface
     * @return A service proxy
     */
    <T> T getPersistentServiceProxy(Class<T> serviceInterface);

    /**
     * Close the persistent service proxy, produced by {@link #getPersistentServiceProxy(Class, Filter)}.
     *
     * @param service Persistent service proxy.
     */
    void closePersistentServiceProxy(Object service);

    /**
     * Get the service proxy factory used to get {@link #getServiceProxy(Class, Filter) service proxies}
     * and {@link #getPersistentServiceProxy(Class, Filter) persistent service proxies}.
     *
     * @return Service Proxy Factory
     */
    ServiceProxyFactory getServiceProxyFactory();

    /**
     * Properties for the bundle context.
     *
     * @return Properties
     */
    PropertySet getPropertySet();

    /**
     * Shortcut for bundle context property access.
     *
     * @param property Property name
     * @return String value of property
     */
    String getProperty(String property);

    /**
     * Get a resource from the bundle's class path
     *
     * @param location Reference to resource
     * @return URL for the resource
     */
    URI getResource(String location);

    /**
     * Get an entry from the bundle's class path.
     *
     * @param location Reference to entry
     * @return URL for the entry
     */
    URI getEntry(String location);
}
