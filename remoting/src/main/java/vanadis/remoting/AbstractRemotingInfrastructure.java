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

import vanadis.core.io.Location;
import vanadis.services.remoting.RemotingException;
import vanadis.services.remoting.RemotingInfrastructure;
import vanadis.util.log.Log;
import vanadis.util.log.Logs;

import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractRemotingInfrastructure implements RemotingInfrastructure {

    private static final Log log = Logs.get(AbstractRemotingInfrastructure.class);

    private boolean active;

    private final AtomicReference<Location> location = new AtomicReference<Location>();

    private final boolean endPoint;

    protected AbstractRemotingInfrastructure(Location location, boolean endPoint) {
        this.endPoint = endPoint;
        this.location.set(location);
    }

    @Override
    public boolean isServerActive() {
        return active;
    }

    @Override
    public Location getLocation() {
        if (!isEndPoint()) {
            return null;
        }
        while (true) { // Lock ma, nu looks!
            Location set = location.get();
            if (set != null) {
                return set;
            }
            Location update = subclassProvidedLocation();
            if (location.compareAndSet(null, update)) {
                return update;
            }
        }
    }

    Location subclassProvidedLocation() {
        throw new UnsupportedOperationException(getClass() + " should implement this method");
    }

    @Override
    public final void createServer(ClassLoader classLoader) {
        if (!isEndPoint()) {
            throw new IllegalStateException(this + " is not an endpoint.");
        }
        if (active) {
            throw new IllegalStateException
                    (this + " was asked to create server, but is already active");
        }
        setupServer(classLoader);
        active = true;
        log.info(this + " setup server");
    }

    protected abstract void setupServer(ClassLoader classLoader);

    @Override
    public final void destroyServer(boolean force) {
        if (active) {
            if (tearDownServer(force)) {
                log.info(this + " tore down server");
            } else {
                throw new RemotingException
                        (this + " failed to destroy server!");
            }
        }
    }

    @Override
    public final boolean isEndPoint() {
        return endPoint;
    }

    protected abstract boolean tearDownServer(boolean force);

}
