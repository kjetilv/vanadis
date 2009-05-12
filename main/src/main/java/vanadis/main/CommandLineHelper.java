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

import vanadis.core.collections.Generic;
import vanadis.core.io.Location;

import java.io.File;
import java.net.URI;
import java.util.List;

class CommandLineHelper {

    static List<String> blueprintResourcesArg(String[] args) {
        String string = parseOption(args, "blueprint-resources");
        return string == null ? Generic.<String>list()
                : Generic.list(string.split(","));
    }

    static List<String> blueprintPathsArg(String[] args) {
        String string = parseOption(args, "blueprint-paths");
        return string == null ? Generic.<String>list()
                : Generic.list(string.split(","));
    }

    static List<String> sheets(String[] args) {
        return Generic.list(parseOption(args, "blueprint-sheets", "base-commands").split(","));
    }

    static URI repoArg(String[] args) {
        String repo = parseOption(args, "repo");
        if (repo == null) {
            String defaultRepo = parseOption(args, "defaultRepo");
            return defaultRepo == null ? null : URI.create(defaultRepo);
        }
        return URI.create(repo);
    }

    static File homeArg(String[] args) {
        String home = parseOption(args, "home");
        if (home == null) {
            String defaultHome = parseOption(args, "defaultHome");
            return defaultHome == null ? null
                    : new File(defaultHome).getAbsoluteFile();
        }
        return new File(home).getAbsoluteFile();
    }

    static Location locationArg(String[] args) {
        String spec = parseOption(args, "location");
        if (spec == null) {
            String defaultLocation = parseOption(args, "defaultLocation");
            return defaultLocation == null ? null
                    : Location.parse(defaultLocation);
        }
        return Location.parse(spec);
    }

    private static String parseOption(String[] args, String option) {
        return parseOption(args, option, null);
    }

    private static String parseOption(String[] args, String option, String defaultValue) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].startsWith("-")) {
                String arg = dedash(args[i].toLowerCase());
                if (option.toLowerCase().startsWith(arg)) {
                    return args[i + 1];
                }
            }
        }
        return defaultValue;
    }

    private static String dedash(String arg) {
        String dedashed = arg;
        while (dedashed.startsWith("-")) {
            dedashed = dedashed.substring(1);
        }
        return dedashed;
    }
}
