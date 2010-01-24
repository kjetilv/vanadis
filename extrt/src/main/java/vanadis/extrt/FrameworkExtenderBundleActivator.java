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

package vanadis.extrt;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vanadis.core.collections.Generic;
import vanadis.core.lang.ToString;
import vanadis.core.lang.UsedByReflection;
import vanadis.core.time.TimeSpan;
import vanadis.ext.Command;
import vanadis.ext.GenericCommand;
import vanadis.osgi.Context;
import vanadis.osgi.Registration;

import java.util.List;

/**
 * Starts and stops a {@link FrameworkExtender}.  This is the
 * chief callback point from the OSGi runtime.  When
 * {@link #start(org.osgi.framework.BundleContext) the start method}
 * returns, vanadis is busy loading and registering bundles.
 */
@UsedByReflection
public final class FrameworkExtenderBundleActivator implements BundleActivator {

    private FrameworkExtender frameworkExtender;

    private BundleContext bundleContext;

    private final List<Registration<Command>> commands = Generic.list();

    @UsedByReflection
    public FrameworkExtenderBundleActivator() {
        log.info(this + " created");
    }

    @Override
    public void start(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        start();
    }

    @Override
    public void stop(BundleContext bundleContext) {
        unregisterCommands();
        stop(true);
    }

    void restart() {
        stop(false);
        start();
    }

    private void start() {
        TimeSpan timeout = timeout();
        frameworkExtender = new FrameworkExtender(this.bundleContext, timeout);
        registerCommands(frameworkExtender.getContext());
        log.info(this + " activated");
    }

    private void stop(boolean finalStop) {
        if (frameworkExtender != null) {
            frameworkExtender.close(finalStop);
        }
        frameworkExtender = null;
        log.info(this + " deactivated");
    }

    private void registerCommands(Context context) {
        reg(context, new GenericCommand("v-exit-system", "force System.exit()",
                                        context, new SystemExitExecution()));
        reg(context, new GenericCommand("v-restart-ext", "restart vanadis extender",
                                        context, new RestartExecution(this)));
    }

    private void unregisterCommands() {
        for (Registration<Command> command : commands) {
            command.unregisterSafely();
        }
    }

    private void reg(Context context, Command genericCommand) {
        commands.add(context.register(genericCommand, Command.class));
    }

    private static final Logger log = LoggerFactory.getLogger(FrameworkExtenderBundleActivator.class);

    private static final String TIMEOUT_SYSTEM_PROPERTY = "vanadis.timeout";

    private static final TimeSpan DEFAULT_TIMEOUT = TimeSpan.HALF_MINUTE;

    private static TimeSpan timeout() {
        String timeout = System.getProperty(TIMEOUT_SYSTEM_PROPERTY);
        if (timeout != null) {
            try {
                return TimeSpan.parse(timeout);
            } catch (Exception e) {
                log.warn("Failed to parse timeout in system property " + TIMEOUT_SYSTEM_PROPERTY +
                        ", using default timeout " + DEFAULT_TIMEOUT, e);
            }
        }
        return DEFAULT_TIMEOUT;
    }

    @Override
    public String toString() {
        return ToString.of(this, frameworkExtender);
    }
}
