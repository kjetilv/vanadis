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
package net.sf.vanadis.deployer;

import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.core.lang.VarArgs;
import net.sf.vanadis.core.time.Time;
import net.sf.vanadis.core.time.TimeSpan;
import net.sf.vanadis.osgi.Registration;
import net.sf.vanadis.util.log.Log;
import net.sf.vanadis.util.log.Logs;
import org.osgi.framework.ServiceRegistration;

import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

final class ActiveDeployer implements Runnable {

    private final Lock lock = new ReentrantLock();

    private final Condition cycleCondition = lock.newCondition();

    private final Deploy deploy;

    private final TimeSpan pause;

    private final List<UriExplorer> explorers;

    private final AtomicReference<Time> nextTime = new AtomicReference<Time>();

    private final Map<Object, Registration<?>> registrations = Generic.linkedHashMap();

    private boolean stopped;

    ActiveDeployer(Deploy deploy, TimeSpan pause, UriExplorer... explorers) {
        this.deploy = deploy;
        this.pause = pause;
        this.explorers = VarArgs.present(explorers) ? Arrays.asList(explorers)
                : Collections.<UriExplorer>emptyList();
    }

    public void stop() {
        try {
            lock.lock();
            stopped = true;
            cycleCondition.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void run() {
        if (pause == null) {
            throw new IllegalStateException(this + " has no pause parameter, cannot run");
        }
        try {
            lock.lock();
            while (!stopped) {
                runCycle();
                boolean interrupted = pause();
                if (interrupted) {
                    log.error(this + " interrupted, stopping thread...");
                    return;
                }
            }
            close();
        } finally {
            lock.unlock();
        }
    }

    private void close() {
        for (Registration<?> registration : registrations.values()) {
            try {
                registration.unregister();
            } catch (IllegalStateException ignore) {
                // Felix shutdown issues
            }
        }
    }

    public void runCycle() {
        Collection<Throwable> exceptions = Generic.set();
        for (UriExplorer explorer : explorers) {
            flashChanges(explorer, exceptions);
        }
        if (!exceptions.isEmpty()) {
            log.error(exceptions.size() + " exceptions encountered!");
        }
    }

    private void flashChanges(UriExplorer explorer, Collection<Throwable> exceptions) {
        if (explorer != null) {
            DiscoveredUris discoveredUris = explorer.discover();
            for (URI uri : discoveredUris.updatesAndRemoves()) {
                unregisterUri(explorer, exceptions, uri);
            }
            for (URI uri : discoveredUris.newsAndUpdates()) {
                registerUri(explorer, uri, exceptions);
            }
        }
    }

    public ServiceRegistration undeploy(Object uri) {
        Registration<?> registration = registrations.remove(uri);
        if (registration != null) {
            registration.unregister();
        }
        return (ServiceRegistration) registration;
    }

    private void unregisterUri(UriExplorer explorer, Collection<Throwable> exceptions, Object object) {
        try {
            ServiceRegistration registration = undeploy(object);
            log.info(this + ": " + explorer + " unregistered URI " + object + " from " + registration);
        } catch (Exception e) {
            exceptions.add(e);
            log.error(this + " failed to unregister " + object, e);
        }
    }

    private void registerUri(UriExplorer explorer, URI uri, Collection<Throwable> exceptions) {
        Registration<?> registration = null;
        try {
            String type = explorer.getType();
            if (type.equalsIgnoreCase(BUNDLE)) {
                registration = deploy.deployBundle(uri);
            } else if (type.equalsIgnoreCase(SERVICE)) {
                registration = deploy.deployModule(uri);
            }
        } catch (Exception e) {
            exceptions.add(e);
            return;
        }
        if (registration != null) {
            registrations.put(uri, registration);
            log.info(this + ": " + explorer + " registered URL " + uri +
                    " on " + registration);
        }
    }

    void triggerCycle() {
        lock.lock();
        try {
            cycleCondition.signal();
        } finally {
            lock.unlock();
        }
    }

    private boolean pause() {
        nextTime.set(Time.mark().after(pause));
        return !pause.waitFor(cycleCondition, false);
    }

    private static final Log log = Logs.get(ActiveDeployer.class);

    private static final String BUNDLE = "bundle";

    private static final String SERVICE = "service";

    @Override
    public String toString() {
        String nextTime;
        try {
            TimeSpan untilThen = TimeSpan.until(this.nextTime.get());
            nextTime = untilThen.approximate().toHumanFriendlyString();
        } catch (Exception e) {
            String oops = "Failed resolve next time";
            if (log.isDebug()) {
                log.debug(oops, e);
            }
            nextTime = oops;
        }
        return ToString.of(this, "next", nextTime);
    }
}
