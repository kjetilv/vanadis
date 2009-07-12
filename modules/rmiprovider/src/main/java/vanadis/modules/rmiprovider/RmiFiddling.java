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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vanadis.core.io.Location;
import vanadis.services.remoting.RemotingException;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

class RmiFiddling {

    private static final Logger log = LoggerFactory.getLogger(RmiFiddling.class);

    private static Registry REGISTRY;

    private static final Object lock = new Object();

    static Registry ensureLocalRegistry(Location location) {
        synchronized (lock) {
            if (REGISTRY != null) {
                return REGISTRY;
            }
            try {
                REGISTRY = LocateRegistry.createRegistry(location.getPort());
            } catch (RemoteException e) {
                throw new RemotingException("Failed to create new registry @ " + location, e);
            }
            if (REGISTRY != null) {
                return REGISTRY;
            }
            try {
                REGISTRY = LocateRegistry.getRegistry(location.getHost(), location.getPort());
                if (REGISTRY != null) {
                    return REGISTRY;
                }
            } catch (RemoteException e) {
                throw new RemotingException("Failed to locate registry @ " + location, e);
            }
        }
        throw new IllegalStateException("No registry could be found or created.");
    }

    static boolean rmiUnexport(Remote remote, boolean force) {
        try {
            return UnicastRemoteObject.unexportObject(remote, force);
        } catch (RemoteException e) {
            throw new RemotingException("Failed to export " + remote, e);
        }
    }

    static void rmiExport(Remote remote, Location location) {
        try {
            UnicastRemoteObject.exportObject(remote, location.getPort());
        } catch (RemoteException e) {
            throw new RemotingException("Failed to export " + remote + " @ " + location, e);
        }
    }

    static void rmiBind(String url, Remote remote) {
        try {
            Naming.bind(url, remote);
        } catch (RemoteException e) {
            throw new RemotingException("Failed to bind " + url + " -> " + remote, e);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Illegal url: " + url, e);
        } catch (AlreadyBoundException e) {
            throw new RemotingException("Alreay bound: " + url + ", cannot rebind " + remote, e);
        }
    }

    static void unbind(String url) {
        try {
            Naming.unbind(url);
        } catch (RemoteException e) {
            throw new RemotingException("Failed to unbind " + url, e);
        } catch (NotBoundException e) {
            if (log.isDebugEnabled()) {
                log.debug(url + " was not bound, probably already closed: " + e, e);
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Illegal url: " + url, e);
        }
    }

    static void verifyEmptyLocalRegistry() {
        synchronized (lock) {
            if (REGISTRY == null) {
                return;
            }
            String[] strings;
            try {
                strings = REGISTRY.list();
            } catch (RemoteException e) {
                throw new RemotingException(REGISTRY + " unavailable", e);
            }
            if (strings != null && strings.length > 0) {
                throw new RemotingException(REGISTRY + " still holds " + Arrays.toString(strings));
            }
        }
    }

    static Remote rmiLookup(String url) {
        try {
            return Naming.lookup(url);
        } catch (RemoteException e) {
            throw new RemotingException("Failed to lookup " + RmiRemotingInfrastructure.INVOKER, e);
        } catch (NotBoundException e) {
            throw new RemotingException("Could not find " + RmiRemotingInfrastructure.INVOKER, e);
        } catch (MalformedURLException e) {
            throw new RemotingException("Malformed url: " + url, e);
        }
    }
}
