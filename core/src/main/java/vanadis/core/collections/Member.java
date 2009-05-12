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

package net.sf.vanadis.core.collections;

import java.util.Collection;

public class Member {

    public static <T, E extends T> boolean of(E element, T... set) {
        for (T t : set) {
            if (element.equals(t)) {
                return true;
            }
        }
        return false;
    }

    public static <T, E extends T> boolean of(E element, Collection<T> set) {
        return of(element, (Iterable<T>) set);
    }

    public static <T, E extends T> boolean of(E element, Iterable<T> iterable) {
        if (iterable != null) {
            for (T member : iterable) {
                if (element.equals(member)) {
                    return true;
                }
            }
        }
        return false;
    }

}
