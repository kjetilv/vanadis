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
package vanadis.remoting;

import vanadis.core.collections.Generic;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.Collections;
import java.util.Map;

class ThreadContextClassLoaderObjectInputStream extends ObjectInputStream {

    private static final Map<String, Class<?>> PRIMITIVES = Collections.<String, Class<?>>unmodifiableMap
            (Generic.map("boolean", boolean.class,
                         "byte", byte.class,
                         "char", char.class,
                         "double", double.class,
                         "float", float.class,
                         "int", int.class,
                         "long", long.class,
                         "short", short.class));

    ThreadContextClassLoaderObjectInputStream(InputStream inputStream)
            throws IOException {
        super(inputStream);
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass osc)
            throws IOException, ClassNotFoundException {
        String name = osc.getName();
        return PRIMITIVES.containsKey(name) ? PRIMITIVES.get(name)
                : Class.forName(name, false, Thread.currentThread().getContextClassLoader());
    }

}