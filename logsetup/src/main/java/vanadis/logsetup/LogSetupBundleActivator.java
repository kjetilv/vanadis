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
package vanadis.logsetup;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import vanadis.core.lang.UsedByReflection;
import vanadis.core.system.VM;

@UsedByReflection
public class LogSetupBundleActivator implements BundleActivator {

    @Override
    public void start(BundleContext bundleContext) {
        LogSetup.go(bundleContext,
                getProperty(bundleContext, "vanadis.home", VM.HOME.getAbsolutePath()),
                getProperty(bundleContext, "vanadis.location", "localhost:16000"));
    }

    private String getProperty(BundleContext bundleContext, String property, String def) {
        String value = bundleContext.getProperty(property);
        return value == null ? System.getProperty(property, def) : value;
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LogSetup.gone();
    }
}
