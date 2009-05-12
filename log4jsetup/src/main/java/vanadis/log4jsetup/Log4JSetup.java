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
package net.sf.vanadis.log4jsetup;

import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.io.Files;
import net.sf.vanadis.core.io.Location;
import org.apache.log4j.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.logging.Handler;

public final class Log4JSetup {

    private static final String LOG_PATH = "var/logs/vanadis-{0}-{1}.log";

    private static final String SYSTEM_OUT = "System.out";

    private static File logFile;

    private static ServiceRegistration logFileRegistration;

    private static final String VANADIS_LOGFILE = "vanadis.logfile";

    private static final Dictionary<String, Object> LOGFILE_PROPERTIES =
            Generic.dictionary(Generic.map(VANADIS_LOGFILE, true));

    /**
     * Duplicate of code in vanadis-utils:  This class should be loadable without dependencies!
     *
     * @param home Suggested home directory
     * @return Home directory
     */
    private static File resolveHome(String home) {
        if (home != null) {
            File file = Files.create(URI.create(home));
            if (file.exists() && file.isDirectory()) {
                return file;
            }
        }
        Object homeProperty = System.getProperty("vanadis.home");
        return homeProperty == null
                ? new File(String.valueOf(System.getProperty("user.dir")))
                : Files.createFromURI(homeProperty.toString());
    }

    public static void gone() {
        if (logFileRegistration != null) {
            logFileRegistration.unregister();
        }
    }

    public static void go(BundleContext bundleContext, String home, String location) {
        rerouteJavaUtilLogging();
        setupLog4J(bundleContext, home, location);
    }

    @SuppressWarnings({"EmptyCatchBlock"})
    private static String fetchPid() {
        String runtimeMBeanName = ManagementFactory.getRuntimeMXBean().getName();
        try {
            String[] strings = runtimeMBeanName.split("@");
            if (strings.length != 0) {
                return strings[0];
            }
        } catch (Throwable ignore) {
        }
        return String.valueOf(System.currentTimeMillis());
    }

    private static void setupLog4J(BundleContext bundleContext, String home, String location) {
        Logger logger = LogManager.getRootLogger();
        logger.setLevel(Level.DEBUG);
        LogManager.getLogger("net.sf.vanadis").setLevel(Level.ALL);
        LogManager.getLogger("org.jgroups").setLevel(Level.ERROR);
        try {
            File file = setupLogFile(bundleContext, home, location);
            Layout layout = new VanadisLayout();
            WriterAppender appender = file.getParentFile().exists() || file.getParentFile().mkdirs()
                    ? new RollingFileAppender(layout, file.getPath(), false)
                    : new ConsoleAppender(layout, SYSTEM_OUT);
            logger.addAppender(appender);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.err.println("Log setup failed, terminating!");
            System.exit(-1);
        }
    }

    private static File setupLogFile(BundleContext bundleContext, String home, String location) {
        if (logFile != null) {
            throw new IllegalStateException("log file should only be set up once!");
        }
        logFile = new File(resolveHome(home), MessageFormat.format(LOG_PATH, fetchPid(), parsePort(location)));
        logFileRegistration = bundleContext.registerService(File.class.getName(), logFile, LOGFILE_PROPERTIES);
        return logFile;
    }

    private static String parsePort(String location) {
        return String.valueOf(Location.parse(location).getPort());
    }

    private static void rerouteJavaUtilLogging() {
        java.util.logging.Logger rootLogger =
                java.util.logging.LogManager.getLogManager().getLogger("");
        if (rootLogger != null) {
            clearHandlers(rootLogger);
            rootLogger.addHandler(new ForwardingHandler());
        }
    }

    private static void clearHandlers(java.util.logging.Logger logger) {
        Handler[] handlers = logger.getHandlers();
        if (handlers != null) {
            for (Handler handler : Arrays.asList(handlers)) {
                logger.removeHandler(handler);
            }
        }
    }

    private Log4JSetup() {
        // Hee hee he
    }
}
