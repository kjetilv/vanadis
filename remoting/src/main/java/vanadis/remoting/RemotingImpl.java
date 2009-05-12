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
package vanadis.remoting;

import vanadis.core.collections.Generic;
import vanadis.core.io.Location;
import vanadis.core.lang.ToString;
import vanadis.services.remoting.RemoteClientFactory;
import vanadis.services.remoting.Remoting;
import vanadis.services.remoting.RemotingInfrastructure;
import vanadis.services.remoting.TargetHandle;

import java.util.Map;

public class RemotingImpl implements Remoting {

    private static <T> ClassLoader classLoaderOf(TargetHandle<T> targetHandle) {
        return targetHandle.getReference().getTargetInterface().getClassLoader();
    }

    private final Map<TargetHandle<?>, Object> clients = Generic.synchLinkedMap();

    private final RemotingInfrastructure infrastructure;

    private final RemoteClientFactory clientFactory;

    private final Object lock = new Object();

    public RemotingImpl(RemotingInfrastructure infrastructure,
                        RemoteClientFactory clientFactory) {
        this(infrastructure, clientFactory, null);
    }

    public RemotingImpl(RemotingInfrastructure infrastructure,
                        RemoteClientFactory clientFactory,
                        ClassLoader classLoader) {
        this.infrastructure = infrastructure;
        this.clientFactory = clientFactory;
        boolean endPoint = this.infrastructure != null && this.infrastructure.isEndPoint();
        if (endPoint && !this.infrastructure.isServerActive()) {
            this.infrastructure.createServer(classLoader);
        }
    }

    @Override
    public <T> T connect(TargetHandle<T> targetHandle) {
        return connect(classLoaderOf(targetHandle), targetHandle, false);
    }

    @Override
    public <T> T connect(TargetHandle<T> targetHandle, boolean newSession) {
        return connect(classLoaderOf(targetHandle), targetHandle, newSession);
    }

    @Override
    public <T> T connect(ClassLoader classLoader, TargetHandle<T> targetHandle) {
        return connect(classLoader, targetHandle, false);
    }

    @Override
    public <T> T connect(ClassLoader classLoader, TargetHandle<T> targetHandle, boolean newSession) {
        if (clientFactory == null) {
            throw new IllegalStateException(this + " does not have a client factory, cannot connect " + targetHandle);
        }
        Class<T> type = targetHandle.getReference().getTargetInterface();
        synchronized (lock) {
            if (clients.containsKey(targetHandle)) {
                T client = type.cast(clients.get(targetHandle));
                if (newSession) {
                    clientFactory.destroyClient(targetHandle, client, false);
                } else {
                    return client;
                }
            }
            T client = clientFactory.createClient(classLoader, targetHandle);
            clients.put(targetHandle, client);
            return client;
        }
    }

    @Override
    public boolean isEndPoint() {
        return infrastructure != null && infrastructure.isEndPoint();
    }

    @Override
    public Location getEndPoint() {
        return infrastructure == null ? null : infrastructure.getLocation();
    }

    @Override
    public boolean isActive() {
        return isEndPoint() && infrastructure.isServerActive();
    }

    @Override
    public RemotingImpl addLocator(Object locator) {
        if (isEndPoint()) {
            Accessor.getSingleton().registerAccessPoint(locator);
            return this;
        } else {
            throw new IllegalStateException(this + " is not an endpoint, cannot add locator " + locator);
        }
    }

    @Override
    public void close() {
        synchronized (lock) {
            if (isActive()) {
                infrastructure.destroyServer(true);
            }
        }
    }

    @Override
    public String toString() {
        return ToString.of(this, clients);
    }

}
