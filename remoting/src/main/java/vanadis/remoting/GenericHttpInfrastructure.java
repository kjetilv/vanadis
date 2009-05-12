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
package vanadis.remoting;

import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import vanadis.core.io.Location;
import vanadis.core.time.TimeSpan;

public class GenericHttpInfrastructure extends AbstractHttpInfrastructure {

    private final GrizzlyAdapter adapter;

    public GenericHttpInfrastructure(GrizzlyAdapter adapter) {
        this(TimeSpan.HALF_MINUTE, DEFAULT_THREADS, DEFAULT_THREADS, false, adapter);
    }

    public GenericHttpInfrastructure(TimeSpan keepAlive,
                                     int coreThreads, int maxThreads,
                                     boolean endPoint,
                                     GrizzlyAdapter adapter) {
        this(null, keepAlive, coreThreads, maxThreads, endPoint, adapter);
    }

    public GenericHttpInfrastructure(Location location,
                                     TimeSpan keepAlive,
                                     int coreThreads, int maxThreads,
                                     boolean endPoint,
                                     GrizzlyAdapter adapter) {
        super(location, keepAlive, coreThreads, maxThreads, endPoint);
        this.adapter = adapter;
    }

    @Override
    protected GrizzlyAdapter adapter(ClassLoader classLoader) {
        if (adapter == null) {
            return failIllegalState();
        }
        return adapter;
    }

    private GrizzlyAdapter failIllegalState() {
        Class<? extends GenericHttpInfrastructure> clazz = getClass();
        String header = clazz == GenericHttpInfrastructure.class
                ? "Plain instance of " + clazz + " "
                : clazz + " should override adapter(ClassLoader), or this instance ";
        throw new IllegalStateException
                (header + "should be constructed with an adapter: " + this);
    }
}
