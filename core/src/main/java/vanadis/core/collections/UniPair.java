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

package net.sf.vanadis.core.collections;

import net.sf.vanadis.core.lang.EqHc;

/**
 * A pair of two objects, called one and two.  They have the same type.
 */
public final class UniPair<T> {

    private final T one;

    private final T two;

    public static <T> UniPair<T> of(T obj1, T obj2) {
        return new UniPair<T>(obj1, obj2);
    }

    public UniPair(T one, T two) {
        this.one = one;
        this.two = two;
    }

    public T getOne() {
        return this.one;
    }

    public T getTwo() {
        return this.two;
    }

    @Override
    public String toString() {
        return super.toString() + "(" + this.one + ", " + this.two + ")";
    }

    @Override
    public boolean equals(Object o) {
        UniPair<T> pair = EqHc.retyped(this, o);
        return pair != null && EqHc.eq(this.one, pair.one,
                                       this.two, pair.two);
    }

    @Override
    public int hashCode() {
        return EqHc.hc(this.one, this.two);
    }
}