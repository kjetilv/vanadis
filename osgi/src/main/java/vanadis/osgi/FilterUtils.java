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

class FilterUtils {

    private static final Filter[] NO_EXPRS = new Filter[]{};

    public static Filter[] nonNulls(Filter[] base) {
        int nonNulls = nonNullCount(base);
        return nonNulls == 0 ? NO_EXPRS
                : removeNonNulls(base, nonNulls);
    }

    private static Filter[] removeNonNulls(Filter[] base, int nonNullsCount) {
        Filter[] array = new Filter[nonNullsCount];
        int index = 0;
        for (Filter filter : base) {
            if (filter != null && !filter.isNull()) {
                array[index++] = filter;
            }
        }
        return array;
    }

    private static int nonNullCount(Filter[] base) {
        int nonNulls = 0;
        for (Filter filter : base) {
            if (filter != null && !filter.isNull()) {
                nonNulls++;
            }
        }
        return nonNulls;
    }
}
