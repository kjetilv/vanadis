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

import vanadis.common.io.Location;
import vanadis.common.io.Probe;
import vanadis.core.system.VM;

class LocationHelper {

    private static final int portStart = 10000;

    private static int portRange = portStart;

    private static final int portEnd = 64000;

    static Location verifiedBindable(Location location) {
        if (bindable(location)) {
            return location;
        }
        throw new StartupException("Invalid location: " + location);
    }

    static boolean bindable(Location location) {
        return !(Probe.detectedActivity(location));
    }

    static Location tmpLocation() {
        int attemptsLeft = 50;
        while (true) {
            try {
                Location location = new Location(VM.HOST, portRange);
                if (bindable(location)) {
                    return location;
                } else {
                    attemptsLeft--;
                    if (attemptsLeft == 0) {
                        throw new IllegalStateException("Unable to find a suitable location");
                    }
                }
            } finally {
                portRange += 960;
                if (portRange >= portEnd) {
                    portRange= portStart;
                }
            }
        }
    }

    static Location resolveLocation(Location location) {
        return location != null ? verifiedBindable(location)
                : tmpLocation();
    }
}
