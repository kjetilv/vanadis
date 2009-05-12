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
package vanadis.modules.rmiprovider;

import vanadis.core.io.Location;
import vanadis.core.lang.ToString;
import vanadis.ext.AbstractModule;
import vanadis.ext.Configure;
import vanadis.ext.Expose;
import vanadis.ext.Module;
import vanadis.services.remoting.RemoteClientFactory;
import vanadis.services.remoting.RemotingInfrastructure;

@Module(moduleType = "rmiprovider")
public class RmiRemotingModule extends AbstractModule {

    private RmiRemotingInfrastructure infrastructure;

    private RmiRemoteClientFactory remoteClientFactory;

    @Configure(required = true)
    private Location location;

    @Expose
    public RemotingInfrastructure getInfrastructure() {
        if (infrastructure == null) {
            infrastructure = new RmiRemotingInfrastructure
                    (location == null ? DEFAULT_LOCATION : location);
        }
        return infrastructure;
    }

    @Expose
    public RemoteClientFactory getRemoteClientFactory() {
        if (remoteClientFactory == null) {
            remoteClientFactory = new RmiRemoteClientFactory();
        }
        return remoteClientFactory;
    }

    @Override
    public String toString() {
        return ToString.of(this, infrastructure, "@", location);
    }

    private static final Location DEFAULT_LOCATION = Location.parse("localhost:11199");
}
