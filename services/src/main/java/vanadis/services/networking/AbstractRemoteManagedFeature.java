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
import vanadis.core.lang.EqHc;
import vanadis.core.system.VM;

abstract class AbstractRemoteManagedFeature<T extends AbstractRemoteManagedFeature<T>>
        implements RemoteManagedFeature<T> {

    private static final long serialVersionUID = 8111986093483748156L;

    private final Location remoteLocation;

    private final int pid;

    private final String serviceInterfaceName;

    AbstractRemoteManagedFeature(Location remoteLocation, String serviceInterfaceName) {
        this.serviceInterfaceName = serviceInterfaceName;
        this.remoteLocation = remoteLocation;
        this.pid = VM.pid();
    }

    @Override
    public Location getRemoteLocation() {
        return remoteLocation;
    }

    @Override
    public int getPid() {
        return pid;
    }

    @Override
    public String getServiceInterfaceName() {
        return serviceInterfaceName;
    }

    protected String coreString() {
        return serviceInterfaceName + "@" + remoteLocation;
    }

    protected abstract Object identitySource();

    @Override
    public final boolean equals(Object obj) {
        AbstractRemoteManagedFeature<T> feature = EqHc.retyped(this, obj);
        return feature != null && EqHc.eq
                (remoteLocation, feature.remoteLocation,
                 pid, feature.pid,
                 identitySource(), feature.identitySource());
    }

    @Override
    public int hashCode() {
        return EqHc.hc(getClass().getName(), remoteLocation, pid, identitySource());
    }
}
