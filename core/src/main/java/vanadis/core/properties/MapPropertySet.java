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

package vanadis.core.properties;

import vanadis.core.collections.Generic;
import vanadis.core.lang.EqHc;
import vanadis.core.lang.ToString;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.*;

final class MapPropertySet extends AbstractPropertySet {

    private final Map<String, Object> map;

    private static final long serialVersionUID = 948850651877232903L;

    MapPropertySet(Map<?, ?> map, PropertySet parent, boolean writable, boolean copyMap, boolean isAStringMap) {
        super(parent, writable);
        this.map = resolveMap(map, copyMap, writable, isAStringMap);
    }

    @Override
    protected void setLocal(String key, Object value) {
        map.put(key, value);
    }

    @Override
    public MapPropertySet writeProperties(OutputStream stream, String encoding) {
        super.writeProperties(stream, encoding);
        return this;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Collection<String> getPropertyNames() {
        return map.keySet();
    }

    @Override
    public PropertySet expand(PropertySet... variables) {
        PropertySet propertySet = PropertySets.create();
        for (String property : this) {
            Object propertyValue = get(property, variables);
            propertySet.set(property, propertyValue);
        }
        return propertySet;

    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Properties toProperties() {
        Properties properties = new Properties();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                properties.setProperty(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return properties;
    }

    @Override
    protected PropertySet makeCopy(PropertySet parent, boolean writable) {
        boolean immutableCopyOfEmpty = !writable && this.isEmpty();
        if (immutableCopyOfEmpty) {
            return PropertySets.EMPTY;
        }
        boolean immutableCopyOfImmutable = !this.isWritable() && !writable;
        return immutableCopyOfImmutable ? this
                : new MapPropertySet(map, parent == null ? copyOfNext(writable) : parent, writable, writable, true);
    }

    @Override
    public Map<String, Object> toMap(boolean collapse) {
        if (collapse) {
            Map<String, Object> map = Generic.linkedHashMap();
            for (String key : this.map.keySet()) {
                map.put(key, get(key));
            }
            if (getParent() == null) {
                return map;
            }
            map.putAll(getParent().toMap(true));
            return map;
        }
        return Collections.unmodifiableMap(Generic.linkedHashMap(map));
    }

    @Override
    public Dictionary<String, Object> toDictionary(boolean collapse) {
        return Generic.dictionary(toMap(collapse));
    }

    @Override
    public Hashtable<String, Object> toHashtable(boolean collapse) {
        return Generic.hashtable(toMap(collapse));
    }

    @Override
    protected Set<String> keySet() {
        return map.keySet();
    }

    @Override
    protected Object getLocal(String key) {
        return map.get(key);
    }

    @Override
    protected AbstractPropertySet doOrphan() {
        return new MapPropertySet(map, null, isWritable(), isWritable(), true);
    }

    private String printMap() {
        if (map.isEmpty()) {
            return "[]";
        }
        if (map.size() > 5) {
            return map.keySet().toString();
        }
        StringBuilder sb = new StringBuilder("[");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(" ");
        }
        sb.setCharAt(sb.length() - 1, ']');
        return sb.toString();
    }

    private PropertySet copyOfNext(boolean writable) {
        return getParent() == null ? null : getParent().copy(writable);
    }

    static Map<String, Object> map(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        Properties properties = load(inputStream);
        LinkedHashMap<String, Object> map = Generic.linkedHashMap();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            map.put(entry.getKey().toString(), entry.getValue());
        }
        return map;
    }

    private static Properties load(InputStream stream) {
        Properties properties = new Properties();
        try {
            properties.load(stream);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load from " + stream, e);
        }
        return properties;
    }

    private static Map<String, Object> resolveMap(Map<?, ?> map,
                                                  boolean copyMap,
                                                  boolean writable,
                                                  boolean isAStringMap) {
        if (map == null) {
            return writable ? Generic.<String, Object>linkedHashMap()
                    : Collections.<String, Object>emptyMap();
        }
        Map<String, Object> stringMap = isAStringMap ? (Map<String, Object>) map : strungUp(map);
        if (copyMap) {
            return writable ? Generic.linkedHashMap(stringMap)
                    : Generic.seal(stringMap);
        }
        if (writable) {
            return stringMap;
        }
        return Collections.unmodifiableMap(stringMap);
    }

    private static Map<String, Object> strungUp(Map<?, ?> map) {
        Map<String, Object> stringMap = Generic.map();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            stringMap.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return stringMap;
    }

    @Override
    public int hashCode() {
        return EqHc.hc(map.keySet());
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object == null) {
            return false;
        }
        MapPropertySet sp = EqHc.retyped(this, object);
        if (sp == null) {
            return false;
        }
        if (map.size() != sp.map.size()) {
            return false;
        }
        if (EqHc.eq(map, sp.map)) {
            return true;
        }
        if (!map.keySet().equals(sp.map.keySet())) {
            return false;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object spVal = sp.map.get(entry.getKey());
            Object val = entry.getValue();
            if (val.getClass().isArray() && spVal.getClass().isArray()) {
                int len = Array.getLength(val);
                int spLen = Array.getLength(spVal);
                if (len != spLen) {
                    return false;
                }
                for (int i = 0; i < len; i++) {
                    if (!Array.get(val, i).equals(Array.get(spVal, i))) {
                        return false;
                    }
                }
            } else if (!val.equals(spVal)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return ToString.of(this, printMap(), "writable", isWritable());
    }
}
