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

import vanadis.common.io.Location;
import vanadis.core.lang.Not;
import vanadis.core.properties.PropertySet;

import java.util.regex.Pattern;

class PortUtils {

    static void writeToSystemProperties(PropertySet propertySet, Location location, PropertySet... subst) {
        for (String property : propertySet) {
            String rawValue = propertySet.getString(property, subst);
            String value = location == null ? rawValue
                    : resolveIntegerValue(property, propertySet, location, subst);
            System.setProperty(property, value);
        }
    }

    static Location resolveLocation(Location base, String value) {
        Not.nil(base, "base location");
        if (value == null) {
            return null;
        }
        String str = value.trim().toLowerCase();
        if (DELTA_PATTERN.matcher(str).matches()) {
            String suffix = value.substring(str.lastIndexOf("+") + 1).trim();
            int delta = parsed(value, suffix);
            return base.incrementPort(delta);
        }
        if (Location.isLocation(str)) {
            return Location.parse(str);
        }
        if (PORT_PATTERN.matcher(str).matches()) {
            return new Location(base.getHost(), toInt(str));
        }
        throw new IllegalArgumentException("Invalid location specification: " + value);
    }

    private static int toInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Illegal port: " + str, e);
        }
    }

    private static final String PORTS = "\\d{1,5}";

    private static final Pattern DELTA_PATTERN = Pattern.compile("baseport\\s*[+]\\s*" + PORTS);

    private static final Pattern PORT_PATTERN = Pattern.compile(PORTS);

    private static int parsed(String value, String suffix) {
        try {
            return Integer.parseInt(suffix);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException
                    ("Invalid baseport" + (value.equals(suffix) ? "" : " suffix") + ": " + value, e);
        }
    }

    private static String resolveIntegerValue(String property, PropertySet propertySet, Location location,
                                              PropertySet... subst) {
        String value = propertySet.getString(property, subst);
        return DELTA_PATTERN.matcher(value).matches()
                ? String.valueOf(resolveLocation(location, value).getPort())
                : value;
    }
}
