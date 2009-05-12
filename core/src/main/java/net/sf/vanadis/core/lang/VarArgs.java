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

package net.sf.vanadis.core.lang;

import net.sf.vanadis.core.collections.Generic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Utility methods for checking the state of your varargs.
 */
public final class VarArgs {

    /**
     * True if non-null varargs are present.
     *
     * @param varargs Varargs array
     * @return True if non-null varargs are present.
     */
    public static boolean present(Object[] varargs) {
        return varargs != null && (varargs.length == 1 && varargs[0] != null || varargs.length > 1);
    }

    /**
     * True if the varargs is null, empty or contains only one null.
     *
     * @param varargs Varargs
     * @return True if void
     */
    public static boolean notPresent(Object[] varargs) {
        return empty(varargs) || onlyNull(varargs);
    }

    /**
     * True if null or empty.
     *
     * @param varargs Varargs
     * @return True if null or empty
     */
    private static boolean empty(Object[] varargs) {
        return varargs == null || varargs.length == 0;
    }

    /**
     * True if varargs is a single null value.
     *
     * @param varargs Varargs
     * @return True if there is a single null value.
     */
    private static boolean onlyNull(Object[] varargs) {
        return varargs.length == 1 && varargs[0] == null;
    }

    public static boolean present(long[] varargs) {
        return !notPresent(varargs);
    }

    public static boolean notPresent(long[] varargs) {
        return empty(varargs) || onlyNull(varargs);
    }

    private static boolean empty(long[] varargs) {
        return varargs == null || varargs.length == 0;
    }

    private static boolean onlyNull(long[] varargs) {
        return varargs.length == 1 && varargs[0] == 0;
    }

    public static boolean present(boolean[] varargs) {
        return !notPresent(varargs);
    }

    public static boolean notPresent(boolean[] varargs) {
        return empty(varargs) || onlyNull(varargs);
    }

    private static boolean empty(boolean[] varargs) {
        return varargs == null || varargs.length == 0;
    }

    private static boolean onlyNull(boolean[] varargs) {
        return varargs.length == 1 && !varargs[0];
    }

    public static boolean present(int[] varargs) {
        return !notPresent(varargs);
    }

    public static boolean notPresent(int[] varargs) {
        return empty(varargs) || onlyNull(varargs);
    }

    private static boolean empty(int[] varargs) {
        return varargs == null || varargs.length == 0;
    }

    private static boolean onlyNull(int[] varargs) {
        return varargs.length == 1 && varargs[0] == 0;
    }

    public static <T> List<T> list(T[] varargs) {
        return present(varargs) ? Arrays.asList(varargs) : Collections.<T>emptyList();
    }

    public static <T> Set<T> set(T[] varargs) {
        return Generic.set(list(varargs));
    }

    private VarArgs() { }
}
