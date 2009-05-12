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

import vanadis.core.collections.Generic;
import vanadis.core.collections.Pair;
import vanadis.core.io.Location;
import vanadis.core.properties.PropertySets;
import vanadis.core.time.TimeSpan;
import vanadis.ext.*;
import vanadis.osgi.Context;
import vanadis.osgi.Filters;
import vanadis.osgi.ServiceProperties;
import vanadis.osgi.impl.BareBonesContext;
import vanadis.remoting.RemotingImpl;
import vanadis.services.networking.Node;
import vanadis.services.networking.Router;
import vanadis.services.remoting.Remoting;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@SuppressWarnings({"UnusedDeclaration"})
public class NetworkTest {

    private static final String LOC1 = "foo:2000";

    private static final String LOC2 = "bar:2002";

    private static final String LOC3 = "zot:2004";

    private final Map<Location, Pair<Context, Node>> nodes = Generic.map();

    private ScheduledExecutorService service;

    private Router router;

    private static final String REMOTED1_NAME = "remotedloc1";

    @Before
    public void start() {
        service = Executors.newSingleThreadScheduledExecutor();
        nodes.clear();
        Remoting remoting = setupNode(LOC1);
        setupNode(LOC2);
        setupNode(LOC3);
        router = new RouterImpl(service, TimeSpan.HALF_MINUTE, remoting);
        for (Pair<Context, Node> nodePair : nodes.values()) {
            nodePair.getTwo().connect();
        }
    }

    @After
    public void stop() {
        service.shutdown();
        TimeSpan.HALF_MINUTE.awaitTermination(service, true);
        nodes.clear();
    }

    private Node node(String loc) {
        return nodes.get(Location.parse(loc)).getTwo();
    }

    private Context context(String loc) {
        return nodes.get(Location.parse(loc)).getOne();
    }

    private Remoting setupNode(String where) {
        BareBonesContext context = new BareBonesContext();
        Remoting remoting = new RemotingImpl
                (new FakeRemotingInfrastructure(Location.parse(where)),
                 new FakeRemoteClientFactory(context));
        nodes.put(Location.parse(where),
                  Pair.<Context, Node>of
                          (context, new NodeImpl(context, service, remoting, router, TimeSpan.HALF_MINUTE)));
        return remoting;
    }

    @Test
    @Ignore
    public void noRouter() {
        ServiceProperties<RemotedService> serviceProperties =
                ServiceProperties.create(RemotedService.class,
                                         PropertySets.create("snazzy", true));
        node(LOC1).registerRemoteInjectPoints(Collections.singletonList
                (new RemoteInjectPoint(RemotedService.class, REMOTED1_NAME, Filters.isTrue("snazzy"))));
        node(LOC2).registerRemoteReachableExposures(Collections.singletonList
                (new RemoteExposure(serviceProperties)));
    }

    @Module(moduleType = "remoted", launch = @AutoLaunch(name = "remotedloc1"))
    public static class RemotedServiceActivatorLoc1 {

    }

    @Module(moduleType = "remoted", launch = @AutoLaunch(name = "remotedloc2"))
    public static class RemotedServiceActivatorLoc2 {

    }

    @Module(moduleType = "remoted", launch = @AutoLaunch(name = "remotedloc3"))
    public static class RemotedServiceActivatorLoc3 {

    }

    public static class RemoteClient {

        @Inject
        private RemotedService remotedService;

        public RemotedService getRemotedService() {
            return remotedService;
        }
    }
}