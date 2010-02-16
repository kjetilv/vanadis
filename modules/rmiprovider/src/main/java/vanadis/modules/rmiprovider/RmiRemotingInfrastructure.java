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
package vanadis.modules.rmiprovider;

import vanadis.common.io.Location;
import vanadis.core.lang.ToString;
import static vanadis.modules.rmiprovider.RmiFiddling.*;
import vanadis.remoting.AbstractRemotingInfrastructure;

class RmiRemotingInfrastructure extends AbstractRemotingInfrastructure {

    public static final String INVOKER = "__invoker";

    private RemoteInvoker localInvoker;

    private final String name;

    RmiRemotingInfrastructure() {
        this(null);
    }

    RmiRemotingInfrastructure(Location location) {
        super(location, location != null);
        this.name = location == null ? null : location.toRmiUrl(INVOKER);
    }

    @Override
    protected void setupServer(ClassLoader classLoader) {
        this.localInvoker = new RemoteInvokerImpl();
        ensureLocalRegistry(getLocation());
        rmiExport(localInvoker, getLocation().incrementPort());
        rmiBind(name, localInvoker);
    }

    @Override
    protected boolean tearDownServer(boolean force) {
        unbind(name);
        if (rmiUnexport(localInvoker, force)) {
            verifyEmptyLocalRegistry();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return ToString.of(this, localInvoker, "@", getLocation());
    }
}
