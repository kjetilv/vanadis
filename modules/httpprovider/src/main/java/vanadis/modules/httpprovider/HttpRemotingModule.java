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
package net.sf.vanadis.modules.httpprovider;

import net.sf.vanadis.core.io.Location;
import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.core.properties.PropertySet;
import net.sf.vanadis.core.time.TimeSpan;
import net.sf.vanadis.ext.AbstractModule;
import net.sf.vanadis.ext.Configure;
import net.sf.vanadis.ext.Expose;
import net.sf.vanadis.ext.Module;
import net.sf.vanadis.services.remoting.RemoteClientFactory;
import net.sf.vanadis.services.remoting.RemotingInfrastructure;

@SuppressWarnings({"UnusedDeclaration"})
@Module(moduleType = "httpprovider")
public class HttpRemotingModule extends AbstractModule {

    private static final String LOCATION_PROPERTY = "location";

    private HttpRemotingInfrastructure infrastructure;

    private HttpRemoteClientFactory remoteClientFactory;

    @Configure(def = "baseport + 1")
    private Location location;

    @Configure(def = "30s")
    private TimeSpan keepAlive;

    @Configure(def = "10")
    private int maxThreads;

    /**
     * If true, this node will not accept calls from the outside.
     */
    @Configure(def = "false")
    private boolean outgoingOnly;

    @Expose
    public RemoteClientFactory getRemoteClientFactory() {
        if (remoteClientFactory == null) {
            remoteClientFactory = new HttpRemoteClientFactory();
        }
        return remoteClientFactory;
    }

    @Expose
    public RemotingInfrastructure getInfrastructure(PropertySet propertySet) {
        if (infrastructure == null) {
            infrastructure = new HttpRemotingInfrastructure
                    (location, keepAlive, maxThreads, maxThreads, !outgoingOnly);
            propertySet.set(LOCATION_PROPERTY, infrastructure.getLocation());
        }
        return infrastructure;
    }

    @Override
    public String toString() {
        return ToString.of(this, infrastructure, remoteClientFactory);
    }
}
