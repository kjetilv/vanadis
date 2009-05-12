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
package net.sf.vanadis.ext;

import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.collections.Pair;

import java.util.List;

class Parse {

    private static final String[] NO_ARGS = new String[]{};

    static Pair<String, String[]> args(String fullCommand) {
        if (!fullCommand.contains(" ")) {
            return Pair.of(fullCommand.trim(), NO_ARGS);
        }
        String[] split = fullCommand.split(" ");
        List<String> parsed = Generic.list();
        for (String s : split) {
            if (s == null) {
                {
                    continue;
                }
            }
            String t = s.trim();
            if (t.length() > 0) {
                parsed.add(t);
            }
        }
        String[] args = parsed.subList(1, parsed.size()).toArray(new String[parsed.size() - 1]);
        String command = parsed.get(0);
        return Pair.of(command, args);
    }
}
