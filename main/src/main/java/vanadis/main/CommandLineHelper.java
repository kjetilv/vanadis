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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * All methods in this class remove the argument and value from the input list, after parsing it.
 */
class CommandLineHelper {

    static List<String> blueprintResourcesArg(List<String> args) {
        return split(parseOption(args, "blueprint-resources"));
    }

    static List<String> blueprintPathsArg(List<String> args) {
        return split(parseOption(args, "blueprint-paths"));
    }

    static List<String> blueprints(List<String> args) {
        return split(parseOption(args, "blueprints"));
    }

    static List<String> uriPatterns(List<String> args) {
        return split(parseOption(args, "uri-patterns"));
    }

    static List<String> remainingBlueprints(List<String> args) {
        if (args.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> names = Generic.list();
        for (String arg : args) {
            names.addAll(split(arg));
        }
        return names;
    }

    static URI repoArg(List<String> args) {
        String repo = parseOption(args, "repo");
        if (repo == null) {
            String defaultRepo = parseOption(args, "defaultRepo");
            return defaultRepo == null ? null : URI.create(defaultRepo);
        }
        return URI.create(repo);
    }

    static File homeArg(List<String> args) {
        String home = parseOption(args, "home");
        if (home == null) {
            String defaultHome = parseOption(args, "defaultHome");
            return defaultHome == null ? null
                    : new File(defaultHome).getAbsoluteFile();
        }
        return new File(home).getAbsoluteFile();
    }

    static Location locationArg(List<String> args) {
        String spec = parseOption(args, "location");
        if (spec == null) {
            String defaultLocation = parseOption(args, "defaultLocation");
            return defaultLocation == null ? null
                    : Location.parse(defaultLocation);
        }
        return Location.parse(spec);
    }

    private static String parseOption(List<String> args, String option) {
        for (int i = 0; i < args.size() - 1; i++) {
            if (args.get(i).startsWith("-")) {
                String arg = dedash(args.get(i).toLowerCase());
                if (option.toLowerCase().startsWith(arg)) {
                    try {
                        return args.get(i + 1);
                    } finally {
                        args.remove(i + 1);
                        args.remove(i);
                    }
                }
            }
        }
        return null;
    }

    private static String dedash(String arg) {
        String dedashed = arg;
        while (dedashed.startsWith("-")) {
            dedashed = dedashed.substring(1);
        }
        return dedashed;
    }

    private static List<String> split(String string) {
        return string == null || string.trim().isEmpty()
                ? Collections.<String>emptyList()
                : Arrays.asList(string.split(","));
    }
}
