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

import junit.framework.Assert;
import static junit.framework.Assert.assertTrue;
import vanadis.blueprints.BundleSpecification;
import vanadis.blueprints.ModuleSpecification;
import vanadis.blueprints.SystemSpecification;
import vanadis.core.collections.Generic;
import vanadis.core.collections.Pair;
import vanadis.core.io.Files;
import vanadis.core.io.Location;
import vanadis.core.lang.EqHc;
import vanadis.core.lang.VarArgs;
import vanadis.core.properties.PropertySet;
import vanadis.core.properties.PropertySets;
import vanadis.core.system.VM;
import vanadis.core.time.Deadline;
import vanadis.core.time.TimeSpan;
import vanadis.core.ver.Version;
import vanadis.ext.CoreProperty;
import vanadis.ext.ManagedState;
import vanadis.ext.ObjectManager;
import vanadis.main.LaunchSite;
import vanadis.osgi.*;
import vanadis.services.networking.RemoteNode;
import vanadis.services.networking.Router;
import vanadis.util.mvn.Coordinate;
import vanadis.util.mvn.Repo;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public final class FelixTestSession {

    private final Collection<Registration<?>> regs = Generic.list();

    private final Context context;

    private final TimeSpan timeout;

    private final LaunchSite site;

    public static void fail(FelixTestSession felixTestSession, String msg, Throwable... es) {
        Reference<File> reference =
                felixTestSession.getContext().getReference(File.class, Filters.isTrue("vanadis.logfile"));
        if (reference != null) {
            try {
                File logFile = reference.getService();
                O.println(Files.toString
                        (logFile,
                         Pair.of("Errors     ", ERROR_PATTERN),
                         Pair.of("Warnings   ", WARN_PATTERN),
                         Pair.of("Exceptions ", EXCEPTION_PATTERN)));
            } finally {
                reference.unget();
            }
        }
        for (Throwable e : es) {
            e.printStackTrace(O);
        }
        Assert.fail(felixTestSession + " failed: " + msg);
    }

    FelixTestSession(TimeSpan timeout, boolean full) {
        this.timeout = timeout;
        SystemSpecification checklist = new SystemSpecification
                (null, "base", Repo.DEFAULT.toURI(), BASE_TEST_CONFIGURATION, null, null);
        site = LaunchSite.create(null, null, null, checklist);
        site.launch(System.out);
        assertTrue(site + " failed to launch", site.getLauncher().isLaunched());
        BundleContext bundleContext = site.getLauncher().getLaunchResult().getBundleContext();
        this.context = Contexts.create(bundleContext,
                                       site.getHome(),
                                       site.getLocation(),
                                       site.getSystemSpecification().getRepo());
        if (full) {
            registerVBundle("vanadis", "remoting");
            registerVBundle("vanadis.modules", "networker");
            registerVBundle("vanadis.modules", "remoting");
            registerVBundle("vanadis.modules", "httpprovider");
            registerBundle("org.apache.felix", "org.apache.felix.eventadmin", "1.0.0");
            waitForRequiredBundle("vanadis.modules.remoting");
            waitForRequiredBundle("vanadis.modules.httpprovider");
            waitForRequiredBundle("org.apache.felix.eventadmin");
        }
        waitForAllActive(timeout, TimeSpan.SECOND);
    }

    public LaunchSite getLaunch() {
        return site;
    }

    private void waitForRequiredBundle(String name) {
        waitForBundle(name, timeout, TimeSpan.SECOND, true, Bundle.ACTIVE);
    }

    public Context getContext() {
        return context;
    }

    public void close() {
        for (Registration<?> registration : regs) {
            registration.unregister();
        }
        regs.clear();
        site.close();
    }

    public void startNetworking(PropertySet properties) {
        registerLaunch("networker", properties);
        waitForObjectManager("networker");
        waitForObjectManagerState("networker", ManagedState.ACTIVE);
        waitForReference(RemoteNode.class, null, timeout, TimeSpan.SECOND, true);
        if (properties.is("routing", true)) {
            waitForReference(Router.class, null, timeout, TimeSpan.SECOND, true);
        }
    }

    public Location startRemoting(int threads) {
        registerLaunch("remoting");
        registerLaunch("httpprovider", PropertySets.create("maxThreads", threads));
        waitForObjectManager("remoting");
        waitForObjectManager("httpprovider");
        waitForObjectManagerState("httpprovider", ManagedState.ACTIVE);
        waitForObjectManagerState("remoting", ManagedState.ACTIVE);
        return getLaunch().getLocation().incrementPort(1);
    }

    public final Registration<BundleSpecification> registerVBundle(String groupAndArtifactPrefix, String artifactId) {
        return registerBundle(groupAndArtifactPrefix, groupAndArtifactPrefix + "." + artifactId, "1.1-SNAPSHOT");
    }

    public final Registration<BundleSpecification> registerBundle(String groupId, String artifactId) {
        return registerBundle(groupId, artifactId, "1.1-SNAPSHOT");
    }

    public final Registration<BundleSpecification> registerBundle(String groupId, String artifactId, String version) {
        URI uri = Coordinate.versioned(groupId, artifactId, new Version(version)).uriIn(Repo.DEFAULT);
        BundleSpecification bundleSpecification = BundleSpecification.create(uri, 1, null);
        Registration<BundleSpecification> registration = context.register(bundleSpecification);
        regs.add(registration);
        return registration;
    }

    public Registration<ModuleSpecification> registerLaunch(String type) {
        return registerLaunch(type, type, null);
    }

    public Registration<ModuleSpecification> registerLaunch(String type, PropertySet propertySet) {
        return registerLaunch(type, type, propertySet);
    }

    public Registration<ModuleSpecification> registerLaunch(String type, String name) {
        return registerLaunch(type, name, null);
    }

    public Registration<ModuleSpecification> registerLaunch(String type, String name, PropertySet propertySet) {
        action("Deploying " + name + ":" + type);
        Registration<ModuleSpecification> registration = context.register
                (ModuleSpecification.create(type, name, propertySet),
                 ServiceProperties.create(ModuleSpecification.class, propertySet));
        regs.add(registration);
        return registration;
    }

    public void waitForObjectManagerState(String name, ManagedState expectedState) {
        Reference<ObjectManager> reference =
                getContext().getReference(ObjectManager.class, CoreProperty.OBJECTMANAGER_NAME.filter(name));
        Assert.assertNotNull
                (this + " found no object manager '" + name + "' in " + getContext(), reference);
        Object objectManager = reference.getRawService();
        try {
            identify("Waiting for " + name + " -> " + expectedState);
            startWaiting();
            Deadline deadline = timeout.newDeadline();
            ExpectedStatePoller expectedStatePoller =
                    new ExpectedStatePoller(this, objectManager, expectedState);
            TimeSpan nterval = TimeSpan.SECOND;
            Object polledState = runDeadline(deadline, nterval, expectedStatePoller);
            if (polledState == null) {
                O.println(" nope!");
            }
            Assert.assertNotNull(this + " expected state " + expectedState + ", only got " +
                    ExpectedStatePoller.stateOf(objectManager) + " in " + objectManager, polledState);
            O.println(" ok!");
        } finally {
            reference.unget();
        }
    }

    private static <T> T runDeadline(Deadline deadline, TimeSpan interval, Callable<T> expectedStatePoller) {
        return deadline.tryEvery(interval.dividedBy(10), 1.25, interval, expectedStatePoller);
    }

    public <T> void waitForLostReference(Class<T> serviceInterface, Filter filter,
                                         TimeSpan timeout, TimeSpan retryTime) {
        identify("Waiting for loss of reference of type " + serviceInterface.getName());
        startWaiting("  Filter: " + filter);
        Reference<T> nullExpected = timeout.newDeadline().tryForNullEvery
                (retryTime, new ReferenceLookup<T>(getContext(), serviceInterface, filter, true));
        if (nullExpected == null) {
            return;
        }
        fail(this, "Reference after " + timeout.toHumanFriendlyString() + ", " + nullExpected +
                " found for type '" + serviceInterface + "', " + "filter '" + filter);
    }

    public <T> Reference<T> waitForReference(Class<T> serviceInterface, Filter filter,
                                             TimeSpan timeout, TimeSpan retryTime, boolean required) {
        identify("Waiting for reference of type " + serviceInterface.getName());
        startWaiting("  Filter: " + filter);
        Reference<T> reference = runDeadline
                (timeout.newDeadline(), retryTime,
                 new ReferenceLookup<T>(getContext(), serviceInterface, filter, false));
        if (reference == null && required) {
            fail(this, "No reference found for type '" + serviceInterface + "'/filter '" + filter + "', timeout " +
                    timeout.toHumanFriendlyString());
        }
        return reference;
    }

    Reference<ObjectManager> waitForObjectManager(String type) {
        return waitForObjectManager(type, type);
    }

    public Reference<ObjectManager> waitForObjectManager(String type, String name) {
        return waitForReference(ObjectManager.class,
                                CoreProperty.OBJECTMANAGER_TYPE.filter(type).and
                                        (CoreProperty.OBJECTMANAGER_NAME.filter(name)),
                                timeout, TimeSpan.SECOND, false);
    }

    public Bundle getBundle(String name) {
        Bundle[] bundles = site.getLauncher().getLaunchResult().getAllBundles();
        for (Bundle bundle : bundles) {
            if (bundle.getSymbolicName().equals(name)) {
                return bundle;
            }
        }
        return null;
    }

    public final void waitForAllActive(TimeSpan timeout, TimeSpan retryInteval) {
        Deadline deadline = new Deadline(timeout);
        Integer bCount = runDeadline(deadline, retryInteval, new ActiveBundlePollCounter());
        Assert.assertNotNull(bCount);
        O.println(this + ": " + bCount + " bundles ACTIVE");
        Integer activeCount = runDeadline(deadline, retryInteval, new ActiveObjectManagerPollCounter());
        Assert.assertNotNull(activeCount);
        if (activeCount > 0) {
            O.println(this + ": " + activeCount + " object managers ACTIVE");
        }
    }

    public void waitForAllInactive(TimeSpan timeout, TimeSpan retryInteval) {
        Deadline deadline = new Deadline(timeout);
        Integer bCount = runDeadline(deadline, retryInteval, new ActiveBundlePollCounter());
        Assert.assertNotNull(bCount);
        status(bCount + " bundles ACTIVE");
        O.println(this + ": " + bCount + " bundles ACTIVE");
        Integer activeCount = runDeadline(deadline, retryInteval, new ActiveObjectManagerPollCounter());
        Assert.assertNotNull(activeCount);
        if (activeCount > 0) {
            O.println(this + ": " + activeCount + " object managers ACTIVE");
        }
    }

    public boolean waitForLostBundle(final String name, TimeSpan wait, TimeSpan retryWaitTime) {
        Deadline deadline = wait.newDeadline();
        identify("Waiting for bundle to disappear: " + name);
        startWaiting();
        Boolean ok = runDeadline(deadline, retryWaitTime, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                O.print(".");
                Bundle bundle = getBundle(name);
                if (bundle == null) {
                    O.println(" ok!");
                    return true;
                }
                return null;
            }
        });
        return ok != null && ok;
    }

    public Bundle waitForBundle(final String name, TimeSpan wait, TimeSpan retryWaitTime, boolean required,
                                final int... states) {
        Deadline deadline = wait.newDeadline();
        identify("Waiting for bundle " + name);
        startWaiting();
        final AtomicInteger lastState = new AtomicInteger();
        Bundle bundle = runDeadline(deadline, retryWaitTime, new Callable<Bundle>() {
            @Override
            public Bundle call() {
                O.print(".");
                Bundle bundle = getBundle(name);
                if (bundle != null) {
                    if (inState(bundle, states)) {
                        O.println(" ok!");
                        return bundle;
                    }
                    lastState.set(bundle.getState());
                }
                return null;
            }
        });
        if (bundle == null && required) {
            fail(this, "Bundle " + name + " last seen in state " + lastState +
                    ", did not show up in state(s) " + Arrays.toString(states));
        }
        return bundle;
    }

    public Collection<Reference<ObjectManager>> getObjectManagers() {
        return getContext().getReferences(ObjectManager.class, null);
    }

    public void stop() {
        site.close();
    }

    private void status(String status) {
        O.println("Status: " + this + ": " + status);
    }

    private void action(String action) {
        O.println("Action: " + this +
                VM.LN + " " + action);
    }

    private void identify(String whatsUp) {
        O.println("Meanwhile, back in " + this + ":" +
                VM.LN + " " + whatsUp + " ...");
    }

    private Object expectedState(Object service, ManagedState expectedState) throws Exception {
        return new ExpectedStatePoller(this, service, expectedState).call();
    }

    private static final PrintStream O = System.out;

    private static final Pattern ERROR_PATTERN = Pattern.compile("ERROR ");

    private static final Pattern WARN_PATTERN = Pattern.compile("WARN  ");

    private static final Pattern EXCEPTION_PATTERN = Pattern.compile("\\[exception");

    private static final Collection<BundleSpecification> BASE_TEST_CONFIGURATION = Arrays.asList
            (id("org.apache.felix:org.osgi.compendium:1.2.0"),
             id("org.apache.felix:org.apache.felix.shell:1.2.0"),
             id("org.objectweb.asm:com.springsource.org.objectweb.asm:3.1.0"),
             id("com.sun.grizzly:grizzly-utils:1.9.14"),
             id("com.sun.grizzly:grizzly-portunif:1.9.14"),
             id("com.sun.grizzly:grizzly-rcm:1.9.14"),
             id("com.sun.grizzly:grizzly-framework:1.9.14"),
             id("com.sun.grizzly:grizzly-http:1.9.14"),
             id("org.apache.commons:com.springsource.org.apache.commons.logging:1.1.1"),
             ver("vanadis.modules", "log", "1.1-SNAPSHOT"),
             ver("vanadis", "log4jsetup", "1.1-SNAPSHOT"),
             ver("vanadis", "deployer", "1.1-SNAPSHOT"),
             ver("vanadis", "osgi", "1.1-SNAPSHOT"),
             ver("vanadis", "blueprints", "1.1-SNAPSHOT"),
             ver("vanadis", "services", "1.1-SNAPSHOT"),
             ver("vanadis", "annopro", "1.1-SNAPSHOT"),
             ver("vanadis", "ext", "1.1-SNAPSHOT"),
             ver("vanadis", "extrt", "1.1-SNAPSHOT"));

    private static BundleSpecification id(String id) {
        return BundleSpecification.create(Coordinate.at(id), 1, null);
    }

    private static BundleSpecification ver(String groupAndArtifactPrefix, String artifact, String ver) {
        return BundleSpecification.create
                (Coordinate.versioned
                        (groupAndArtifactPrefix, groupAndArtifactPrefix + "." + artifact, new Version(ver)), 1, null);
    }

    private static void startWaiting(String... preambles) {
        for (String preamble : preambles) {
            O.println(preamble);
        }
        O.print("  ...");
    }

    private static boolean inState(Bundle bundle, int... states) {
        if (VarArgs.notPresent(states)) {
            return true;
        }
        for (int state : states) {
            if (bundle.getState() == state) {
                return true;
            }
        }
        return false;
    }

    private class ActiveObjectManagerPollCounter implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            Collection<Reference<ObjectManager>> references = getObjectManagers();
            for (Reference<ObjectManager> reference : references) {
                Object service = reference.getRawService();
                try {
                    Object failedState = expectedState(service, ManagedState.FAILED);
                    if (failedState != null) {
                        FelixTestSession.fail(FelixTestSession.this, service + " has failed");
                    }
                    Object activeState = expectedState(service, ManagedState.ACTIVE);
                    if (activeState == null) {
                        O.println("Not active: " + reference);
                        return null;
                    }
                } finally {
                    reference.unget();
                }
            }
            return references.size();
        }
    }

    private class ActiveBundlePollCounter implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            Bundle[] bundles = site.getLauncher().getLaunchResult().getAllBundles();
            for (Bundle bundle : bundles) {
                if (bundle.getState() != Bundle.ACTIVE) {
                    System.err.println("Not active: " + bundle);
                    return null;
                }
            }
            return bundles.length;
        }
    }

    @Override
    public int hashCode() {
        return EqHc.hc(getLaunch().getLocation());
    }

    @Override
    public boolean equals(Object obj) {
        FelixTestSession testSession = EqHc.retyped(this, obj);
        return testSession.getLaunch().getLocation().equals(getLaunch().getLocation());
    }

    @Override
    public String toString() {
        return "[Felix:" + getLaunch().getHome() + "@" + getLaunch().getLocation() + "]";
    }
}
