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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * <P>Static utility methods for creating collections/maps/dictionaries, and for capturing generic
 * type parameters.</P>
 *
 * <P>Don't do: {@code Map<Foo,Bar> map = new HashMap<Foo,Bar>();}</P>
 *
 * <P>Do: {@code Map<Foo,Bar> map = Mapper.map();} - capturing Foo and Bar for you.</P>
 *
 * <P>Various utility methods for almost-literal map creation are also prevalent in this class:</P>
 *
 * <blockquote>{@code Map<Foo,Bar> map = Mapper.map(foo1, bar1, foo2, bar2);}</blockquote>
 */
public class Generic {

    public static final EmptyDictionary<String, Object> EMPTY_DICTIONARY = new EmptyDictionary<String, Object>();

    public static final Hashtable<String, Object> EMPTY_HASHTABLE = new EmptyHashtable<String, Object>();

    private static final Object[] EMPTY_ARRAY = new Object[]{};

    private static <E> List<E> iterate(Iterable<? extends E> iterable) {
        List<E> es = list();
        for (E e : iterable) {
            es.add(e);
        }
        return es;
    }

    public static <E> Collection<E> collection(Iterable<E> iterable) {
        return iterate(iterable);
    }

    public static <E> Set<E> set() {
        return new HashSet<E>();
    }

    public static <E> LinkedHashSet<E> linkedHashSet() {
        return new LinkedHashSet<E>();
    }

    public static <E> List<E> list() {
        return new ArrayList<E>();
    }

    public static <E> List<E> list(E... es) {
        List<E> list = list();
        list.addAll(Arrays.asList(es));
        return list;
    }

    public static <E> CopyOnWriteArrayList<E> copyOnWriteArrayList() {
        return new CopyOnWriteArrayList<E>();
    }

    public static <E> CopyOnWriteArraySet<E> copyOnWriteArraySet() {
        return new CopyOnWriteArraySet<E>();
    }

    public static <E> LinkedList<E> linkedList() {
        return new LinkedList<E>();
    }

    public static <E> Set<E> set(int size) {
        return new HashSet<E>(size);
    }

    public static <E> LinkedHashSet<E> linkedHashSet(int size) {
        return new LinkedHashSet<E>(size);
    }

    public static <E> List<E> list(int size) {
        return new ArrayList<E>(size);
    }

    public static <E> Set<E> set(Iterable<? extends E> iterable) {
        return set(iterate(iterable));
    }

    public static <E> LinkedHashSet<E> linkedHashSet(Iterable<? extends E> iterable) {
        return linkedHashSet(iterate(iterable));
    }

    public static <E> Set<E> set(Collection<? extends E> es) {
        return es == null ? Generic.<E>set()
                : (es instanceof LinkedHashSet ? linkedHashSet(es) : new HashSet<E>(es));
    }

    public static <E> LinkedHashSet<E> linkedHashSet(Collection<? extends E> es) {
        return es == null ? Generic.<E>linkedHashSet() : new LinkedHashSet<E>(es);
    }

    public static <T> Set<T> set(T... ts) {
        return new HashSet<T>(Arrays.asList(ts));
    }

    public static <T> Set<T> linkedHashSet(T... ts) {
        return new LinkedHashSet<T>(Arrays.asList(ts));
    }

    public static <E> List<E> list(Iterable<? extends E> iterable) {
        return list(iterate(iterable));
    }

    public static <E> List<E> list(Map<?, ? extends E> map) {
        return map == null ? Generic.<E>list() : new ArrayList<E>(map.values());
    }

    public static <E> List<E> list(Collection<? extends E> es) {
        return es == null ? Generic.<E>list() : new ArrayList<E>(es);
    }

    public static <E> CopyOnWriteArrayList<E> copyOnWriteList(Collection<? extends E> es) {
        return es == null ? Generic.<E>copyOnWriteArrayList() : new CopyOnWriteArrayList<E>(es);
    }

    public static <E> CopyOnWriteArraySet<E> copyOnWriteArraySet(Collection<? extends E> es) {
        return es == null ? Generic.<E>copyOnWriteArraySet() : new CopyOnWriteArraySet<E>(es);
    }

    public static <E> LinkedList<E> linkedList(Iterable<? extends E> iterable) {
        return linkedList(iterate(iterable));
    }

    public static <E> LinkedList<E> linkedList(E... es) {
        return es == null ? Generic.<E>linkedList() : new LinkedList<E>(Arrays.asList(es));
    }

    public static <E> LinkedList<E> linkedList(Collection<? extends E> es) {
        return es == null ? Generic.<E>linkedList() : new LinkedList<E>(es);
    }

    public static <E> Set<E> synchLinkedSet() {
        return Collections.synchronizedSet(new LinkedHashSet<E>());
    }

    public static <E> Set<E> synchSet() {
        return Collections.synchronizedSet(new HashSet<E>());
    }

    public static <E> List<E> synchList() {
        return Collections.synchronizedList(new ArrayList<E>());
    }

    public static <E> List<E> synchLinkedList() {
        return Collections.synchronizedList(new LinkedList<E>());
    }

    public static <E> Set<E> synchSet(Iterable<? extends E> iterable) {
        return synchSet(iterate(iterable));
    }

    public static <E> Set<E> synchSet(Collection<? extends E> es) {
        return es == null ? Generic.<E>synchSet()
                : Collections.synchronizedSet(new HashSet<E>(es));
    }

    public static <E> List<E> synchList(Iterable<? extends E> iterable) {
        return synchList(iterate(iterable));
    }

    public static <E> List<E> synchList(Collection<? extends E> es) {
        return es == null ? Generic.<E>synchList()
                : Collections.synchronizedList(new ArrayList<E>(es));
    }

    public static <E> List<E> synchLinkedList(Iterable<? extends E> iterable) {
        return synchLinkedList(iterate(iterable));
    }

    public static <E> List<E> synchLinkedList(Collection<? extends E> es) {
        return es == null ? Generic.<E>synchLinkedList()
                : Collections.synchronizedList(new LinkedList<E>(es));
    }

    public static Object[] toObjectArray(Object v) {
        if (v == null) {
            return EMPTY_ARRAY;
        }
        if (!v.getClass().isArray()) {
            return new Object[]{v};
        }
        Class<?> arrayType = v.getClass().getComponentType();
        if (arrayType.isArray()) {
            throw new IllegalArgumentException("Multi-dimensional array unsupported: " + v.getClass());
        }
        if (arrayType.isPrimitive()) {
            if (arrayType.equals(int.class)) {
                return toIntArray(v);
            } else if (arrayType.equals(byte.class)) {
                return toByteArray(v);
            } else if (arrayType.equals(long.class)) {
                return toLongArray(v);
            } else if (arrayType.equals(boolean.class)) {
                return toBooleanArray(v);
            } else if (arrayType.equals(char.class)) {
                return toCharArray(v);
            } else if (arrayType.equals(short.class)) {
                return toShortArray(v);
            } else if (arrayType.equals(float.class)) {
                return toFloatArray(v);
            } else if (arrayType.equals(double.class)) {
                return toDoubleArray(v);
            }
            throw new IllegalArgumentException
                    ("Unexpected array type: " + arrayType + ", this must be the future!  " +
                            "With more primitive types!  The fools!  The damn, damn fools!");
        }
        return (Object[]) v;
    }

    private static Object[] toDoubleArray(Object v) {
        double[] in = (double[]) v;
        Object[] out = new Object[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i];
        }
        return out;
    }

    private static Object[] toFloatArray(Object v) {
        float[] in = (float[]) v;
        Object[] out = new Object[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i];
        }
        return out;
    }

    private static Object[] toShortArray(Object v) {
        short[] in = (short[]) v;
        Object[] out = new Object[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i];
        }
        return out;
    }

    private static Object[] toCharArray(Object v) {
        char[] in = (char[]) v;
        Object[] out = new Object[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i];
        }
        return out;
    }

    private static Object[] toBooleanArray(Object v) {
        boolean[] in = (boolean[]) v;
        Object[] out = new Object[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i];
        }
        return out;
    }

    private static Object[] toLongArray(Object v) {
        long[] in = (long[]) v;
        Object[] out = new Object[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i];
        }
        return out;
    }

    private static Object[] toIntArray(Object v) {
        int[] in = (int[]) v;
        Object[] out = new Object[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i];
        }
        return out;
    }

    private static Object[] toByteArray(Object v) {
        byte[] in = (byte[]) v;
        Object[] out = new Object[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i];
        }
        return out;
    }

    @SuppressWarnings({"UseOfObsoleteCollectionType", "CollectionDeclaredAsConcreteClass"})
    public static <K, V> Hashtable<K, V> hashtable(Map<K, V> map) {
        return new Hashtable<K, V>(map);
    }

    @SuppressWarnings({"UseOfObsoleteCollectionType", "CollectionDeclaredAsConcreteClass"})
    public static <E> Vector<E> vector(List<E> es) {
        return new Vector<E>(es);
    }

    @SuppressWarnings({"UseOfObsoleteCollectionType", "CollectionDeclaredAsConcreteClass"})
    public static <E> Vector<E> vector() {
        return new Vector<E>();
    }

    @SuppressWarnings("unchecked")
    public static <E> List<E> cast(Class<E> elementClass, List<?> objects) {
        for (Object e : objects) {
            if (e != null && !elementClass.isInstance(e)) {
                throw new ClassCastException("Wrong type in arrayList " + objects);
            }
        }
        return (List<E>) objects;
    }

    @SuppressWarnings("unchecked")
    public static <E> Set<E> cast(Class<E> elementClass, Set<?> objects) {
        for (Object e : objects) {
            if (e != null && !elementClass.isInstance(e)) {
                throw new ClassCastException("Wrong type in set " + objects);
            }
        }
        return (Set<E>) objects;
    }

    @SuppressWarnings({"unchecked", "CollectionDeclaredAsConcreteClass", "UseOfObsoleteCollectionType"})
    public static <E> Vector<E> cast(Class<E> elementClass, Vector<?> vector) {
        for (Object e : vector) {
            if (e != null && !elementClass.isInstance(e)) {
                throw new ClassCastException("Wrong type in vector " + vector);
            }
        }
        return (Vector<E>) vector;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> cast(Class<K> keyClass, Class<V> valueClass, Map<?, ?> map) {
        for (Map.Entry<?, ?> e : map.entrySet()) {
            if (e != null) {
                Object key = e.getKey();
                if (key != null && !keyClass.isInstance(key)) {
                    throw new ClassCastException("Wrong key class in map " + map);
                }
                Object value = e.getValue();
                if (value != null && !valueClass.isInstance(value)) {
                    throw new ClassCastException("Wrong key type in map " + map);
                }
            }
        }
        return (Map<K, V>) map;
    }

    /**
     * Return safely unmodifiable copy of the input set.
     *
     * @param set Set
     * @return Unmodifiable copy
     */
    public static <T> Set<T> seal(Set<T> set) {
        return set == null || set.isEmpty() ? Collections.<T>emptySet()
                : Collections.unmodifiableSet(set(set));
    }

    /**
     * Return safely unmodifiable copy of the input list.
     *
     * @param list List
     * @return Unmodifiable copy
     */
    public static <T> List<T> seal(List<T> list) {
        return list == null || list.isEmpty() ? Collections.<T>emptyList()
                : Collections.unmodifiableList(list(list));
    }

    /**
     * Return safely unmodifiable copy of the input collection.
     *
     * @param collection Collection
     * @return Unmodifiable copy
     */
    public static <T> Collection<T> seal(Collection<T> collection) {
        return collection == null || collection.isEmpty() ? Collections.<T>emptyList()
                : Collections.unmodifiableList(list(collection));
    }

    public static <K, V> IdentityHashMap<K, V> identityMap() {
        return new IdentityHashMap<K, V>();
    }

    public static <K, V> IdentityHashMap<K, V> identityMap(Map<? extends K, ? extends V> map) {
        return map == null ? Generic.<K, V>identityMap() : new IdentityHashMap<K, V>(map);
    }

    public static <K, V> Map<K, V> map(Map<K, V> mkv, K key, V value) {
        mkv.put(key, value);
        return mkv;
    }

    public static <K, V> Map<K, V> map() {
        return new HashMap<K, V>();
    }

    public static <K, V> Map<K, V> treeMap() {
        return new TreeMap<K, V>();
    }

    public static <K, V> Map<K, V> map(K key, V value) {
        return map(Generic.<K, V>map(), key, value);
    }

    public static <K, V> Map<K, V> map(K k1, V v1,
                                       K k2, V v2) {
        return map(map(k1, v1), k2, v2);
    }

    public static <K, V> Map<K, V> map(K k1, V v1,
                                       K k2, V v2,
                                       K k3, V v3) {
        return map(map(k1, v1, k2, v2), k3, v3);
    }

    public static <K, V> Map<K, V> map(K k1, V v1,
                                       K k2, V v2,
                                       K k3, V v3,
                                       K k4, V v4) {
        return map(map(k1, v1, k2, v2, k3, v3), k4, v4);
    }

    public static <K, V> Map<K, V> map(K k1, V v1,
                                       K k2, V v2,
                                       K k3, V v3,
                                       K k4, V v4,
                                       K k5, V v5) {
        return map(map(k1, v1, k2, v2, k3, v3, k4, v4), k5, v5);
    }

    public static <K, V> Map<K, V> map(K k1, V v1,
                                       K k2, V v2,
                                       K k3, V v3,
                                       K k4, V v4,
                                       K k5, V v5,
                                       K k6, V v6) {
        return map(map(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5), k6, v6);
    }

    public static <K, V> Map<K, V> map(K k1, V v1,
                                       K k2, V v2,
                                       K k3, V v3,
                                       K k4, V v4,
                                       K k5, V v5,
                                       K k6, V v6,
                                       K k7, V v7) {
        return map(map(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6), k7, v7);
    }

    public static <K, V> Map<K, V> map(K k1, V v1,
                                       K k2, V v2,
                                       K k3, V v3,
                                       K k4, V v4,
                                       K k5, V v5,
                                       K k6, V v6,
                                       K k7, V v7,
                                       K k8, V v8) {
        return map(map(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7), k8, v8);
    }

    public static <K, V> LinkedHashMap<K, V> linkedHashMap(LinkedHashMap<K, V> mkv, K key, V value) {
        mkv.put(key, value);
        return mkv;
    }

    public static <K, V> LinkedHashMap<K, V> linkedHashMap() {
        return new LinkedHashMap<K, V>();
    }

    public static <K, V> LinkedHashMap<K, V> linkedHashMap(K key, V value) {
        return linkedHashMap(Generic.<K, V>linkedHashMap(), key, value);
    }

    public static <K, V> LinkedHashMap<K, V> linkedHashMap(K k1, V v1,
                                                           K k2, V v2) {
        return linkedHashMap(linkedHashMap(k1, v1), k2, v2);
    }

    public static <K, V> LinkedHashMap<K, V> linkedHashMap(K k1, V v1,
                                                           K k2, V v2,
                                                           K k3, V v3) {
        return linkedHashMap(linkedHashMap(k1, v1, k2, v2), k3, v3);
    }

    public static <K, V> LinkedHashMap<K, V> linkedHashMap(K k1, V v1,
                                                           K k2, V v2,
                                                           K k3, V v3,
                                                           K k4, V v4) {
        return linkedHashMap(linkedHashMap(k1, v1, k2, v2, k3, v3), k4, v4);
    }

    public static <K, V> LinkedHashMap<K, V> linkedHashMap(K k1, V v1,
                                                           K k2, V v2,
                                                           K k3, V v3,
                                                           K k4, V v4,
                                                           K k5, V v5) {
        return linkedHashMap(linkedHashMap(k1, v1, k2, v2, k3, v3, k4, v4), k5, v5);
    }

    public static <K, V> LinkedHashMap<K, V> linkedHashMap(K k1, V v1,
                                                           K k2, V v2,
                                                           K k3, V v3,
                                                           K k4, V v4,
                                                           K k5, V v5,
                                                           K k6, V v6) {
        return linkedHashMap(linkedHashMap(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5), k6, v6);
    }

    public static <K, V> LinkedHashMap<K, V> linkedHashMap(K k1, V v1,
                                                           K k2, V v2,
                                                           K k3, V v3,
                                                           K k4, V v4,
                                                           K k5, V v5,
                                                           K k6, V v6,
                                                           K k7, V v7) {
        return linkedHashMap(linkedHashMap(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6), k7, v7);
    }

    public static <K, V> LinkedHashMap<K, V> linkedHashMap(K k1, V v1,
                                                           K k2, V v2,
                                                           K k3, V v3,
                                                           K k4, V v4,
                                                           K k5, V v5,
                                                           K k6, V v6,
                                                           K k7, V v7,
                                                           K k8, V v8) {
        return linkedHashMap(linkedHashMap(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7), k8, v8);
    }

    public static <K, V> Map<K, V> map(Hashtable<K, V> ht) {
        Map<K, V> map = map();
        for (Map.Entry<K, V> entry : ht.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    @SuppressWarnings({"unchecked"})
    public static Map<String, Object> map(Properties p) {
        Map<String, Object> map = map();
        if (p != null) {
            EnumerationIterable<?> iterable = new EnumerationIterable(p.propertyNames());
            for (Object key : iterable) {
                String keyString = key.toString();
                map.put(keyString, p.getProperty(keyString));
            }
        }
        return map;
    }

    public static <K, V> WeakHashMap<K, V> weakHashMap() {
        return new WeakHashMap<K, V>();
    }

    public static <K, V> Map<K, V> map(Map<? extends K, ? extends V> map) {
        if (map == null) {
            return map();
        }
        if (map instanceof IdentityHashMap) {
            return new IdentityHashMap<K, V>(map);
        }
        if (map instanceof LinkedHashMap) {
            return new LinkedHashMap<K, V>(map);
        }
        return new HashMap<K, V>(map);
    }

    public static LinkedHashMap<Object, Object> linkedHashMap(Properties map) {
        return map == null ? Generic.linkedHashMap() : new LinkedHashMap(map);
    }

    public static <K, V> LinkedHashMap<K, V> linkedHashMap(Map<? extends K, ? extends V> map) {
        return map == null ? Generic.<K, V>linkedHashMap() : new LinkedHashMap<K, V>(map);
    }

    public static <K, V> Map<K, V> synchMap() {
        return Collections.synchronizedMap(new HashMap<K, V>());
    }

    public static <K, V> Map<K, V> synchLinkedMap() {
        return Collections.synchronizedMap(new LinkedHashMap<K, V>());
    }

    public static <K, V> Map<K, V> synchMap(Map<? extends K, ? extends V> map) {
        return map == null ? Generic.<K, V>synchMap()
                : Collections.synchronizedMap(map(map));
    }

    public static <E> ConcurrentSkipListSet<E> concurrentSkipListSet() {
        return new ConcurrentSkipListSet<E>();
    }

    public static <E> ConcurrentSkipListSet<E> concurrentSkipListSet(Collection<? extends E> es) {
        return new ConcurrentSkipListSet<E>(es);
    }

    public static <E> ConcurrentSkipListSet<E> concurrentSkipListSet(Comparator<? super E> comparator) {
        return new ConcurrentSkipListSet<E>(comparator);
    }

    public static <K, V> SizeLimitedHashMap<K, V> sizeLimitedHashMap(int size) {
        return new SizeLimitedHashMap<K, V>(size < 1 ? 1 : size);
    }

    public static <K, V> ConcurrentHashMap<K, V> concurrentHashMap() {
        return new ConcurrentHashMap<K, V>();
    }

    public static <K, V> ConcurrentHashMap<K, V> concurrentHashMap(int initialCapacity) {
        return new ConcurrentHashMap<K, V>(initialCapacity);
    }

    public static <K, V> ConcurrentHashMap<K, V> concurrentHashMap(int initialCapacity,
                                                                   float loadFactor) {
        return new ConcurrentHashMap<K, V>(initialCapacity, loadFactor);
    }

    public static <K, V> ConcurrentHashMap<K, V> concurrentHashMap(int initialCapacity,
                                                                   float loadFactor,
                                                                   int concurrencyLevel) {
        return new ConcurrentHashMap<K, V>(initialCapacity, loadFactor, concurrencyLevel);
    }

    public static <K, V> Map<K, V> synchLinkedMap(Map<? extends K, ? extends V> map) {
        return map == null ? Generic.<K, V>synchLinkedMap()
                : Collections.synchronizedMap(new LinkedHashMap<K, V>(map));
    }

    @SuppressWarnings({"UseOfObsoleteCollectionType"})
    public static Dictionary<String, Object> dictionary(Map<String, ?> map) {
        return map == null || map.isEmpty() 
                ? emptyDictionary()
                : new Hashtable<String, Object>(map);
    }

    public static Map<String, Object> mapDictionary(Dictionary<String, Object> dictionary) {
        Map<String, Object> map = map();
        for (Enumeration<String> keys = dictionary.keys(); keys.hasMoreElements();) {
            String key = keys.nextElement();
            map.put(key, dictionary.get(key));
        }
        return map;
    }

    public static <K, V> Map<K, V> seal(Map<K, V> map) {
        return map == null || map.isEmpty() ? Collections.<K, V>emptyMap()
                : Collections.unmodifiableMap(map(map));
    }

    // Beastly long, this class     ...
    @SuppressWarnings({"ReturnOfCollectionOrArrayField"})
    public static Dictionary<String, Object> emptyDictionary() {
        return EMPTY_DICTIONARY;
    }

    @SuppressWarnings({"ReturnOfCollectionOrArrayField"})
    public static Hashtable<String, Object> emptyHashtable() {
        return EMPTY_HASHTABLE;
    }

    private Generic() {}
}
