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
package vanadis.services.networking;

import vanadis.core.lang.EqHc;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

public final class RemoteConnection implements Serializable, Iterable<RemoteManagedFeature<?>> {

    private final RemoteInjectPoint remoteInjectPoint;

    private final RemoteExposure remoteExposure;

    private static final long serialVersionUID = 8478070589998171259L;

    public RemoteConnection(RemoteInjectPoint remoteInjectPoint,
                            RemoteExposure remoteExposure) {
        if (!remoteInjectPoint.matches(remoteExposure)) {
            throw new IllegalArgumentException
                    ("Invalid connection! " + remoteInjectPoint + " does not match " + remoteExposure);
        }
        this.remoteInjectPoint = Not.nil(remoteInjectPoint, "remote inject point");
        this.remoteExposure = Not.nil(remoteExposure, "remote exposure");
    }

    public RemoteInjectPoint getRemoteInjectPoint() {
        return remoteInjectPoint;
    }

    public RemoteExposure getRemoteExposure() {
        return remoteExposure;
    }

    @Override
    public boolean equals(Object obj) {
        RemoteConnection connection = EqHc.retyped(this, obj);
        return connection != null &&
                EqHc.eq(remoteExposure, connection.remoteExposure,
                        remoteInjectPoint, connection.remoteInjectPoint);
    }

    @Override
    public int hashCode() {
        return EqHc.hc(remoteExposure, remoteInjectPoint);
    }

    @Override
    public String toString() {
        return ToString.of(this, "inject", remoteInjectPoint, "expose", remoteExposure);
    }

    @Override
    public Iterator<RemoteManagedFeature<?>> iterator() {
        return Arrays.<RemoteManagedFeature<?>>asList(remoteInjectPoint, remoteExposure).iterator();
    }
}
