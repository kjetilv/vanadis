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
import vanadis.core.reflection.Retyper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.*;

public abstract class AbstractPropertySet implements PropertySet, Serializable {

    private static final String NULL_STRING = "NULL";

    private static final Object NULL = new Serializable() {
        private static final long serialVersionUID = 0L;
        @Override
        public String toString() {
            return NULL_STRING;
        }
    };

    private final PropertySet parent;

    private final boolean writable;

    private static final long serialVersionUID = 1L;

    protected AbstractPropertySet(PropertySet parent, boolean writable) {
        this.parent = parent;
        this.writable = writable;
    }

    @Override
    public final PropertySet copy(boolean writable) {
        if (!this.writable && !writable) {
            return this;
        }
        return makeCopy(parent, writable);
    }

    protected abstract PropertySet makeCopy(PropertySet parent, boolean writable);

    @Override
    public final boolean isWritable() {
        return writable;
    }

    @Override
    public PropertySet with(PropertySet additional) {
        return with(additional, writable);
    }

    @Override
    public PropertySet with(PropertySet additional, boolean writable) {
        PropertySet copy = makeCopy(null, true);
        for (String key : additional) {
            copy.set(key, additional.get(key));
        }
        return copy;
    }

    @Override
    public PropertySet withParent(PropertySet parent) {
        return withParent(parent, writable);
    }

    @Override
    public final PropertySet withParent(PropertySet parent, boolean writable) {
        if (parent == null || parent instanceof AbstractPropertySet) {
            return makeCopy(parent, writable);
        }
        throw new IllegalArgumentException
                (this + " can only chain with subclasses of " + AbstractPropertySet.class +
                        ", received " + parent + " of " + parent.getClass());
    }

    @Override
    public PropertySet asOrphan() {
        return hasParent() ? doOrphan() : copy(isWritable());
    }

    /**
     * @deprecated
     * @return {@link #asOrphan()}
     */
    @Override
    @Deprecated
    public final PropertySet orphan() {
        return asOrphan();
    }

    protected abstract AbstractPropertySet doOrphan();

    @Override
    public final boolean has(String variable) {
        Object object = retrieve(variable);
        return object != null;
    }

    @Override
    public final boolean hasParent() {
        return parent != null;
    }

    @Override
    public final PropertySet getParent() {
        return parent;
    }

    @Override
    public final String getString(String key, PropertySet... variables) {
        return has(key) ? Retyper.toString(get(key, variables)) : null;
    }

    @Override
    public final boolean has(Class<?> type, String variable) {
        Object object = get(variable);
        return object != null && type.isInstance(object);
    }

    @Override
    public final String resolve(String str) {
        return Resolve.resolve(str, this);
    }

    @Override
    public Properties toProperties() {
        return new Properties();
    }

    @Override
    public Hashtable<String, Object> toHashtable(Object nullValue) {
        return toHashtable(nullValue, true);
    }

    @Override
    public Hashtable<String, Object> toHashtable(Object nullValue, boolean collapse) {
        return Generic.emptyHashtable();
    }

    @Override
    public Dictionary<String, Object> toDictionary(Object nullValue) {
        try {
            return toDictionary(nullValue, true);
        } catch (Exception e) {
            throw new IllegalStateException(this + " failed to turn into dictionary!", e);
        }
    }

    @Override
    public Dictionary<String, Object> toDictionary(Object nullValue, boolean collapse) {
        return Generic.emptyDictionary();
    }

    @Override
    public Map<String, Object> toMap() {
        return toMap(true);
    }

    @Override
    public Map<String, Object> toMap(boolean collapse) {
        return Collections.emptyMap();
    }

    @Override
    public AbstractPropertySet setIf(boolean condition, String key, Object value) {
        if (condition) {
            set(key, value);
        }
        return this;
    }

    @Override
    public final Iterator<String> iterator() {
        if (parent == null) {
            return keySet().iterator();
        }
        Set<String> keys = Generic.set();
        for (AbstractPropertySet set : lineage()) {
            keys.addAll(set.getPropertyNames());
        }
        return keys.iterator();
    }

    protected Set<String> keySet() {
        return Collections.emptySet();
    }

    @Override
    public final Integer getInt(String key, PropertySet... variables) {
        return get(Integer.class, key, variables);
    }

    @Override
    public final Long getLong(String key, PropertySet... variables) {
        return get(Long.class, key, variables);
    }

    @Override
    public final boolean is(String variable, boolean truth) {
        Object object = get(variable);
        return object instanceof Boolean && (Boolean) object == truth;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    protected abstract Object getLocal(String key);

    @Override
    public final Object get(String key, PropertySet... variables) {
        return get(Object.class, key, variables);
    }

    @Override
    public final <T> T get(Class<T> clazz, String key, PropertySet... variables) {
        Object local = retrieve(key);
        return local == null ? null : process(clazz, local, variables);
    }

    private Object retrieve(String key) {
        for (PropertySet set : lineage()) {
            Object local = ((AbstractPropertySet) set).getLocal(key);
            if (local != null) {
                return local;
            }
        }
        return null;
    }

    private ParentIterable<AbstractPropertySet> lineage() {
        return ParentIterable.create(AbstractPropertySet.class, this);
    }

    private static <T> T process(Class<T> type, Object object, PropertySet... variables) {
        if (object == NULL) {
            return null;
        }
        if (object instanceof String) {
            String sourceString = object.toString();
            String string = plainValue(sourceString) ? sourceString
                    : Resolve.resolve(sourceString, variables);
            return type == String.class ? type.cast(string)
                    : Retyper.coerceSingle(type, string);
        }
        if (type.isInstance(object)) {
            return type.cast(object);
        }
        if (Number.class.isAssignableFrom(type) && object instanceof Number) {
            return Retyper.coerceSingle(type, object.toString());
        }
        throw new IllegalArgumentException
                ("Failed to coerce " + object + " of " + object.getClass() + " to " + type);
    }

    private static boolean plainValue(String sourceString) {
        return !sourceString.contains("${");
    }

    @Override
    public PropertySet set(String key, Object value) {
        Object val = value == null ? NULL : value;
        return writable ? setLocal(key, val)
                : copy(true).set(key, val);
    }

    protected abstract AbstractPropertySet setLocal(String key, Object value);

    public AbstractPropertySet writeProperties(OutputStream stream, String encoding) {
        Properties properties = toProperties();
        try {
            properties.store(stream, encoding);
        } catch (IOException e) {
            throw new IllegalArgumentException(this + " failed to write to " + stream, e);
        }
        return this;
    }
}
