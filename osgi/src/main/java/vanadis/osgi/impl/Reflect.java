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
package vanadis.osgi.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

final class Reflect {

    @SuppressWarnings({"unchecked"})
    static <T> Class<T> resolveServiceInterface(BundleContext bundleContext,
                                                String serviceInterfaceName,
                                                ServiceReference serviceReference) {
        ClassLoader loader = resolveLoader(bundleContext, serviceReference);
        return (Class<T>) (loader == null ? classpathLookup(serviceInterfaceName)
                : classloaderLookup(serviceInterfaceName, loader));
    }

    private static ClassLoader resolveLoader(BundleContext bundleContext, ServiceReference serviceReference) {
        return QuickServiceProbe.create(bundleContext, serviceReference).getLoader();
    }

    private static Class<?> classloaderLookup(String serviceInterfaceName, ClassLoader loader) {
        try {
            return Class.forName(serviceInterfaceName, true, loader);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException
                    ("Unable to load class " + serviceInterfaceName + " with " + loader, e);
        }
    }

    private static Class<?> classpathLookup(String serviceInterfaceName) {
        try {
            return Class.forName(serviceInterfaceName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException
                    ("Unable to load class " + serviceInterfaceName + " with root classloader", e);
        }
    }

    private Reflect() {
        // Don't make me
    }
}
