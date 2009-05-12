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

/**
 * A service registration, wrapping the OSGi ServiceRegistration and decorating
 * it with typed {@link vanadis.osgi.ServiceProperties}.
 */
public interface Registration<T> {

    /**
     * Service properties for the registration.
     *
     * @return Service properties
     */
    ServiceProperties<T> getServiceProperties();

    /**
     * The registered instance.
     *
     * @return The instance
     */
    T getInstance();

    /**
     * Disband the registration!
     *
     * @return False if unregistration failed
     */
    boolean unregister();

    long getServiceId();

    String getServicePid();

    /**
     * As {@link #unregister()}, but catch any exception and return it for inspection.
     * Mostly a convenience in shutdown situations.
     *
     * @return Any thrown exception
     */
    Throwable unregisterSafely();

}
