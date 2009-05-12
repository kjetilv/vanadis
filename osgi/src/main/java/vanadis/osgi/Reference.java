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

import java.io.Closeable;

/**
 * A service reference, wrapping the OSGi ServiceReference.
 * Generically typed.  Also contains a {@link vanadis.osgi.ServiceProperties
 * service properties} object.
 */
public interface Reference<T> extends Closeable {

    /**
     * <P>Get the service.</P>
     *
     * <P>Should be paired with a call to {@link #unget()}.</P>
     *
     * @return Service
     */
    T getService();

    /**
     * <P>Get the service as-is, without having to agree about the
     * type of the object.</P>
     *
     * <P>This method is mostly needed for reflective usage, i.e.
     * in a framework or in a test.</P>
     *
     * <P>For example, if you embed an
     * OSGi instance and retrieve references from it for testing,
     * the type of the object may be managed internally in the OSGi
     * runtime, and cannot be compared with an instance of the type
     * loaded in the test environment's classpath.</P>
     *
     * <P>Should be paired with a call to {@link #unget()}.</P>
     *
     * @return The same object as {@link #getService()}
     */
    Object getRawService();

    /**
     * Drop a reference to an object, after doing a {@link #getService()}
     * or {@link #getRawService()}.
     *
     * @return False if unget failed due to bundle issues
     */
    boolean unget();

    long getServiceId();

    String getServicePid();

    ServiceProperties<T> getServiceProperties();

}
