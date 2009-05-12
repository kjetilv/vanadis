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
package net.sf.vanadis.modules.rmiprovider;

import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.io.Location;
import net.sf.vanadis.core.lang.Proxies;
import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.services.remoting.RemoteClientFactory;
import net.sf.vanadis.services.remoting.TargetHandle;

import java.util.Map;

class RmiRemoteClientFactory implements RemoteClientFactory {

    private final Map<Location, RemoteConnector> invokers = Generic.map();

    @Override
    public <T> T createClient(ClassLoader classLoader, TargetHandle<T> targetHandle) {
        RemoteInvoker remoteInvoker = getInvoker(targetHandle.getLocation());
        Class<T> targetInterface = targetHandle.getReference().getTargetInterface();
        Handler handler = new Handler(targetHandle, remoteInvoker);
        return Proxies.genericProxy(classLoader, targetInterface, handler);
    }

    private RemoteInvoker getInvoker(Location location) {
        RemoteConnector connector = invokers.get(location);
        if (connector != null) {
            return connector.getRemoteInvoker();
        }
        RemoteConnector newConnector = new RemoteConnector(location);
        invokers.put(location, newConnector);
        return newConnector.getRemoteInvoker();
    }

    @Override
    public void destroyClient(TargetHandle<?> targetHandle, Object client, boolean dispose) {
        if (dispose) {
            invokers.remove(targetHandle.getLocation());
        }
    }

    @Override
    public String toString() {
        return ToString.of(this, invokers.keySet());
    }
}