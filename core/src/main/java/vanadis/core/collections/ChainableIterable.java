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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

final class ChainableIterable<E> implements Iterable<E> {

    static <E> ChainableIterable<E> create(Iterable<E> iterable) {
        return new ChainableIterable<E>(iterable);
    }

    private final List<Iterable<? extends E>> iterables = Generic.list();

    private ChainableIterable(Iterable<E> iterable) {
        iterables.add(iterable);
    }

    @Override
    public Iterator<E> iterator() {
        if (iterables.isEmpty()) {
            return Collections.<E>emptyList().iterator();
        }
        return new ChainedIterator<E>(Generic.seal(iterables).iterator());
    }

    ChainableIterable<E> chain(Iterable<E> it) {
        iterables.add(it);
        return this;
    }

    private static final class ChainedIterator<E> implements Iterator<E> {

        private final Iterator<Iterable<? extends E>> iterables;

        private Iterator<? extends E> currentIterator;

        private ChainedIterator(Iterator<Iterable<? extends E>> iterables) {
            this.iterables = iterables;
            this.currentIterator = iterables.next().iterator();
            resolveIterator();
        }

        @Override
        public boolean hasNext() {
            return currentIterator != null && currentIterator.hasNext() || iterables.hasNext();
        }

        @Override
        public E next() {
            E e = currentIterator.next();
            resolveIterator();
            return e;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void resolveIterator() {
            if (currentIterator.hasNext()) {
                return;
            }
            currentIterator = null;
            while (iterables.hasNext()) {
                Iterator<? extends E> nextIterator = iterables.next().iterator();
                if (nextIterator.hasNext()) {
                    currentIterator = nextIterator;
                    return;
                }
            }
        }
    }
}
