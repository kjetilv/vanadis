/*
 * Copyright 2008 Kjetil Valstadsve
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
package net.sf.vanadis.ext;

/**
 * A callback interface for reacting to vanadis management events.  A convenient
 * way to support this interface is to extend {@link net.sf.vanadis.ext.AbstractCommand}.
 */
public interface ManagedLifecycle {

    /**
     * The instance has just been created.
     */
    void initialized();

    /**
     * Configuration data has been passed to the instance.
     *
     * @see Configure
     * @see Configuration
     */
    void configured();

    /**
     * Dependencies have been resolved.
     *
     * @see Inject
     */
    void dependenciesResolved();

    /**
     * Services have been exposed.
     *
     * @see Expose
     */
    void servicesExposed();

    /**
     * The instance is now assumed to be active.
     */
    void activate();

    /**
     * Dependencies were lost.
     */
    void dependenciesLost();

    /**
     * The instance was closed.
     */
    void closed();

}
