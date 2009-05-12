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
package vanadis.osgi;

import java.util.Iterator;

enum CompositeOp {

    AND("&") {
        @Override
        public boolean match(Iterable<Boolean> matches) {
            return allTrue(matches);
        }},

    OR("|") {
        @Override
        public boolean match(Iterable<Boolean> matches) {
            return existsTrue(matches);
        }
    },

    NOT("!") {
        @Override
        public boolean match(Iterable<Boolean> matches) {
            return !singleMatch(matches);
        }
    };

    private static boolean existsTrue(Iterable<Boolean> matches) {
        for (boolean match : matches) {
            if (match) {
                return true;
            }
        }
        return false;
    }

    private static boolean allTrue(Iterable<Boolean> matches) {
        for (boolean match : matches) {
            if (!match) {
                return false;
            }
        }
        return true;
    }

    private static boolean singleMatch(Iterable<Boolean> matches) {
        boolean match;
        Iterator<Boolean> iterator = matches.iterator();
        if (iterator.hasNext()) {
            match = iterator.next();
        } else {
            throw new IllegalStateException(NOT + " got no argument!");
        }
        if (iterator.hasNext()) {
            throw new IllegalStateException(NOT + " got > 1 arguments!");
        }
        return match;
    }

    private final String repr;

    CompositeOp(String repr) {
        this.repr = repr;
    }

    public String repr() {
        return repr;
    }

    public abstract boolean match(Iterable<Boolean> matches);

}
