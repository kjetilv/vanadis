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
package net.sf.vanadis.osgi;

import java.util.Iterator;

abstract class FiltersIterable implements Iterable<Boolean> {

    private final Filter[] filters;

    @SuppressWarnings({"AssignmentToCollectionOrArrayFieldFromParameter"})
        // All Filter arrays are constructed internally, and/or by varargs
    FiltersIterable(Filter[] filters) {
        this.filters = filters;
    }

    @Override
    public final Iterator<Boolean> iterator() {
        return newIterator(filters);
    }

    protected abstract Iterator<Boolean> newIterator(Filter[] filters);

}
