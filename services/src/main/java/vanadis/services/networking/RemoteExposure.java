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
package vanadis.services.networking;

import vanadis.core.io.Location;
import vanadis.core.lang.ToString;
import vanadis.osgi.ServiceProperties;

public final class RemoteExposure extends AbstractRemoteManagedFeature<RemoteExposure> {

    private static final long serialVersionUID = -4641270866083392589L;

    private final ServiceProperties<?> serviceProperties;

    public RemoteExposure(ServiceProperties<?> serviceProperties) {
        this(null, serviceProperties);
    }

    private RemoteExposure(Location location, ServiceProperties<?> serviceProperties) {
        super(location, serviceProperties.getMainClassName());
        this.serviceProperties = serviceProperties;
    }

    public ServiceProperties<?> getServiceProperties() {
        return serviceProperties;
    }

    public RemoteExposure reinflate(ClassLoader classLoader) {
        return new RemoteExposure(getRemoteLocation(),
                                  getServiceProperties().reinflate(classLoader));
    }

    @Override
    protected ServiceProperties<?> identitySource() {
        return serviceProperties;
    }

    @Override
    public RemoteExposure copyRelocatedAt(Location location) {
        return new RemoteExposure(location, serviceProperties);
    }

    @Override
    public String toString() {
        return ToString.of(this, coreString(), "properties", serviceProperties, "@", getRemoteLocation());
    }
}