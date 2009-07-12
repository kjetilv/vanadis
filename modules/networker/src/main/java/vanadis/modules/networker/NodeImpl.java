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
import vanadis.core.properties.PropertySet;
import vanadis.core.properties.PropertySets;
import vanadis.core.time.TimeSpan;
import vanadis.ext.Attribute;
import vanadis.ext.CoreProperty;
import static vanadis.ext.CoreProperty.OBJECTMANAGER_NAME;
import vanadis.objectmanagers.ObjectManager;
import vanadis.osgi.*;
import vanadis.remoting.ServiceFilterTargetReference;
import vanadis.services.networking.*;
import vanadis.services.remoting.Remoting;
import vanadis.services.remoting.TargetHandle;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

final class NodeImpl extends AbstractNetworker implements Node {

    private static final Logger log = LoggerFactory.getLogger(NodeImpl.class);

    private final Map<Location, RouterRetrier> routers = Generic.map();

    private final Map<RemoteExposure, Registration<?>> proxies = Generic.map();

    private final Context context;

    private final Remoting remoting;

    private final NodeState nodeState;

    @Attribute
    private final AtomicBoolean connected = new AtomicBoolean();

    private final Lock connectingLock = new ReentrantLock(true);

    private static final PropertySet REMOTE_PROXY_PROPERTY_SET =
            PropertySets.create(CoreProperty.REMOTE_PROXY.name(), true);

    NodeImpl(Context context, ScheduledExecutorService service,
             Remoting remoting,
             Router localRouter,
             TimeSpan retry) {
        this(context, service, remoting, localRouter, new NodeState(), retry);
    }

    NodeImpl(Context context, ScheduledExecutorService service,
             Remoting remoting,
             Router localRouter,
             NodeState nodeState,
             TimeSpan retry) {
        super(retry, service);
        this.context = context;
        this.remoting = remoting;
        this.nodeState = nodeState;
        if (localRouter != null) {
            storeRouter(getLocation(), localRouter);
        }
    }

    @Attribute
    public int getRoutersCount() {
        return routers.size();
    }

    @Attribute
    public Location getLocation() {
        return remoting.getEndPoint();
    }

    private Collection<RouterRetrier> routers() {
        return routers.values();
    }

    @Override
    public void disconnect(RemoteConnection remoteConnection) {
        RemoteExposure exposure = remoteConnection.getRemoteExposure();
        Registration<?> registration = proxies.remove(exposure);
        if (registration == null) {
            log.warn(this + " was asked to disconnect unknown connection " + remoteConnection);
        } else {
            registration.unregister();
        }
    }

    @Override
    public boolean isConnected() {
        return connected.get();
    }

    @Override
    public void registerRemoteInjectPoints(List<RemoteInjectPoint> injectors) {
        runInjectionRegistrations(injectors);
    }

    @Override
    public void unregisterRemoteInjectPoints(List<RemoteInjectPoint> injectPoints) {
        List<RemoteInjectPoint> anchoredInjectPoints = anchoredInjectPoints(injectPoints);
        for (Router router : routers()) {
            router.unregisterRemoteInjectPoints(anchoredInjectPoints);
        }
    }

    @Override
    public void registerRemoteReachableExposures(List<RemoteExposure> exposures) {
        runExposureRegistrations(exposures);
    }

    @Override
    public void unregisterRemoteReachableExposures(List<RemoteExposure> exposures) {
        List<RemoteExposure> anchoredExposures = anchoredExposures(exposures);
        for (Router router : routers()) {
            router.unregisterRemoteExposures(anchoredExposures);
        }
    }

    @Override
    public void connect() {
        connectingLock.lock();
        try {
            if (!connected.get()) {
                for (Location location : nodeState.locations()) {
                    storeRouter(location, connectToRouter(location));
                }
                runExposureRegistrations(Generic.list(nodeState.remoteReachables()));
                runInjectionRegistrations(Generic.list(nodeState.remoteInjectPoints()));
                connected.set(true);
            }
        } finally {
            connectingLock.unlock();
        }
    }

    private void storeRouter(Location location, Router router) {
        routers.put(location, new RouterRetrier(getRetry(), getService(), router));
    }

    private Router connectToRouter(Location location) {
        ServiceFilterTargetReference<Router> reference =
                new ServiceFilterTargetReference<Router>(Router.class);
        TargetHandle<Router> handle = new TargetHandle<Router>(location, reference);
        return remoting.connect(getClass().getClassLoader(), handle);
    }

    @Override
    public void connect(RemoteConnection remoteConnection) {
        log.info("Last leg: " + this + " connects " + remoteConnection);
        Reference<ObjectManager> managerReference = localInjectee(remoteConnection);
        Registration<?> registration = createProxy(remoteConnection, managerReference);
        storeLocalProxy(remoteConnection, registration);
    }

    private void storeLocalProxy(RemoteConnection remoteConnection,
                                 Registration<?> registration) {
        proxies.put(remoteConnection.getRemoteExposure(), registration);
    }

    private Registration<?> createProxy(RemoteConnection remoteConnection, Reference<ObjectManager> managerReference) {
        ObjectManager manager = managerReference.getService();
        try {
            return registerLocalProxyFor(remoteConnection.getRemoteExposure(), manager);
        } finally {
            managerReference.unget();
        }
    }

    private Reference<ObjectManager> localInjectee(RemoteConnection remoteConnection) {
        return context.getSingleReference(ObjectManager.class,
                                          objectManagerNameFilter(remoteConnection.getRemoteInjectPoint()));
    }

    private Registration<?> registerLocalProxyFor(RemoteExposure matchingRemoteExposure, ObjectManager manager) {
        ServiceProperties<?> serviceProperties = reinflatedServiceProperties(matchingRemoteExposure, manager);
        Object proxy = createConnectedProxy
                (matchingRemoteExposure, serviceProperties, manager.getManagedObjectClassLoader());
        Class<?> serviceInterface = serviceInterface
                (matchingRemoteExposure, manager.getManagedObjectClassLoader());
        return register(serviceProperties, proxy, serviceInterface);
    }

    private static ServiceProperties<?> reinflatedServiceProperties(RemoteExposure matchingRemoteExposure,
                                                                    ObjectManager manager) {
        ClassLoader classLoader = manager.getManagedObjectClassLoader();
        RemoteExposure anchoredExposure = matchingRemoteExposure.reinflate(classLoader);
        return anchoredExposure.getServiceProperties();
    }

    private void runInjectionRegistrations(Iterable<RemoteInjectPoint> injectors) {
        List<RemoteInjectPoint> injectPoints = anchoredInjectPoints(injectors);
        for (Router router : routers()) {
            router.registerRemoteInjectPoints(injectPoints);
        }
    }

    private void runExposureRegistrations(Iterable<RemoteExposure> exposures) {
        List<RemoteExposure> anchoredExposures = anchoredExposures(exposures);
        for (Router router : routers()) {
            router.registerRemoteExposures(anchoredExposures);
        }
    }

    private <T> Registration<T> register(ServiceProperties<?> serviceProperties, Object proxy, Class<T> serviceInterface) {
        ServiceProperties<T> proxyServiceProperties =
                serviceProperties.with(REMOTE_PROXY_PROPERTY_SET).retypedTo(serviceInterface);
        return context.register(serviceInterface.cast(proxy), proxyServiceProperties);
    }

    private <T> T createConnectedProxy(RemoteExposure remoteExposure,
                                       ServiceProperties<T> serviceProperties,
                                       ClassLoader classLoader) {
        ServiceFilterTargetReference<T> targetReference = new ServiceFilterTargetReference<T>
                (serviceProperties.getMainClass(), serviceProperties.toFilter());
        return remoting.connect
                (classLoader, new TargetHandle<T>(remoteExposure.getRemoteLocation(), targetReference));
    }

    private Class<?> serviceInterface(RemoteExposure remoteReference, ClassLoader classLoader) {
        String serviceInterfaceName = remoteReference.getServiceInterfaceName();
        try {
            return Class.forName(serviceInterfaceName, false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException
                    (this + " could not set up proxy for " + remoteReference, e);
        }
    }

    private static Filter objectManagerNameFilter(RemoteInjectPoint originInjectPoint) {
        return OBJECTMANAGER_NAME.filter(originInjectPoint.getObjectManagerName());
    }

    private List<RemoteInjectPoint> anchoredInjectPoints(Iterable<RemoteInjectPoint> injectors) {
        return anchored(injectors, Generic.<RemoteInjectPoint>list());
    }

    private List<RemoteExposure> anchoredExposures(Iterable<RemoteExposure> exposures) {
        return anchored(exposures, Generic.<RemoteExposure>list());
    }

    private <T extends RemoteManagedFeature<T>> List<T> anchored(Iterable<T> remoteManagedFeatures, List<T> anchored) {
        for (T remoteManagedFeature : remoteManagedFeatures) {
            T copy = remoteManagedFeature.copyRelocatedAt(getLocation());
            anchored.add(copy);
        }
        return anchored;
    }

    @Override
    public String toString() {
        return ToString.of(this, "routers", routers, "nodeState", nodeState);
    }
}
