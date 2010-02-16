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
package vanadis.modules.httpprovider;

import vanadis.common.io.Location;
import vanadis.common.time.TimeSpan;
import vanadis.remoting.AbstractHttpInfrastructure;
import vanadis.remoting.GenericHttpInfrastructure;

public class HttpRemotingInfrastructure extends GenericHttpInfrastructure {

    public HttpRemotingInfrastructure() {
        this(AbstractHttpInfrastructure.DEFAULT_KEEPALIVE);
    }

    public HttpRemotingInfrastructure(TimeSpan keepAlive) {
        this(null, keepAlive, 1, 1, false);
    }

    public HttpRemotingInfrastructure(Location location,
                                      TimeSpan keepAlive,
                                      int coreThreads, int maxThreads,
                                      boolean endPoint) {
        super(location, keepAlive, coreThreads, maxThreads, endPoint, null);
    }

    @Override
    protected InvocationAdapter adapter(ClassLoader classLoader) {
        return new InvocationAdapter(AbstractHttpInfrastructure.getRootPath(), classLoader);
    }
}
