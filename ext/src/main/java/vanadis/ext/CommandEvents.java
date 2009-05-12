/*
 * Copyright 2008 Kjetil Valstadsve
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
package vanadis.ext;

import vanadis.core.collections.Generic;
import org.osgi.service.event.Event;

import java.util.Dictionary;

public class CommandEvents {

    public static final String TOPIC = "V_COMMAND";

    public static final String COMMAND_PROPERTY = "command";

    public static final String ARGUMENTS_PROPERTY = "arguments";

    public static final String APOCALYPSE_PROPERTY = "ragnarok";

    public static final String NOW = "no!";

    public static Event newEvent(String command, String... args) {
        return new Event(TOPIC, properties(command, args));
    }

    private static Dictionary<String, Object> properties(String command, String[] args) {
        return Generic.dictionary(Generic.map
            (COMMAND_PROPERTY, command,
             ARGUMENTS_PROPERTY, args));
    }
}
