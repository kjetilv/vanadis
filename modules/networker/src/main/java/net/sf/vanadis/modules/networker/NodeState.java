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
package net.sf.vanadis.modules.networker;

import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.io.Location;
import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.ext.RemoteExposure;
import net.sf.vanadis.ext.RemoteInjectPoint;
import net.sf.vanadis.osgi.ServiceProperties;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

final class NodeState {

    private final Set<Location> routerLocations = Generic.set();

    private final Map<Object, RemoteExposure> remoteExposures = Generic.identityMap();

    private final Set<RemoteInjectPoint> remoteInjectPoints = Generic.set();

    public void setRouterLocations(Location[] routerLocations) {
        this.routerLocations.clear();
        this.routerLocations.addAll(Arrays.asList(routerLocations));
    }

    public RemoteExposure remoteExposed(Object remoteReachable, ServiceProperties<?> serviceProperties) {
        RemoteExposure remoteExposure = new RemoteExposure(serviceProperties);
        remoteExposures.put(remoteReachable, remoteExposure);
        return remoteExposure;
    }

    public RemoteExposure unexposed(Object reachable) {
        return remoteExposures.remove(reachable);
    }

    public void injectPoint(RemoteInjectPoint remoteInjectPoint) {
        remoteInjectPoints.add(remoteInjectPoint);
    }

    public void injectPointGone(RemoteInjectPoint injectPoint) {
        remoteInjectPoints.remove(injectPoint);
    }

    public Iterable<Location> locations() {
        return routerLocations;
    }

    public Iterable<RemoteExposure> remoteReachables() {
        return remoteExposures.values();
    }

    public Iterable<RemoteInjectPoint> remoteInjectPoints() {
        return remoteInjectPoints;
    }

    @Override
    public String toString() {
        return ToString.of(this,
                           "locations", routerLocations.size(),
                           "remoteInjectPoints", remoteInjectPoints.size(),
                           "remoteExposures", remoteExposures.size());
    }
}
