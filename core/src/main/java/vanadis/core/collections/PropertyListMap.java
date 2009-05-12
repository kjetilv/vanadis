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

import java.util.*;

final class PropertyListMap<K, V> implements Map<K, V> {

    private Object[][] matrix;

    private int capacity;

    private final int loadFactorPercentage;

    private final int shrinkRange;

    private int size;

    PropertyListMap() {
        this(20, 75);
    }

    PropertyListMap(int capacity, int loadFactorPercentage) {
        this.capacity = capacity;
        this.shrinkRange = capacity * 2;
        this.loadFactorPercentage = loadFactorPercentage;
        clear();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public V get(Object query) {
        int code = query.hashCode();
        for (Object[] pair : matrix) {
            if (notEmpty(pair) && keyMatch(query, code, pair)) {
                return val(pair);
            }
        }
        return null;
    }

    @Override
    public V put(K key, V val) {
        return put(key.hashCode(), key, val, 0);
    }

    private V put(int code, K key, V val, int start) {
        if (fillPercentage(size + 1) > loadFactorPercentage) {
            grow();
        }
        Object[] empty = null;
        for (int i = start; i < matrix.length; i++) {
            Object[] pair = matrix[i];
            boolean emptyPair = empty(pair);
            if (empty == null && emptyPair) {
                empty = pair;
            } else if (!emptyPair && keyMatch(key, code, pair)) {
                return setVal(pair, val);
            }
        }
        if (empty != null) {
            setKey(empty, key);
            setVal(empty, val);
            size++;
            return null;
        }
        grow();
        return put(code, key, val, capacity / 2);
    }

    private void shrink() {
        capacity /= 2;
        redist(matrix);
    }

    private void grow() {
        capacity *= 2;
        redist(matrix);
    }

    private void redist(Object[][] previous) {
        clear();
        int i = 0;
        for (Object[] pair : previous) {
            if (notEmpty(pair)) {
                matrix[i++] = pair;
            }
        }
    }

    @Override
    public V remove(Object key) {
        int code = key.hashCode();
        for (Object[] pair : matrix) {
            if (keyMatch(key, code, pair)) {
                setKey(pair, null);
                V previous = setVal(pair, null);
                size--;
                if (shrinkable()) {
                    shrink();
                }
                return previous;
            }
        }
        return null;
    }

    private boolean shrinkable() {
        return capacity >= shrinkRange && fillPercentage(size) < loadFactorPercentage;
    }

    @Override
    public Collection<V> values() {
        List<V> list = Generic.list(matrix.length);
        for (Object[] pair : matrix) {
            if (notEmpty(pair)) {
                list.add(val(pair));
            }
        }
        return list;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        if (matrix.length == 0) {
            return Collections.emptySet();
        }
        Set<Entry<K, V>> entrySet = Generic.set();
        for (Object[] pair : matrix) {
            if (notEmpty(pair)) {
                entrySet.add(new E<K, V>(val(pair), key(pair)));
            }
        }
        return entrySet;
    }

    @Override
    public Set<K> keySet() {
        Set<K> set = Generic.set(matrix.length);
        for (Object[] pair : matrix) {
            if (notEmpty(pair)) {
                set.add(key(pair));
            }
        }
        return set;
    }

    @Override
    public void clear() {
        this.matrix = new Object[capacity][2];
    }

    @Override
    public boolean containsValue(Object query) {
        int code = query.hashCode();
        for (Object[] pair : matrix) {
            if (valMatch(query, code, pair)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        int code = key.hashCode();
        for (Object[] pair : matrix) {
            if (keyMatch(key, code, pair)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return matrix.length == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @SuppressWarnings({"unchecked"})
    private V val(Object[] pairs) {
        return (V) pairs[1];
    }

    @SuppressWarnings({"unchecked"})
    private K key(Object[] pairs) {
        return (K) pairs[0];
    }

    private K setKey(Object[] pair, Object key) {
        K previous = key(pair);
        pair[0] = key;
        return previous;
    }

    private V setVal(Object[] pair, Object value) {
        V previous = val(pair);
        pair[1] = value;
        return previous;
    }

    private int fillPercentage(int size) {
        return size * 100 / capacity;
    }

    private static boolean match(Object query, int hashCode, Object object) {
        return object.hashCode() == hashCode && object.equals(query);
    }

    private static boolean keyMatch(Object query, int code, Object[] pairs) {
        return match(query, code, pairs[0]);
    }

    private static boolean valMatch(Object query, int code, Object[] pairs) {
        return match(query, code, pairs[1]);
    }

    private static boolean empty(Object[] pair) {
        return pair[0] == null && pair[1] == null;
    }

    private static boolean notEmpty(Object[] pair) {
        return pair[0] != null || pair[1] != null;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public String toString() {
        return ToString.of(this, Arrays.toString(matrix));
    }
}
