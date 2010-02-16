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

import vanadis.common.io.Location;
import vanadis.core.lang.ToString;
import vanadis.osgi.Filter;

public final class RemoteInjectPoint extends AbstractRemoteManagedFeature<RemoteInjectPoint> {

    private static final long serialVersionUID = -5642290437074852883L;

    private final String objectManagerName;

    private final Filter filter;

    public RemoteInjectPoint(Class<?> serviceInterface,
                             String objectManagerName,
                             Filter filter) {
        this(null, serviceInterface.getName(), objectManagerName, filter);
    }

    private RemoteInjectPoint(Location location,
                              String serviceInterfaceName,
                              String objectManagerName,
                              Filter filter) {
        super(location, serviceInterfaceName);
        this.objectManagerName = objectManagerName;
        this.filter = filter;
    }

    public String getObjectManagerName() {
        return objectManagerName;
    }

    public boolean matches(RemoteExposure remoteExposure) {
        return filter.matches(remoteExposure.getServiceProperties());
    }

    @Override
    public Filter identitySource() {
        return filter;
    }

    @Override
    public RemoteInjectPoint copyRelocatedAt(Location location) {
        return new RemoteInjectPoint
                (location, getServiceInterfaceName(), objectManagerName, filter);
    }

    @Override
    public String toString() {
        return ToString.of(this, objectManagerName,
                           "serviceInterface", getServiceInterfaceName(),
                           "filter", filter,
                           "@", getRemoteLocation());
    }
}
