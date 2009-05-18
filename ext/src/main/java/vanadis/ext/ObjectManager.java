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
package vanadis.ext;

import vanadis.blueprints.ModuleSpecification;

import java.util.Collection;

/**
 * <P>The ObjectManager handles the lifecycle of exactly one managed object, in
 * terms of exposures, trackings and injections.  It is primarily applied to
 * {@link Module modules}. It is created by a dedicated factory , which is
 * created at bundle load time.  The factory can create instances of this
 * interface, using a {@link ModuleSpecification}.</P>
 *
 * <P>The object manager and its factory share a common {@link #getType() type} attribute.</P>
 */
public interface ObjectManager {

    /**
     * The object manager name.
     *
     * @return name
     */
    String getName();

    /**
     * The object manager type.
     *
     * @return type
     */
    String getType();

    /**
     * The origin service specification.
     *
     * @return Service specification
     */
    ModuleSpecification getModuleSpecification();

    /**
     * The object under management.
     *
     * @return Managed object
     */
    Object getManagedObject();

    /**
     * The managed class.
     *
     * @return Managed class
     */
    Class<?> getManagedClass();

    /**
     * True if the managed object can be launched.
     *
     * @return Launchable status
     */
    boolean isLaunchable();

    /**
     * Launch the managed object.
     */
    void launch();

    /**
     * Shutdown the managed object.
     */
    void shutdown();

    /**
     * True if the object has reported a failure.
     *
     * @return Failure status
     */
    boolean hasFailed();

    /**
     * The object manager's failure status.  If the object manager
     * {@link #hasFailed() has failed}, it will have at least one
     * {@link ObjectManagerFailures#getFailureCount() failure}.
     *
     * @return Failures status object
     */
    ObjectManagerFailures getFailures();

    /**
     * The state of the object, as seen from the manager.
     *
     * @return Managed state
     */
    ManagedState getManagedState();

    /**
     * The class loader of the managed object.
     *
     * @return Class loader
     */
    ClassLoader getManagedObjectClassLoader();

    Collection<ExposedServiceSummary> getExposedServices();

    Collection<InjectedServiceSummary> getInjectedServices();

    Collection<ConfigureSummary> getConfigureSummaries();
}
