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
package net.sf.vanadis.core.lang;

import java.util.Collection;

public final class Not {

    public static <T> T nil(T object, String msg) {
        if (object == null) {
            throw new NullPointerException("Expected non-null: " + msg);
        }
        return object;
    }

    public static <T, C extends Collection<T>> C empty(C collection, String msg) {
        if (nil(collection, msg).isEmpty()) {
            nonEmpty(msg);
        }
        return collection;
    }

    public static String nilOrEmpty(String string, String msg) {
        if (nil(string, msg).trim().isEmpty()) {
            return nonEmpty(msg);
        }
        return string;
    }

    private static String nonEmpty(String msg) {
        throw new IllegalArgumentException("Expected non-empty: " + msg);
    }

    private Not() {
        // No instantiation without representation! Not!
    }

    public static <T> T[] emptyVarArgs(T[] args, String msg) {
        if (VarArgs.present(args)) {
            return args;
        }
        throw new IllegalArgumentException("Expected varargs: " + msg);
    }
}
