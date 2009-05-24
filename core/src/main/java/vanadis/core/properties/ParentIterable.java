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

package vanadis.core.properties;

import vanadis.core.lang.TraverseIterable;

final class ParentIterable<T extends PropertySet> extends TraverseIterable<T> {

    private final Class<T> clazz;

    static <T extends PropertySet> ParentIterable<T> create(Class<T> clazz, T t) {
        return new ParentIterable<T>(clazz, t);
    }

    private ParentIterable(Class<T> clazz, T start) {
        super(start);
        this.clazz = clazz;
    }

    @Override
    protected T getNext(T current) {
        return clazz.cast(current.getParent());
    }
}
