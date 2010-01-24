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
package vanadis.modules.networker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vanadis.core.collections.Generic;
import vanadis.core.io.Location;
import vanadis.core.lang.ToString;
import vanadis.core.time.TimeSpan;
import vanadis.ext.Attr;
import vanadis.remoting.ServiceFilterTargetReference;
import vanadis.services.networking.*;
import vanadis.services.remoting.Remoting;
import vanadis.services.remoting.TargetHandle;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

class RouterImpl extends AbstractNetworker implements Router {

    private static final Logger log = LoggerFactory.getLogger(RouterImpl.class);

    private final Map<Location, RemoteNodeRetrier> nodes = Generic.map();

    private final Set<RemoteExposure> exposures = Generic.synchSet();

    private final Set<RemoteInjectPoint> injectPoints = Generic.synchSet();

    private final Remoting remoting;

    RouterImpl(ScheduledExecutorService service, TimeSpan retry, Remoting remoting) {
        super(retry, service);
        this.remoting = remoting;
    }

    @Attr
    public int getExposureCount() {
        return exposures.size();
    }

    @Attr
    public int getInjectPointCount() {
        return injectPoints.size();
    }

    @Attr
    public int getNodeCount() {
        return nodes.size();
    }

    @Override
    public void registerRemoteInjectPoints(List<RemoteInjectPoint> injectPoints) {
        for (RemoteInjectPoint injectPoint : injectPoints) {
            registerRemoteInjectPoint(injectPoint);
        }
    }

    @Override
    public void registerRemoteExposures(List<RemoteExposure> exposures) {
        for (RemoteExposure exposure : exposures) {
            registerRemoteExposure(exposure);
        }
    }

    @Override
    public void unregisterRemoteInjectPoints(List<RemoteInjectPoint> injectPoints) {
        for (RemoteInjectPoint injectPoint : injectPoints) {
            unregisterRemoteInjectPoint(injectPoint);
        }
    }

    @Override
    public void unregisterRemoteExposures(List<RemoteExposure> exposures) {
        for (RemoteExposure remoteExposure : exposures) {
            unregisterRemoteExposure(remoteExposure);
        }
    }

    private void registerRemoteInjectPoint(RemoteInjectPoint injectPoint) {
        injectPoints.add(injectPoint);
        for (RemoteExposure remoteExposure : exposures) {
            considerConnect(injectPoint, remoteExposure);
        }
    }

    private void considerConnect(RemoteInjectPoint injectPoint, RemoteExposure remoteExposure) {
        if (injectPoint.matches(remoteExposure)) {
            connect(new RemoteConnection(injectPoint, remoteExposure));
        }
    }

    private void registerRemoteExposure(RemoteExposure exposure) {
        exposures.add(exposure);
        for (RemoteInjectPoint remoteInjectPoint : injectPoints) {
            considerConnect(remoteInjectPoint, exposure);
        }
    }

    private void unregisterRemoteInjectPoint(RemoteInjectPoint remoteInjectPoint) {
        injectPoints.remove(remoteInjectPoint);
        for (RemoteExposure remoteExposure : exposures) {
            considerDisconnect(remoteExposure, remoteInjectPoint);
        }
    }

    private void unregisterRemoteExposure(RemoteExposure remoteExposure) {
        exposures.remove(remoteExposure);
        for (RemoteInjectPoint remoteInjectPoint : injectPoints) {
            considerDisconnect(remoteExposure, remoteInjectPoint);
        }
    }

    private void considerDisconnect(RemoteExposure remoteExposure, RemoteInjectPoint remoteInjectPoint) {
        if (remoteInjectPoint.matches(remoteExposure)) {
            disconnect(new RemoteConnection(remoteInjectPoint, remoteExposure));
        }
    }

    private void connect(RemoteConnection remoteConnection) {
        harvestNodeReferences(remoteConnection);
        node(remoteConnection.getRemoteInjectPoint()).connect(remoteConnection);
        log.info(this + " established " + remoteConnection);
    }

    private void disconnect(RemoteConnection remoteConnection) {
        RemoteNode remoteNode = node(remoteConnection.getRemoteInjectPoint());
        remoteNode.disconnect(remoteConnection);
        log.info(this + " removed " + remoteConnection);
    }

    private void harvestNodeReferences(Iterable<RemoteManagedFeature<?>> features) {
        for (RemoteManagedFeature<?> feature : features) {
            Location hostingNodeLocation = feature.getRemoteLocation();
            if (isNew(hostingNodeLocation)) {
                establishConnectionToNodeAt(hostingNodeLocation);
            }
        }
    }

    private RemoteNode node(RemoteManagedFeature<?> managedFeature) {
        return nodes.get(managedFeature.getRemoteLocation());
    }

    private boolean isNew(Location location) {
        return !nodes.containsKey(location);
    }

    private void establishConnectionToNodeAt(Location location) {
        TargetHandle<RemoteNode> handle = remoteNodeHandle(location);
        RemoteNode remoteNode = remoting.connect(getClass().getClassLoader(), handle);
        nodes.put(location, new RemoteNodeRetrier(getRetry(), getService(), remoteNode));
        log.info(this + " connected to node " + remoteNode + " @ " + location);
    }

    private static TargetHandle<RemoteNode> remoteNodeHandle(Location location) {
        ServiceFilterTargetReference<RemoteNode> reference =
                new ServiceFilterTargetReference<RemoteNode>(RemoteNode.class);
        return new TargetHandle<RemoteNode>(location, reference);
    }

    @Override
    public String toString() {
        String remotingText =
                remoting == null ? "<no remoting>"
                        : "remoting " + (remoting.isActive() ? "" : "in") + "active@" + remoting.getEndPoint();
        return ToString.of(this, remotingText,
                           "nodes", nodes.keySet().size(),
                           "injectPoints", injectPoints.size(),
                           "exposure", exposures.size());
    }
}
