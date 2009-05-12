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

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Enumeration;

/**
 * An empty dictionary.
 *
 * @param <K> Type of the keys in this dictionary
 * @param <V> Type of the values in this dictionary
 */
final class EmptyDictionary<K, V> extends Dictionary<K, V> implements Serializable {

    private static final long serialVersionUID = 2569554589310719825L;

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Enumeration<K> keys() {
        return new EmptyEnumeration<K>();
    }

    @Override
    public Enumeration<V> elements() {
        return new EmptyEnumeration<V>();
    }

    @Override
    public V get(Object key) {
        return null;
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EmptyDictionary;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[EMPTY]";
    }

}
