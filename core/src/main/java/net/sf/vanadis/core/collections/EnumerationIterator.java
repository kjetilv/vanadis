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
import java.util.NoSuchElementException;

/**
 * An iterator that walks over an Enumeration.
 */
public final class EnumerationIterator<T> implements Iterator<T> {

    private final Enumeration<T> enumeration;

    public static <T> EnumerationIterator<T> create(Enumeration<T> enumeration) {
        return new EnumerationIterator<T>(enumeration);
    }

    @SuppressWarnings({"RawUseOfParameterizedType", "unchecked"})
    public static EnumerationIterator<?> createGeneric(Enumeration enumeration) {
        return new EnumerationIterator<Object>(enumeration);
    }

    public EnumerationIterator(Enumeration<T> enumeration) {
        this.enumeration = enumeration;
    }

    @Override
    public boolean hasNext() {
        return this.enumeration != null && this.enumeration.hasMoreElements();
    }

    @Override
    public T next() {
        if (enumeration == null) {
            throw new NoSuchElementException(this + ": Null enumeration");
        }
        return this.enumeration.nextElement();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
