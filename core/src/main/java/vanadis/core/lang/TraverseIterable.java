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

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class TraverseIterable<E> implements Iterable<E> {

    private final E start;

    protected TraverseIterable(E start) {
        this.start = start;
    }

    @Override
    public Iterator<E> iterator() {
        return start == null ? Collections.<E>emptyList().iterator() : new It(start);
    }

    private class It implements Iterator<E> {

        private E point;

        It(E point) {
            this.point = point;
        }

        @Override
        public boolean hasNext() {
            return point != null;
        }

        @Override
        public E next() {
            if (point != null) {
                E returned = point;
                point = getNext(point);
                return returned;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    protected abstract E getNext(E current);
}
