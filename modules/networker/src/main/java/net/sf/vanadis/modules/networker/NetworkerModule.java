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
package net.sf.vanadis.modules.networker;

import net.sf.vanadis.core.io.Location;
import net.sf.vanadis.core.lang.EntryPoint;
import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.core.time.TimeSpan;
import net.sf.vanadis.ext.*;
import net.sf.vanadis.osgi.ServiceProperties;
import net.sf.vanadis.services.networking.LocalNode;
import net.sf.vanadis.services.networking.Node;
import net.sf.vanadis.services.networking.RemoteNode;
import net.sf.vanadis.services.networking.Router;
import net.sf.vanadis.services.remoting.Remoting;
import net.sf.vanadis.util.concurrent.ExecutorUtils;
import net.sf.vanadis.util.log.Log;
import net.sf.vanadis.util.log.Logs;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

@Module(moduleType = "networker")
@EntryPoint
public class NetworkerModule extends AbstractContextAware {

    private static final Log log = Logs.get(NetworkerModule.class);

    @Configure(def = "false")
    @Attribute(desc = "If true, routes registrations to remote nodes")
    private boolean routing;

    @Configure(def = "30s")
    @Attribute(desc = "Time between retries")
    private TimeSpan retry;

    @Configure(def = "30s")
    @Attribute(desc = "At shutdown: Grace period before forcing shutdown")
    private TimeSpan shutdown;

    @Configure(def = "1")
    @Attribute(desc = "Number of threads routing")
    private int threads;

    @Inject
    private Remoting remoting;

    private RouterImpl router;

    @Attribute(desc = "Local node state")
    private final AtomicReference<Node> node = new AtomicReference<Node>();

    private ScheduledExecutorService service;

    private final NodeState nodeState = new NodeState();

    @Override
    public void configured() {
        service = threads > 1 ? Executors.newScheduledThreadPool(threads) :
                Executors.newSingleThreadScheduledExecutor();
    }

    private LocalNode connectedNode() {
        Node node = node();
        return node != null && node.isConnected() ? node : null;
    }

    private Node node() {
        return this.node.get();
    }

    @Configure(required = false)
    public void setRouterLocations(Location[] routerLocations) {
        nodeState.setRouterLocations(routerLocations);
    }

    /**
     * Listen for exposed objects that have {@link net.sf.vanadis.ext.Inject#remotable()
     * remotable} set to true.
     *
     * @param remoteReachable   Remote-exposed object
     * @param serviceProperties Properties
     */
    @Inject(required = false, properties = {
            @Property(name = CoreProperty.REMOTABLE_NAME, value = "true", propertyType = Boolean.class),
            @Property(name = CoreProperty.REMOTE_PROXY_NAME, negate = true)
    })
    public void addRemoteExposed(Object remoteReachable, ServiceProperties<?> serviceProperties) {
        RemoteExposure remoteExposure = nodeState.remoteExposed(remoteReachable, serviceProperties);
        LocalNode node = connectedNode();
        if (node != null) {
            node.registerRemoteReachableExposures(Collections.singletonList(remoteExposure));
        }
    }

    @Retract
    public void removeRemoteExposed(Object reachable) {
        RemoteExposure exposure = nodeState.unexposed(reachable);
        if (exposure != null) {
            LocalNode node = connectedNode();
            if (node != null) {
                node.unregisterRemoteReachableExposures(Collections.singletonList(exposure));
            }
        }
    }

    @Inject(required = false)
    public void addInjectPoint(RemoteInjectPoint remoteInjectPoint) {
        nodeState.injectPoint(remoteInjectPoint);
        LocalNode node = connectedNode();
        if (node != null) {
            node.registerRemoteInjectPoints(Collections.singletonList(remoteInjectPoint));
        }
    }

    @Retract
    public void removeInjectPoint(RemoteInjectPoint injectPoint) {
        nodeState.injectPointGone(injectPoint);
        LocalNode node = connectedNode();
        if (node != null) {
            node.unregisterRemoteInjectPoints(Collections.singletonList(injectPoint));
        }
    }

    @Override
    public void activate() {
        node().connect();
    }

    @Override
    public void closed() {
        List<Runnable> list = ExecutorUtils.terminate(this, service, shutdown);
        log.info(this + " closed, " + list.size() + " outstanding requests");
    }

    @Override
    public void dependenciesResolved() {
        this.router = routing
                ? new RouterImpl(service, retry, remoting)
                : null;
        NodeImpl node = new NodeImpl
                (requiredContext(), service, remoting, router, nodeState, retry);
        this.node.set(node);
        log.info(this + " set up node");
    }

    @Expose
    public RemoteNode getRemoteNode() {
        return node.get();
    }

    @Expose
    public LocalNode getLocalNode() {
        return node.get();
    }

    @Expose(optional = true)
    public Router getRouter() {
        return router;
    }

    @Override
    public String toString() {
        return ToString.of(this, "node", node, "router", router);
    }
}
