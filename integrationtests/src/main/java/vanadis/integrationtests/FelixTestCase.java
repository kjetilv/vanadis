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
package vanadis.integrationtests;

import org.junit.After;
import org.osgi.framework.Bundle;
import vanadis.blueprints.BundleSpecification;
import vanadis.blueprints.ModuleSpecification;
import vanadis.core.collections.Generic;
import vanadis.common.io.Location;
import vanadis.common.time.TimeSpan;
import vanadis.ext.CoreProperty;
import vanadis.objectmanagers.ManagedState;
import vanadis.objectmanagers.ObjectManager;
import vanadis.objectmanagers.ObjectManagerFactory;
import vanadis.osgi.Filter;
import vanadis.osgi.Reference;
import vanadis.osgi.Registration;

import java.util.Map;

public abstract class FelixTestCase {

    private static final TimeSpan RETRY_WAIT_TIME = TimeSpan.SECOND;

    private static final TimeSpan WAIT_DEADLINE = TimeSpan.MINUTE;

    @After
    public void clearRegistrations() {
        for (FelixTestSession felixTestSession : felixInstances.values()) {
            felixTestSession.close();
        }
        felixInstances.clear();
    }

    private final Map<Location, FelixTestSession> felixInstances = Generic.linkedHashMap();

    protected FelixTestSession newFelixSession(TimeSpan timeout) {
        return newFelixSession(timeout, true);
    }

    protected FelixTestSession newFelixSession(TimeSpan timeout, boolean full) {
        if (felixInstances.containsKey(null)) {
            throw new IllegalArgumentException
                    ("Felix already set up @ " + null + ": " + felixInstances.get(null));
        }
        FelixTestSession session = new FelixTestSession(timeout, full);
        felixInstances.put(session.getLaunch().getLocation(), session);
        System.out.println("Felix started @ " + null + ", home: " + null);
        return session;
    }

    protected FelixTestSession newFelixSession(Location location, TimeSpan timeout, boolean full) {
        FelixTestSession session = new FelixTestSession(timeout, full);
        felixInstances.put(location, session);
        System.out.println("Felix started @ " + session.getLaunch().getLocation() + ", home: " +
                session.getLaunch().getHome());
        return session;
    }

    protected void freeFelixSession(FelixTestSession session) {
        if (session != null) {
            session.close();
        }
    }

    protected static Registration<BundleSpecification> registerVBundle(FelixTestSession testSession,
                                                                       String groupAndArtifactPrefix,
                                                                       String artifactId) {
        return registerBundle(testSession, groupAndArtifactPrefix, groupAndArtifactPrefix + "." + artifactId);
    }

    protected static Registration<BundleSpecification> registerBundle(FelixTestSession testSession, String groupId, String artifactId) {
        return testSession.registerBundle(groupId, artifactId);
    }

    protected static Registration<ModuleSpecification> registerLaunch(FelixTestSession testSession, String type, String name) {
        return testSession.registerLaunch(type, name);
    }

    protected static Bundle waitForNonNullBundle(FelixTestSession testSession, String name, int... states) {
        return waitForBundle(testSession, name,
                             WAIT_DEADLINE, true, states);
    }

    protected static boolean waitForLostBundle(FelixTestSession testSession, String name) {
        return waitForLostBundle(testSession, name, WAIT_DEADLINE);
    }

    protected static Bundle waitForBundle(FelixTestSession testSession, String name, int... states) {
        return waitForBundle(testSession, name, WAIT_DEADLINE, false, states);
    }

    protected static Bundle waitForBundle(FelixTestSession testSession, String name, TimeSpan wait,
                                          boolean required,
                                          int... states) {
        return testSession.waitForBundle(name, wait, RETRY_WAIT_TIME, required, states);
    }

    protected static boolean waitForLostBundle(FelixTestSession testSession, String name, TimeSpan wait) {
        return testSession.waitForLostBundle(name, wait, RETRY_WAIT_TIME);
    }

    protected static <T> Reference<T> waitForNonNull(FelixTestSession testSession, Class<T> serviceInterface, Filter filter) {
        return waitFor(testSession, serviceInterface, filter,
                       WAIT_DEADLINE, true);
    }

    protected static <T> Reference<T> waitFor(FelixTestSession testSession, Class<T> serviceInterface, Filter filter) {
        return waitFor(testSession, serviceInterface, filter,
                       WAIT_DEADLINE, false);
    }

    protected static Reference<ObjectManagerFactory> waitForObjectManagerFactory(FelixTestSession testSession, String type) {
        return waitFor(testSession, ObjectManagerFactory.class, CoreProperty.OBJECTMANAGER_TYPE.filter(type),
                       WAIT_DEADLINE, false);
    }

    protected static void waitForLostObjectManagerFactory(FelixTestSession testSession, String type) {
        waitForLost(testSession, ObjectManagerFactory.class, CoreProperty.OBJECTMANAGER_TYPE.filter(type),
                    WAIT_DEADLINE);
    }

    protected static void waitForLostObjectManager(FelixTestSession testSession, String type) {
        waitForLost(testSession, ObjectManager.class, CoreProperty.OBJECTMANAGER_TYPE.filter(type),
                    WAIT_DEADLINE);
    }

    protected static Reference<ObjectManager> waitForObjectManager(FelixTestSession testSession, String type) {
        return waitForObjectManager(testSession, type, type);
    }

    protected static Reference<ObjectManager> waitForObjectManager(FelixTestSession testSession, String type, String name) {
        return testSession.waitForObjectManager(type, name);
    }

    private static <T> Reference<T> waitFor(FelixTestSession testSession,
                                            Class<T> serviceInterface, Filter filter,
                                            TimeSpan wait, boolean required) {
        return testSession.waitForReference(serviceInterface, filter, wait, RETRY_WAIT_TIME, required);
    }

    private static <T> void waitForLost(FelixTestSession testSession,
                                        Class<T> serviceInterface, Filter filter,
                                        TimeSpan wait) {
        testSession.waitForLostReference(serviceInterface, filter, wait, RETRY_WAIT_TIME);
    }

    protected static Bundle getBundle(FelixTestSession testSession, String name) {
        return testSession.getBundle(name);
    }

    protected static void assertObjectManagerState(FelixTestSession felixTestSession, String name, ManagedState state) {
        felixTestSession.waitForObjectManagerState(name, state);
    }
}
