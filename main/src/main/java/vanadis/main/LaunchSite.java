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

package vanadis.main;

import vanadis.blueprints.Blueprints;
import vanadis.blueprints.BlueprintsReader;
import vanadis.blueprints.SystemSpecification;
import vanadis.core.collections.Generic;
import vanadis.core.io.Location;
import vanadis.core.jmx.Jmx;
import vanadis.core.lang.ToString;
import vanadis.core.system.VM;
import vanadis.core.test.ForTestingPurposes;
import vanadis.launcher.LaunchResult;
import vanadis.launcher.OSGiLauncher;
import vanadis.launcher.StartupException;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LaunchSite {

    private final URI home;

    private final URI repo;

    private final SystemSpecification systemSpecification;

    private final Location location;

    private final OSGiLauncher launcher;

    private final List<String> blueprintNames;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final CloseHook closeHook;

    private PrintStream out;

    private final List<String> blueprintPaths;

    private final List<String> blueprintResources;

    @ForTestingPurposes
    public static LaunchSite repository(List<String> blueprintNames,
                                        List<String> blueprintPaths) {
        return create(null, null, null, blueprintNames, blueprintPaths, null);
    }

    @ForTestingPurposes
    public static LaunchSite repository(SystemSpecification systemSpecification) {
        return new LaunchSite(null, null, null, null, null, null, systemSpecification);
    }

    public static LaunchSite create(File home, Location location, URI repo,
                                    List<String> blueprintNames,
                                    List<String> blueprintPaths,
                                    List<String> blueprintResources) {
        return new LaunchSite(home, location, repo, blueprintNames, blueprintPaths, blueprintResources, null);
    }

    private LaunchSite(File home, Location location, URI repo,
                       List<String> blueprintNames,
                       List<String> blueprintPaths,
                       List<String> blueprintResources,
                       SystemSpecification systemSpecification) {
        this.home = DirHelper.resolveHome(home);
        this.repo = DirHelper.resolveRepo(this.home, repo);
        this.blueprintNames = blueprintNames(blueprintNames, systemSpecification);
        this.location = LocationHelper.resolveLocation(location);
        this.launcher = createLauncher();
        this.closeHook = new CloseHook(this);
        this.blueprintPaths = Generic.seal(blueprintPaths);
        this.blueprintResources = Generic.seal(blueprintResources);
        this.systemSpecification = systemSpecification == null
                ? readSystemSpecification(blueprintPaths, blueprintResources)
                : systemSpecification;
    }

    public List<String> getBlueprintPaths() {
        return blueprintPaths;
    }

    public List<String> getBlueprintResources() {
        return blueprintResources;
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

    public List<String> getBlueprintNames() {
        return Collections.unmodifiableList(blueprintNames);
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
                                                        List<String> blueprintResources) {
        Blueprints blueprints = BlueprintsReader.read(classLoader(), blueprintPaths, blueprintResources);
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
            launcher.launch(home, location, systemSpecification);
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

    private static final String LAUNCHER_PROPERTY = "vanadis.launcher";

    private static final String DEFAULT_LAUNCHERS =
            "vanadis.felix.FelixOSGiLauncher," +
                    "vanadis.equinox.EquinoxOSGiLauncher";

    private static void write(StringBuilder sb, String sep, Iterable<?> list) {
        for (Object object : list) {
            if (sb.length() > 0) {
                sb.append(sep);
            }
            sb.append(object);
        }
    }

    private static List<String> blueprintNames(List<String> blueprintNames,
                                               SystemSpecification systemSpecification) {
        if (systemSpecification == null) {
            return blueprintNames == null || blueprintNames.isEmpty()
                    ? Collections.singletonList("base-commands")
                    : Generic.seal(blueprintNames);
        }
        return Collections.emptyList();
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

    private static OSGiLauncher createLauncher() {
        String property = System.getProperty(LAUNCHER_PROPERTY, DEFAULT_LAUNCHERS);
        String[] classNames = property.split(",");
        Class<?> launcherClass = launcherClass(classNames);
        return newLauncher(launcherClass);
    }

    private static OSGiLauncher newLauncher(Class<?> launcherClass) {
        try {
            return (OSGiLauncher)launcherClass.newInstance();
        } catch (Exception e) {
            throw new StartupException("Could not instantiate launcher " + launcherClass, e);
        }
    }

    private static Class<?> launcherClass(String[] classNames) {
        Exception exception = null;
        for (String className : classNames) {
            try {
                return Class.forName(className, true, classLoader());
            } catch (Exception e) {
                exception = exception == null ? e : exception;
            }
        }
        throw new StartupException
                ("Could not load any of launcher classes: " + Arrays.toString(classNames), exception);
    }

    private static ClassLoader classLoader() {
        ClassLoader loader = LaunchSite.class.getClassLoader();
        return loader == null ? ClassLoader.getSystemClassLoader() : loader;
    }

    private static void startupFailed(Throwable throwable, boolean stackTrace) {
        System.err.println("STARTUP FAILED : " + throwable == null ? "<no exception>" : throwable.getMessage());
        if (throwable != null) {
            for (Throwable cause = throwable.getCause(); cause != null; cause = cause.getCause()) {
                System.err.println("         cause : " + cause);
            }
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
