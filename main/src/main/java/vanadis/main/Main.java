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
package net.sf.vanadis.main;

import net.sf.vanadis.core.io.Location;
import static net.sf.vanadis.main.CommandLineHelper.*;

import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.util.List;

public final class Main {

    public static void main(String[] args) {
        LaunchSite launchSite = fromCommandLine(args);
        if (!successful(launchSite)) {
            ERR.println("Startup failed");
        }
//        launchSite.awaitShutdown();
    }

    private static final PrintStream OUT = System.out;

    private static final PrintStream ERR = System.err;

    private static boolean successful(LaunchSite launchSite) {
        return launchSite.launch(OUT);
    }

    public static LaunchSite fromCommandLine(String[] args) {
        List<String> blueprintPaths = blueprintPathsArg(args);
        List<String> blueprintResources = blueprintResourcesArg(args);
        File home = homeArg(args);
        Location location = locationArg(args);
        List<String> sheets = CommandLineHelper.sheets(args);
        URI repoRoot = repoArg(args);
        return LaunchSite.create(home, location, repoRoot, sheets, blueprintPaths, blueprintResources);
    }
}
