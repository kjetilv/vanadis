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

import java.util.Enumeration;
import java.util.Iterator;

/**
 * An iterable that takes an enumeration.
 */
public final class EnumerationIterable<E> implements Iterable<E> {

    private final Enumeration<E> enumeration;

    public static <E> EnumerationIterable<E> create(Enumeration<E> enumeration) {
        return new EnumerationIterable<E>(enumeration);
    }

    @SuppressWarnings({"RawUseOfParameterizedType", "unchecked"})
    public static EnumerationIterable<?> createGeneric(Enumeration enumeration) {
        return new EnumerationIterable<Object>(enumeration);
    }

    public EnumerationIterable(Enumeration<E> enumeration) {
        this.enumeration = enumeration;
    }

    @Override
    public Iterator<E> iterator() {
        return new EnumerationIterator<E>(enumeration);
    }
}
