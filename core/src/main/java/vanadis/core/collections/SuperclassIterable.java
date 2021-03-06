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

import vanadis.core.lang.TraverseIterable;

public final class SuperclassIterable extends TraverseIterable<Class<?>> {

    private final boolean includeObject;

    public SuperclassIterable(Class<?> start) {
        this(start, true, true);
    }

    public SuperclassIterable(Class<?> start, boolean includeStart) {
        this(start, includeStart, true);
    }

    public SuperclassIterable(Class<?> start, boolean includeStart, boolean includeObject) {
        super(includeStart ? start : start.getSuperclass());
        this.includeObject = includeObject;
    }

    @Override
    protected Class<?> getNext(Class<?> current) {
        Class<?> superclass = current.getSuperclass();
        if (superclass == Object.class) {
            return includeObject ? Object.class : null;
        }
        return superclass;
    }
}
