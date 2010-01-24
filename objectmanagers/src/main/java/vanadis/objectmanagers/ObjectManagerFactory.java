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
package vanadis.objectmanagers;

import vanadis.blueprints.ModuleSpecification;

import java.util.Collection;

/**
 * The object manager factory is associated with exactly one
 * module.  It is capable of creating new instances of
 * {@link ObjectManager object managers} from
 * {@link ModuleSpecification service specifications}.
 */
public interface ObjectManagerFactory extends Iterable<ModuleSpecification> {

    /**
     * Launch the specified service.  Its {@link ModuleSpecification#getType() type}
     * must correspond to this factory's type.  The returned
     * object manager will be stored internally.
     *
     * @param moduleSpecification Service specification
     * @return Object manager
     */
    ObjectManager launch(ModuleSpecification moduleSpecification);

    /**
     * Auto-launch any services specified in the annotation.
     *
     * @return Auto-launched object managers
     */
    Collection<ObjectManager> autoLaunch();

    /**
     * True if this factory has launched the service specification.
     *
     * @param moduleSpecification Service specification
     * @return True if the service specification is known and has been launched.
     */
    boolean hasLaunched(ModuleSpecification moduleSpecification);

    /**
     * Return number of specifications launched.
     *
     * @return Launch count
     */
    int getLaunchCount();

    /**
     * Close the service specification, provided it has been launched.
     *
     * @param moduleSpecification Service specification
     */
    void close(ModuleSpecification moduleSpecification);

    /**
     * All returned {@link ObjectManager object managers} will have
     * this {@link ObjectManager#getType() type}.
     *
     * @return Type
     */
    String getType();

    /**
     * @return Implementation class
     */
    Class<?> getModuleClass();

    /**
     * Corresponds to the underlying OSGi bundle's symbolic name.
     *
     * @return Bundle symbolic name
     */
    String getContextName();

    /**
     * Shutdown the factory.
     */
    void shutdown();

}
