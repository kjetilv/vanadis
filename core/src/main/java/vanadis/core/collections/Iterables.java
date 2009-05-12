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

package vanadis.core.collections;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

public final class Iterables {

    public static <E> Iterable<E> chain(Iterable<E>... its) {
        return chain(Arrays.asList(its));
    }

    public static <E> Iterable<E> chain(Iterable<? extends Iterable<E>> its) {
        if (its == null) {
            return Collections.emptySet();
        }
        Iterator<? extends Iterable<E>> iterator = its.iterator();
        if (!iterator.hasNext()) {
            return Collections.emptySet();
        }
        Iterable<E> first = iterator.next();
        if (!iterator.hasNext()) {
            return first;
        }
        ChainableIterable<E> iterable = ChainableIterable.create(first);
        while (iterator.hasNext()) {
            iterable = iterable.chain(iterator.next());
        }
        return iterable;
    }

    private Iterables() { } 
}
