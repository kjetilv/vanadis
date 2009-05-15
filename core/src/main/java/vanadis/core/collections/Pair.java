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

package vanadis.core.collections;

import vanadis.core.lang.EqHc;
import vanadis.core.lang.ToString;

/**
 * A pair of two objects, called one and two. May be inherited.
 */
public class Pair<F, S> {

    private final F one;

    private final S two;

    public static <F, S> Pair<F, S> of(F obj1, S obj2) {
        return new Pair<F, S>(obj1, obj2);
    }

    public Pair(F one, S two) {
        this.one = one;
        this.two = two;
    }

    public F getOne() {
        return this.one;
    }

    public S getTwo() {
        return this.two;
    }

    @Override
    public String toString() {
        return ToString.of(this, "one", one, "two", two);
    }

    /**
     * Compares both data items.
     *
     * @param o Object
     * @return Equality on data items
     */
    @Override
    public boolean equals(Object o) {
        Pair<F, S> pair = EqHc.retyped(this, o);
        return pair != null && EqHc.eq(this.one, pair.one,
                                       this.two, pair.two);
    }

    /**
     * Hashes both data items.
     *
     * @return Combined hash.
     */
    @Override
    public int hashCode() {
        return EqHc.hc(this.one, this.two);
    }
}
