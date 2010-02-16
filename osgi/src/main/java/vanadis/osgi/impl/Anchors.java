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

import vanadis.common.io.Files;
import vanadis.common.io.Location;
import vanadis.core.system.VM;
import org.osgi.framework.BundleContext;

import java.io.File;
import java.net.URI;
import java.text.MessageFormat;

class Anchors {

    static Location resolveLocation(BundleContext bundleContext, Location location) {
        if (location != null) {
            return location;
        } else {
            String locationProperty = bundleContext.getProperty("vanadis.location");
            return locationProperty == null
                ? new Location(8000)
                : Location.parse(locationProperty);
        }
    }

    static URI resolveHome(BundleContext bundleContext, URI homeURI) {
        File home = homeURI == null ? null : new File(homeURI);
        if (goodHome(home)) {
            return homeURI;
        } else {
            String homeProperty = bundleContext.getProperty("vanadis.home");
            File file = homeProperty == null ? VM.CWD
                    : Files.createFromURI(homeProperty);
            if (goodHome(file)) {
                return file.getAbsoluteFile().toURI();
            } else {
                throw new IllegalStateException
                    (MessageFormat.format
                        ("Unable to find a good anchorHome!  Parameter was {0}, property was {1}",
                         home, file));
            }
        }
    }

    private static boolean goodHome(File home) {
        return home != null && home.exists() && home.isDirectory();
    }

}
