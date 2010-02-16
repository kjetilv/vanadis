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

package vanadis.launcher;

import org.osgi.framework.*;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vanadis.blueprints.BundleResolver;
import vanadis.blueprints.BundleSpecification;
import vanadis.blueprints.SystemSpecification;
import vanadis.core.collections.Generic;
import vanadis.common.io.Location;
import vanadis.core.lang.EntryPoint;
import vanadis.core.lang.Not;
import vanadis.core.lang.VarArgs;
import vanadis.common.time.TimeSpan;

import java.io.PrintStream;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@EntryPoint("Subclasses discovered at runtime")
public abstract class AbstractOSGiLauncher implements OSGiLauncher {

    private final AtomicBoolean launched = new AtomicBoolean(false);

    private final AtomicBoolean failedLaunch = new AtomicBoolean(false);

    private final AtomicBoolean closed = new AtomicBoolean(false);

    private URI home;

    private Location location;

    private List<BundleResolver> bundleResolvers;

    private SystemSpecification systemSpecification;

    private final List<ServiceRegistration> bundleRegistrations = Generic.list();

    private final List<ServiceRegistration> moduleRegistrations = Generic.list();

    private LaunchResult launchResult;

    private BundleContext bundleContext;

    private long bootstrapId;

    private Dictionary<?, ?> bundleHeaders;

    @Override
    public final LaunchResult getLaunchResult() {
        requireLaunched();
        return launchResult;
    }

    @Override
    public final void close(PrintStream stream) {
        if (!closed.getAndSet(true)) {
            unregisterAll(stream);
            stopFramework();
        }
    }

    @Override
    public final String getProviderInfo() {
        requireLaunched();
        return bundleHeaders.get(Constants.BUNDLE_SYMBOLICNAME) + " " + bundleHeaders.get(Constants.BUNDLE_VERSION);
    }

    @SuppressWarnings({"ThrowCaughtLocally"})
    @Override
    public final LaunchResult launch(URI home, Location location,
                                     List<BundleResolver> bundleResolvers,
                                     SystemSpecification systemSpecification) {
        setLaunchState(home, location, bundleResolvers, systemSpecification);

        try {
            assertPrelaunchState(this.bundleContext == null);

            BundleContext bundleContext = launchedBundleContext();
            if (bundleContext == null) {
                throw new IllegalStateException(this + " produced null bundle context!");
            }

            List<Bundle> bundles = startAutoBundles(bundleContext);
            setLaunchResultState(bundleContext, bundles);

            registerBundles();
            registerModules();

            return launchResult;
        } catch (RuntimeException e) {
            failedLaunch.set(true);
            throw e;
        }
    }

    private void setLaunchResultState(BundleContext bundleContext, List<Bundle> bundles) {
        this.launchResult = new LaunchResult(bundleContext, bundles);
        this.bundleContext = launchResult.getBundleContext();
        Bundle bundle = this.bundleContext.getBundle();
        this.bootstrapId = bundle.getBundleId();
        this.bundleHeaders = bundle.getHeaders();
    }

    @Override
    public final boolean isLaunched() {
        return launched.get() && !failedLaunch.get();
    }

    @Override
    public final URI getHome() {
        requireLaunched();
        return home;
    }

    @Override
    public final URI getRepo() {
        return systemSpecification.getRepo();
    }

    @Override
    public final Location getLocation() {
        requireLaunched();
        return location;
    }

    private void assertPrelaunchState(boolean check) {
        assert check : this + " was launched twice!";
    }

    private void requireLaunched() {
        if (!launched.get()) {
            throw new IllegalStateException(this + " not launched");
        }
        if (failedLaunch.get()) {
            throw new IllegalStateException(this + " failed launch, should be discarded");
        }
    }

    private List<Bundle> startAutoBundles(BundleContext bundleContext) {
        List<Bundle> bundles = Generic.list();
        for (BundleSpecification specification : getSystemSpecification().getAutoBundles(bundleResolvers)) {
            bundles.add(install(bundleContext, specification));
        }
        for (Bundle bundle : bundles) {
            startBundle(bundleContext, bundle, bundles);
        }
        resolve(bundleContext);
        return bundles;
    }

    private static void resolve(BundleContext bundleContext) {
        ServiceReference ref = bundleContext.getServiceReference(PackageAdmin.class.getName());
        try {
            if (ref != null) {
                PackageAdmin admin = (PackageAdmin) bundleContext.getService(ref);
                admin.resolveBundles(null);
            }
        } finally {
            bundleContext.ungetService(ref);
        }
    }

    private static void startBundle(BundleContext bundleContext, Bundle bundle, List<Bundle> bundles) {
        if (isNonFragment(bundle)) {
            try {
                bundle.start();
            } catch (BundleException e) {
                throw new StartupException
                        (bundleContext + " failed to start bundle " + bundle + ", " +
                                bundles.size() + " was started: " + bundles, e);
            }
        }
    }

    protected abstract BundleContext launchedBundleContext();

    protected abstract void stopFramework();

    protected abstract String osgiExports();

    protected final SystemSpecification getSystemSpecification() {
        return systemSpecification;
    }

    protected Map<String, Object> getStandardPackagesConfiguration() {
        Map<String, Object> configuration = Generic.map();
        configuration.put(Constants.FRAMEWORK_SYSTEMPACKAGES, systemPackages());
        configuration.put(Constants.FRAMEWORK_BOOTDELEGATION, bootDelegationPackages());
        return configuration;
    }

    private String systemPackages() {
        return new StringBuilder
                (SystemPackages.JDK).append(",").append
                (osgiExports()).append(",").append
                (SystemPackages.UTIL).toString();
    }

    private static String bootDelegationPackages() {
        String[] bootDelegationPackages =
                new String[]{SystemPackages.PROFILING, SystemPackages.COVERAGE};
        if (VarArgs.present(bootDelegationPackages)) {
            StringBuilder sb = new StringBuilder(bootDelegationPackages[0]);
            for (int i = 1; i < bootDelegationPackages.length; i++) {
                sb.append(",").append(bootDelegationPackages[i]);
            }
            return sb.toString();
        }
        return null;
    }

    protected final void setLaunchState(URI home, Location location,
                                        List<BundleResolver> bundleResolvers,
                                        SystemSpecification systemSpecification) {
        if (launched.getAndSet(true)) {
            throw new IllegalArgumentException(this + " already launched");
        }
        this.home = setOnceTo(this.home, home, "home");
        this.location = setOnceTo(this.location, location, "location");
        this.bundleResolvers = setOnceTo(this.bundleResolvers, bundleResolvers, "bundle resolvers");
        this.systemSpecification = setOnceTo(this.systemSpecification, systemSpecification, "system specification");
    }

    private <T> T setOnceTo(T current, T value, String desc) {
        assert current == null : this + " was launched twice, " + desc + " was set to " + current;
        return Not.nil(value, desc);
    }

    private void registerBundles() {
        registerAll(bundleRegistrations, systemSpecification.getDynaBundles());
    }

    private void registerModules() {
        registerAll(moduleRegistrations, systemSpecification.moduleSpecifications());
    }

    private void registerAll(List<ServiceRegistration> registrationList, Collection<?> objects) {
        requireLaunched();
        for (Object object : objects) {
            registrationList.add(registrationOf(object));
        }
    }

    private void unregisterAll(PrintStream stream) {
        if (launchResult == null) {
            return;
        }
        print(stream, "[");
        unregister(moduleRegistrations);
        defaultWait(new ReferenceVanishWaiter("vanadis.ext.ObjectManager", null, 5, stream, launchResult),
                    "ObjectManagers still present, continuing...");
        unregister(bundleRegistrations);
        defaultWait(new ReferenceVanishWaiter("vanadis.ext.ObjectManagerFactory", null, 5, stream, launchResult),
                    "ObjectManagers still present, continuing...");
        print(stream, "|");
        uninstallAll(launchResult.getNonCoreBundles(), stream);
        print(stream, "|");
        uninstallAll(launchResult.getAutoBundles(true), stream);
        print(stream, "]");
    }

    private ServiceRegistration registrationOf(Object object) {
        return bundleContext.registerService(object.getClass().getName(), object, null);
    }

    private void uninstallAll(List<Bundle> bundles, PrintStream stream) {
        for (Bundle bundle : bundles) {
            long bundleId = bundle.getBundleId();
            if (bundleId > 0 && bundleId != bootstrapId) {
                try {
                    shutdown(bundle);
                    print(stream, ".");
                } catch (Throwable e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Bundle " + bundleId + " failed to stop: " + bundle, e);
                    }
                    print(stream, "x");
                }
            }
        }
    }

    private static final Logger log = LoggerFactory.getLogger(AbstractOSGiLauncher.class);

    private static void shutdown(Bundle bundle) throws BundleException {
        String bundleString = null;
        try {
            bundleString = bundle.toString() + " (" + bundle.getSymbolicName() + ")";
        } catch (Exception e) {
            log.warn("Failed to get bundle data", e);
        }
        try {
            int state = bundle.getState();
            if (state == Bundle.ACTIVE || state == Bundle.STARTING || state == Bundle.STOPPING) {
                bundle.stop();
            }
        } finally {
            try {
                bundle.uninstall();
            } catch (Throwable e) {
                log.warn("Bundle " + bundleString + " failed to uninstall", e);
            }
        }
        log.info("Shutdown: " + bundleString);
    }

    private static boolean isNonFragment(Bundle bundle) {
        return bundle.getHeaders().get(Constants.FRAGMENT_HOST) == null;
    }

    private static Bundle install(BundleContext bundleContext, BundleSpecification specification) {
        try {
            return bundleContext.installBundle(specification.getUrlString());
        } catch (BundleException e) {
            throw new StartupException("Failed to install start bundle at " + specification, e);
        }
    }

    private static void print(PrintStream stream, Object object) {
        if (stream != null) {
            stream.print(object);
        }
    }

    private static void defaultWait(Waiter waiter, String warning) {
        if (!wait(waiter, TimeSpan.MINUTE, TimeSpan.millis(250))) {
            log.warn(warning);
        }
    }

    private static void unregister(List<ServiceRegistration> regs) {
        List<ServiceRegistration> reversed = Generic.list(regs);
        Collections.reverse(reversed);
        for (ServiceRegistration registration : reversed) {
            try {
                registration.unregister();
            } catch (Exception e) {
                log.warn("Failed to unregister " + registration, e);
            }
        }
    }

    private static Boolean wait(Waiter waiter, TimeSpan timeout, TimeSpan retry) {
        return timeout.newDeadline().tryEvery(retry, waiter);
    }

}
