package vanadis.core.properties;

import vanadis.core.collections.Generic;

import java.io.InputStream;
import java.util.Map;

public final class PropertySets {

    public static final PropertySet EMPTY = new MapPropertySet(null, null, false, false, true);

    public static PropertySet create(InputStream inputStream) {
        return create(inputStream, true);
    }

    public static PropertySet create() {
        return new MapPropertySet(null, null, true, false, true);
    }

    public static PropertySet create(Map<String, ?> map) {
        return create(map, true);
    }

    public static PropertySet create(Map<String, ?> map, boolean writable) {
        return createFromStringMap(map, writable);
    }

    /**
     * Create a property set from the map, converting all keys to {@link String#valueOf(Object) strings}.
     *
     * @param map Map
     * @return PropertySet
     */
    public static PropertySet createCoerced(Map<?, ?> map) {
        return createCoerced(map, true);
    }

    /**
     * Create a property set from the map, converting all keys to {@link String#valueOf(Object) strings}.
     *
     * @param map Map
     * @param writable Writable
     * @return PropertySet
     */
    public static PropertySet createCoerced(Map<?, ?> map, boolean writable) {
        return new MapPropertySet(map, null, writable, true, false);
    }

    public static PropertySet create(InputStream inputStream, boolean writable) {
        return new MapPropertySet(MapPropertySet.map(inputStream), null, writable, false, true);
    }

    public static PropertySet immutableFrom(Map<String, ?> map) {
        return new MapPropertySet(map, null, false, true, true);
    }

    public static PropertySet create(String property, Object value) {
        return createFromStringMap(Generic.linkedHashMap(property, value), true);
    }

    public static PropertySet create(String property1, Object value1,
                                     String property2, Object value2) {
        return createFromStringMap(Generic.linkedHashMap(property1, value1, property2, value2), true);
    }

    public static PropertySet create(String property1, Object value1,
                                     String property2, Object value2,
                                     String property3, Object value3) {
        return createFromStringMap(Generic.linkedHashMap
                (property1, value1, property2, value2, property3, value3), true);
    }

    public static PropertySet create(String property1, Object value1,
                                     String property2, Object value2,
                                     String property3, Object value3,
                                     String property4, Object value4) {
        return createFromStringMap(Generic.linkedHashMap
                (property1, value1, property2, value2, property3, value3, property4, value4), true);
    }

    public static PropertySet systemProperties() {
        Map<String, Object> sp = Generic.map();
        for (Object key : System.getProperties().keySet()) {
            String property = String.valueOf(key);
            String value = System.getProperty(property);
            sp.put(property, value);
        }
        return create(sp, false);
    }

    private static PropertySet createFromStringMap(Map<String, ?> stringObjectMap, boolean writable) {
        return new MapPropertySet(stringObjectMap, null, writable, false, true);
    }

    private PropertySets() { }
}
