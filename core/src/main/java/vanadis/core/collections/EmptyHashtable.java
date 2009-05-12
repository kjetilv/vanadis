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

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * An empty hashtable.
 *
 * @param <K> Type of the keys in this hashtable
 * @param <V> Type of the values in this hashtable
 */
@SuppressWarnings({"SynchronizedMethod"}) // Sins of the fathers...
final class EmptyHashtable<K, V> extends Hashtable<K, V> {

    private static final long serialVersionUID = 2569554589310719825L;

    @Override
    public synchronized int size() {
        return 0;
    }

    @Override
    public synchronized boolean isEmpty() {
        return true;
    }

    @Override
    public synchronized Enumeration<K> keys() {
        return new EmptyEnumeration<K>();
    }

    @Override
    public synchronized Enumeration<V> elements() {
        return new EmptyEnumeration<V>();
    }

    @Override
    public synchronized V get(Object key) {
        return null;
    }

    @Override
    public synchronized V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized int hashCode() {
        return 1;
    }

    @Override
    public synchronized boolean equals(Object obj) {
        return obj instanceof EmptyHashtable;
    }

    @Override
    public synchronized String toString() {
        return getClass().getName() + "[EMPTY]";
    }

}