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

import java.util.*;

/**
 * <P>A Properties interface that handles various additional functionality, which
 * otherwise tends to heap up randomly in users of {@link Properties} objects:</P>
 *
 * <UL>
 * <LI>Type checking and string/value conversion</LI>
 * <LI>Variable substitution</LI>
 * <LI>Mutable and immutable copies</LI>
 * </UL>
 *
 * <P>The main use case here is importing string representations of properties,
 * possibly substitutng varable references, predictably interpreting resulting
 * strings as typed data, and possible writing them back uot.
 */
public interface PropertySet extends Iterable<String> {

    /**
     * Set value iff condition is true. Mutates the instance, or returns
     * a new instance if this instance is not
     * {@link #isWritable()} writable}.
     *
     * @param condition Condition
     * @param key       Key
     * @param value     Value
     * @return this instance
     * @throws IllegalStateException If immutable
     */
    PropertySet setIf(boolean condition, String key, Object value);

    /**
     * Set value. Mutates the instance, or returns a new instance if this instance is not
     * {@link #isWritable()} writable}.
     *
     * @param key   Key
     * @param value Value
     * @return This instance, or a writable copy with the new key/value pair
     */
    PropertySet set(String key, Object value);

    /**
     * Get the value as a string. Non-string values are converted.
     *
     * @param key       Key
     * @param variables Replacement variables
     * @return String representation of value
     */
    String getString(String key, PropertySet... variables);

    /**
     * <P>Get the value.</P>
     *
     * <P>If replacement variables are given, e.g. the
     * value <code>"${foo}2"</code> will return the integer
     * 22 iff one of the replacement property sets contains
     * says that <code>foo</code> is 2.</P>
     *
     * @param key       Key
     * @param variables Replacement variables
     * @return Value
     */
    Object get(String key, PropertySet... variables);

    /**
     * Get typed value.
     *
     * @param type      Type
     * @param key       Key
     * @param variables Replacement variables
     * @return Typed value
     */
    <T> T get(Class<T> type, String key, PropertySet... variables);

    /**
     * Get parent property set, if {@link #hasParent() any}.
     *
     * @return Parent
     */
    PropertySet getParent();

    PropertySet expand(PropertySet... variables);

    boolean is(String variable, boolean truth);

    boolean has(String variable);

    boolean has(Class<?> type, String variable);

    /**
     * Returns a copy of this property set.  If the writable
     * argument is false, and this property set isn't writable
     * either, it is permitted to return itself.
     *
     * @param writable True iff copy is to be writable
     * @return Copy (or itself iff non-writable)
     */
    PropertySet copy(boolean writable);

    /**
     * True iff this property set is writable.
     *
     * @return Writable flag
     */
    boolean isWritable();

    /**
     * Return copy with the parent.
     *
     * @param parent Parent
     * @return Copy of this, with the new parent
     */
    PropertySet withParent(PropertySet parent);

    PropertySet withParent(PropertySet parent, boolean writable);

    PropertySet with(PropertySet additional);

    PropertySet with(PropertySet additional, boolean writable);

    /**
     * Create a copy of this property set, without its parent.
     * If this property set has no parent, returns a {@link #copy(boolean)}.
     *
     * @return A new asOrphan
     */
    PropertySet asOrphan();

    /**
     * Use the more non-mutating-sounding {@link #asOrphan()} instead.
     *
     * @deprecated
     * @return {@link #asOrphan()}
     */
    @SuppressWarnings({"UnusedDeclaration"}) @Deprecated PropertySet orphan();

    /**
     * Return true iff a parent is set.
     *
     * @return Parental status
     */
    boolean hasParent();

    String resolve(String str);

    Long getLong(String key, PropertySet... variables);

    Integer getInt(String key, PropertySet... variables);

    boolean isEmpty();

    Properties toProperties();

    Dictionary<String, Object> toDictionary(Object nullValue);

    Dictionary<String, Object> toDictionary(Object nullValue, boolean collapse);

    Hashtable<String, Object> toHashtable(Object nullValue);

    Hashtable<String, Object> toHashtable(Object nullValue, boolean collapse);

    Map<String, Object> toMap();

    Map<String, Object> toMap(boolean collapse);

    int size();

    Collection<String> getPropertyNames();
}
