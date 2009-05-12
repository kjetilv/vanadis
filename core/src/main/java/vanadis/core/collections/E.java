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

import vanadis.core.lang.ToString;

import java.util.Map;

final class E<K, V> implements Map.Entry<K, V> {

    private final V value;

    private final K key;

    E(V value, K key) {
        this.value = value;
        this.key = key;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public String toString() {
        return ToString.of(this, key, value);
    }
}
