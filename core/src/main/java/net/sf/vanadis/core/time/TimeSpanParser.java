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

package net.sf.vanadis.core.time;

import net.sf.vanadis.core.collections.Generic;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class TimeSpanParser {

    private static final Map<String, TimeUnit> SUFFS = Generic.linkedHashMap();

    private static final Map<String, Integer> FACTORS = Generic.linkedHashMap();

    static {
        storeSuffix("micros", MICROSECONDS);
        storeSuffix("microsecs", MICROSECONDS);
        storeSuffix("microsecond", MICROSECONDS);
        storeSuffix("microseconds", MICROSECONDS);
        storeSuffix("nanosecs", NANOSECONDS);
        storeSuffix("nanosecond", NANOSECONDS);
        storeSuffix("nanoseconds", NANOSECONDS);
        storeSuffix("?", MICROSECONDS);
        storeSuffix("m", SECONDS, 60);
        storeSuffix("min", SECONDS, 60);
        storeSuffix("mins", SECONDS, 60);
        storeSuffix("ns", NANOSECONDS);
        storeSuffix("hs", SECONDS, 3600);
        storeSuffix("hours", SECONDS, 3600);
        storeSuffix("millis", MILLISECONDS);
        storeSuffix("millisecs", MILLISECONDS);
        storeSuffix("millisecond", MILLISECONDS);
        storeSuffix("milliseconds", MILLISECONDS);
        storeSuffix("ms", MILLISECONDS);
        storeSuffix("second", SECONDS);
        storeSuffix("seconds", SECONDS);
        storeSuffix("secs", SECONDS);
        storeSuffix("s", SECONDS);
    }

    private static final Pattern PARSE = Pattern.compile("^(\\d*)\\s*([a-zA-Z]*)$");

    private static void storeSuffix(String key, TimeUnit value) {
        storeSuffix(key, value, 1);
    }

    private static void storeSuffix(String key, TimeUnit value, int factor) {
        SUFFS.put(key, value);
        FACTORS.put(key, factor);
    }

    static TimeSpan parse(String str) {
        if (str.trim().equalsIgnoreCase(TimeSpan.FOREVER_STRING)) {
            return TimeSpan.FOREVER;
        }
        Matcher matcher = PARSE.matcher(str.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Unparseable: " + str);
        }
        String num = matcher.group(1);
        long time;
        try {
            time = Long.parseLong(num);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Illegal number part: " + str, e);
        }
        String suff = matcher.group(2);
        if (suff.trim().length() == 0) {
            return TimeSpan.create(time, TimeUnit.SECONDS);
        }
        if (!SUFFS.containsKey(suff)) {
            throw new IllegalArgumentException("Unknown unit: " + str);
        }
        if (time <= 0) {
            return TimeSpan.INSTANT;
        }
        return TimeSpan.create(time * FACTORS.get(suff), SUFFS.get(suff));
    }

    private TimeSpanParser() {
    }
}
