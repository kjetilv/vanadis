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
import vanadis.core.test.ForTestingPurposes;
import static vanadis.main.CommandLineHelper.*;

import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

public final class Main {

    public static void main(String[] args) {
        LaunchSite launchSite = fromCommandLine(Generic.linkedList(args));
        if (!successful(launchSite)) {
            ERR.println("Startup failed");
        }
    }

    @ForTestingPurposes
    static LaunchSite fromCommandLine(String args) {
        return fromCommandLine(Arrays.asList(args.split("\\s")));
    }

    private static final PrintStream OUT = System.out;

    private static final PrintStream ERR = System.err;

    private static LaunchSite fromCommandLine(List<String> args) {
        return launch(new CommandLineDigest(args));
    }

    private static LaunchSite launch(CommandLineDigest spc) {
        return LaunchSite.create(spc.getHome(), spc.getLocation(), spc.getRepoRoot(), 
                                 spc.getUriPatterns(),
                                 spc.getBlueprintNames(),
                                 spc.getBlueprintPaths(),
                                 spc.getBlueprintResources());
    }

    private static boolean successful(LaunchSite launchSite) {
        return launchSite.launch(OUT);
    }

    static class CommandLineDigest {

        private final List<String> blueprintPaths;

        private final List<String> blueprintResources;

        private final File home;

        private final Location location;

        private final List<String> blueprintNames;

        private final URI repoRoot;

        private List<String> uriPatterns;

        @ForTestingPurposes
        CommandLineDigest(String args) {
            this(Generic.linkedList(args.split("\\s")));
        }

        CommandLineDigest(List<String> args) {
            this.blueprintPaths = blueprintPathsArg(args);
            this.blueprintResources = blueprintResourcesArg(args);
            this.uriPatterns = uriPatterns(args);
            this.home = homeArg(args);
            this.location = locationArg(args);
            this.repoRoot = repoArg(args);
            List<String> names = Generic.list(blueprints(args));
            addRemainingArgumentsAsBlueprintNames(args, names);
            this.blueprintNames = Generic.seal(names);
        }

        public List<String> getBlueprintPaths() {
            return blueprintPaths;
        }

        public List<String> getUriPatterns() {
            return uriPatterns;
        }

        public List<String> getBlueprintResources() {
            return blueprintResources;
        }

        public File getHome() {
            return home;
        }

        public Location getLocation() {
            return location;
        }

        public List<String> getBlueprintNames() {
            return blueprintNames;
        }

        public URI getRepoRoot() {
            return repoRoot;
        }

        private static void addRemainingArgumentsAsBlueprintNames(List<String> remainingArgs,
                                                                  List<String> blueprintNames) {
            blueprintNames.addAll(remainingBlueprints(remainingArgs));
        }
    }
}
