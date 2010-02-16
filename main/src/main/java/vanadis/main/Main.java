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
import vanadis.common.test.ForTestingPurposes;
import vanadis.launcher.LaunchSite;
import vanadis.launcher.ArgumentsSpecs;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

public final class Main {

    public static void main(String[] args) {
        LaunchSite launchSite = fromCommandLine(Generic.linkedList(args));
        if (!launchSite.launch(OUT)) {
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
        ArgumentsSpecs ss = new ArgumentsSpecs(args);
        return LaunchSite.create(ss);
    }
}
