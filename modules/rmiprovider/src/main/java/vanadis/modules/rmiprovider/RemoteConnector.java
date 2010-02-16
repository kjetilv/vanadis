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

import vanadis.common.io.Location;
import vanadis.core.lang.EqHc;
import vanadis.core.lang.ToString;

class RemoteConnector {

    private final Location location;

    private final RemoteInvoker remoteInvoker;

    RemoteConnector(Location location) {
        this.location = location;
        this.remoteInvoker = (RemoteInvoker) RmiFiddling.rmiLookup
                (location.toRmiUrl(RmiRemotingInfrastructure.INVOKER));
    }

    RemoteInvoker getRemoteInvoker() {
        return remoteInvoker;
    }

    @Override
    public int hashCode() {
        return EqHc.hc(location);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object == null) {
            return false;
        }
        RemoteConnector connector = EqHc.retyped(this, object);
        return connector != null && EqHc.eq(location, connector.location);
    }

    @Override
    public String toString() {
        return ToString.of(this, location);
    }

}
