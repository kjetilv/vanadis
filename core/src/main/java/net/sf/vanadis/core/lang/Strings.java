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

/**
 * String utilities.
 */
public final class Strings {

    /**
     * True for {@link #isEmpty(String) empty} and all-whitespace strings.
     *
     * @param string String
     * @return True if {@link #isEmpty(String) empty} or all-whitespace
     */
    public static boolean isBlank(String string) {
        return isEmpty(string) || string.trim().length() == 0;
    }

    /**
     * True for null and zero-length strings.
     *
     * @param string String
     * @return True if null or zero-length
     */
    public static boolean isEmpty(String string) {
        return string == null || string.length() == 0;
    }

    /**
     * True for objects that are null or zero-length strings.
     *
     * @param object Object
     * @return True if null or a zero-length string
     */
    public static boolean isEmptyString(Object object) {
        return object == null || object instanceof String && isEmpty((String) object);
    }

    /**
     * True for objects that are {@link #isEmpty(String) empty} and all-whitespace strings.
     *
     * @param object Object
     * @return True if {@link #isEmpty(String) empty} string or all-whitespace string
     */
    public static boolean isBlankString(Object object) {
        return object instanceof String && isBlank((String) object);
    }

    public static String neitherNullNorEmpty(Object string) {
        return isEmptyString(string) ? null : string.toString();
    }

    public static String trim(Object string) {
        return isEmptyString(string) ? null : string.toString().trim();
    }

    private Strings() { }
}
