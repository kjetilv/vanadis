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

package vanadis.equinox;

import vanadis.launcher.AbstractOSGiLauncher;
import vanadis.launcher.ShutdownException;
import vanadis.launcher.StartupException;
import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

public class EquinoxOSGiLauncher extends AbstractOSGiLauncher {

    @Override
    protected BundleContext launchedBundleContext() {
        set("vanadis.home", getHome().toASCIIString());
        set("vanadis.location", getLocation().toLocationString());
        set("eclipse.ignoreApp", "true");
        set("eclipse.startTime", Long.toString(System.currentTimeMillis()));
        set("osgi.clean", "true");
        set("osgi.compatibility.bootdelegation", "true");
        set(Constants.FRAMEWORK_SYSTEMPACKAGES, systemPackages());
        set(Constants.FRAMEWORK_BOOTDELEGATION, bootDelegationPackages());
        set(EclipseStarter.PROP_CONSOLE, "");
        set(EclipseStarter.PROP_NOSHUTDOWN, "true");
        BundleContext bundleContext = newBundleContext();
        runIt();
        return bundleContext;
    }

    private void set(String key, String value) {
        System.setProperty(key, value);
    }

    private static void runIt() {
        try {
            EclipseStarter.run(null);
        } catch (Exception e) {
            throw new StartupException("Failed to run application", e);
        }
    }

    private static BundleContext newBundleContext() {
        try {
            return EclipseStarter.startup(new String[]{"-console"}, null);
        } catch (Exception e) {
            throw new StartupException("Failed to startup", e);
        }
    }

    @Override
    protected void stopFramework() {
        try {
            EclipseStarter.shutdown();
        } catch (Exception e) {
            throw new ShutdownException(this + " failed to shutdown", e);
        }
    }
}