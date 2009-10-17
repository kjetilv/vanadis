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

import vanadis.blueprints.*;
import vanadis.core.collections.Generic;
import vanadis.core.io.Location;
import vanadis.core.jmx.Jmx;
import vanadis.core.lang.ToString;
import vanadis.core.system.VM;
import vanadis.core.test.ForTestingPurposes;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LaunchSite {

    private final URI home;

    private final URI repo;

    private final SystemSpecification systemSpecification;

    private final Location location;

    private final OSGiLauncher launcher;

    private final Collection<String> blueprintNames;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final CloseHook closeHook;

    private PrintStream out;

    private final Collection<String> blueprintPaths;

    private final List<String> blueprintResources;

    private final List<BundleResolver> bundleResolvers;

    private static final List<String> BASE_COMMANDS = Arrays.asList("base-commands");

    @ForTestingPurposes
    public static LaunchSite repository(List<String> blueprintNames,
                                        List<String> blueprintPaths) {
        return new LaunchSite(null, null, null, null, blueprintNames, blueprintPaths, null, null, null, null, null);
    }

    @ForTestingPurposes
    public static LaunchSite repository(SystemSpecification systemSpecification) {
        return new LaunchSite(null, null, null, null, null, null, null, null, systemSpecification, null, null);
    }

    public static LaunchSite create(SiteSpecs ss) {
        return create(ss, null, null);
    }

    public static LaunchSite create(SiteSpecs ss, OSGiLauncher launcher, ResourceLoader resourceLoader) {
        return new LaunchSite(ss.getHome(), ss.getLocation(), ss.getRepoRoot(), ss.getUriPatterns(),
                              ss.getAdditionalBlueprintNames(), ss.getBlueprintPaths(), ss.getBlueprintResources(),
                              launcher == null ? ss.getLauncherClasses() : null, null, launcher, resourceLoader);
    }

    private LaunchSite(File home, Location location, URI repo, List<String> uriPatterns,
                       List<String> blueprintNames, List<String> blueprintPaths, List<String> blueprintResources,
                       List<String> launcherClasses, SystemSpecification systemSpecification,
                       OSGiLauncher launcher, ResourceLoader resourceLoader) {
        this.home = DirHelper.resolveHome(home);
        this.repo = DirHelper.resolveRepo(this.home, repo);
        this.bundleResolvers = compileBundleResolvers(uriPatterns);
        this.blueprintNames = systemSpecification == null || blueprintNames == null || blueprintNames.isEmpty()
                ? BASE_COMMANDS
                : Generic.seal(blueprintNames);
        this.location = LocationHelper.resolveLocation(location);
        this.launcher = launcher == null ? createLauncher(launcherClasses) : launcher;
        this.closeHook = new CloseHook(this);
        this.blueprintPaths = Generic.seal(blueprintPaths);
        this.blueprintResources = Generic.seal(blueprintResources);
        this.systemSpecification = systemSpecification == null
                ? readSystemSpecification(blueprintPaths, blueprintResources, resourceLoader)
                : systemSpecification;
    }

    public Collection<String> getBlueprintPaths() {
        return blueprintPaths;
    }

    public List<String> getBlueprintResources() {
        return blueprintResources;
    }

    public Collection<String> getBlueprintNames() {
        return blueprintNames;
    }

    public boolean launch(PrintStream out) {
        this.out = out;
        registerLauncherMBean();
        installShutdownHook();
        if (successfullyLaunched()) {
            printStartup();
            return true;
        }
        return false;
    }

    public OSGiLauncher getLauncher() {
        return launcher;
    }

    public Location getLocation() {
        return location;
    }

    public SystemSpecification getSystemSpecification() {
        return systemSpecification;
    }

    public URI getRepo() {
        return repo;
    }

    public URI getHome() {
        return home;
    }

    public String getProviderInfo() {
        return launcher.getProviderInfo();
    }

    public void close() {
        if (!closed.getAndSet(true)) {
            try {
                launcher.close(out);
            } finally {
                printShutdown();
            }
        }
    }

    public LaunchResult getLaunchResult() {
        return launcher.getLaunchResult();
    }

    private SystemSpecification readSystemSpecification(List<String> blueprintPaths,
                                                        List<String> blueprintResources,
                                                        ResourceLoader resourceLoader) {
        Blueprints blueprints = BlueprintsReader.read
                (resourceLoader == null ? new ClassLoaderResourceLoader(classLoader()) : resourceLoader,
                        blueprintPaths,
                        blueprintResources);
        return blueprints.getSystemSpecification(this.repo, this.blueprintNames);
    }

    private void printStartup() {
        String date = date();
        String info = launcher == null ? "<no launcher>" : launcher.getProviderInfo();
        StringBuilder applicationName = new StringBuilder();
        String unknown = "<unknown>";
        if (blueprintNames == null) {
            applicationName.append(unknown);
        } else {
            write(applicationName, ", ", blueprintNames);
        }
        StringBuilder blueprints = new StringBuilder();
        if (systemSpecification == null) {
            blueprints.append(unknown);
        } else {
            write(blueprints, VM.LN + "              ", systemSpecification.getSources());
        }
        p("vanadis @ " + locationString(),
          "  osgi: " + info,
          "   pid: " + VM.pid() + "@" + VM.LOCALHOST + " at " + date,
          "    vm: " + VM.VERSION + ", " + VM.OS + " " + VM.OS_VERSION,
          "",
          " application: " + applicationName,
          "  blueprints: " + blueprints,
          "        home: " + home,
          "        repo: " + repo,
          "         cwd: " + VM.CWD);
    }

    private void printShutdown() {
        p(date() + ": PID " + VM.pid() + " @ " + locationString() + " closed");
    }

    private String locationString() {
        return location == null ? "<none>" : String.valueOf(location.toInetSocketAddress());
    }

    private void registerLauncherMBean() {
        LaunchSiteMBean mbean = new LaunchSiteMBeanImpl(this);
        Location location = getLocation();
        ObjectName objectName = composeObjectName(location);
        try {
            Jmx.registerJmx(objectName, mbean, LaunchSiteMBean.class);
        } catch (Exception e) {
            if (out != null) {
                e.printStackTrace(out);
                out.println("Failed to register " + mbean + ", continuing...");
            }
        }
    }

    private boolean successfullyLaunched() {
        try {
            launcher.launch(home, location, bundleResolvers, systemSpecification);
            return true;
        } catch (StartupException e) {
            startupFailed(e, true);
        } catch (Throwable e) {
            startupFailed(e, true);
        }
        return false;
    }

    private void installShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(closeHook));
    }

    private void p(String... lines) {
        if (out != null) {
            for (String line : lines) {
                out.println(line);
            }
        }
    }

     private static final List<String> DEFAULT_LAUNCHERS = Arrays.asList
            ("vanadis.felix.FelixOSGiLauncher", "vanadis.equinox.EquinoxOSGiLauncher");

    private static void write(StringBuilder sb, String sep, Iterable<?> list) {
        for (Object object : list) {
            if (sb.length() > 0) {
                sb.append(sep);
            }
            sb.append(object);
        }
    }

    private static List<BundleResolver> compileBundleResolvers(List<String> uriPatterns) {
        if (uriPatterns == null || uriPatterns.isEmpty()) {
            return Collections.emptyList();
        }
        List<BundleResolver> resolvers = Generic.list(uriPatterns.size());
        for (String pattern : uriPatterns) {
            resolvers.add(new URIPatternResolver(pattern));
        }
        return resolvers;
    }

    private static ObjectName composeObjectName(Location location) {
        try {
            return new ObjectName(LaunchSite.class.getName() +
                    ":host=" + location.getHost() +
                    ",port=" + location.getPort() +
                    ",pid=" + VM.pid());
        } catch (MalformedObjectNameException e) {
            throw new StartupException("Unexpectedly invalid object name", e);
        }
    }

    private static String date() {
        try {
            return Calendar.getInstance().getTime().toString();
        } catch (Exception ignore) {
            return "<unknown>";
        }
    }

    private static OSGiLauncher createLauncher(List<String> launcherClasses) {
        return newLauncher(launcherClass(launcherClasses));
    }

    private static OSGiLauncher newLauncher(Class<?> launcherClass) {
        try {
            return (OSGiLauncher)launcherClass.newInstance();
        } catch (Exception e) {
            throw new StartupException("Could not instantiate launcher " + launcherClass, e);
        }
    }

    private static Class<?> launcherClass(List<String> classNames) {
        Exception exception = null;
        for (String className : (classNames == null || classNames.isEmpty()
                ? DEFAULT_LAUNCHERS
                : classNames)) {
            try {
                return Class.forName(className, true, classLoader());
            } catch (Exception e) {
                exception = exception == null ? e : exception;
            }
        }
        throw new StartupException("Could not load any of launcher classes: " + classNames, exception);
    }

    private static ClassLoader classLoader() {
        ClassLoader loader = LaunchSite.class.getClassLoader();
        return loader == null ? ClassLoader.getSystemClassLoader() : loader;
    }

    private static void startupFailed(Throwable throwable, boolean stackTrace) {
        System.err.println(throwable.getMessage());
        for (Throwable cause = throwable.getCause(); cause != null; cause = cause.getCause()) {
            System.err.println("         cause : " + cause);
        }
        if (stackTrace) {
            throwable.printStackTrace(System.err);
        }
    }

    @Override
    public String toString() {
        return ToString.of(this, launcher, "home", home, "loc", locationString());
    }
}
