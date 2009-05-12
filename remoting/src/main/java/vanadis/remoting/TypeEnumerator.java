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
package vanadis.remoting;

import vanadis.core.collections.Generic;
import vanadis.core.lang.EqHc;
import vanadis.core.lang.ToString;

import java.lang.reflect.Method;
import java.util.*;

class TypeEnumerator {

    private final Method[] methods;

    private final String type;

    private final int length;

    private static void addExtendedInterfacesTo(Class<?> type, Set<Method> methods) {
        Class<?>[] interfaces = type.getInterfaces();
        if (interfaces != null && interfaces.length > 0) {
            for (Class<?> other : interfaces) {
                methods.addAll(methodsOf(other));
                addExtendedInterfacesTo(other, methods);
            }
        }
    }

    private static Method[] sortedMethodList(Set<Method> set) {
        List<Method> list = Generic.list(set);
        Collections.sort(list, new MethodComparator());
        return list.toArray(new Method[list.size()]);
    }

    private static Collection<Method> methodsOf(Class<?> type) {
        return Arrays.asList(type.getMethods());
    }

    TypeEnumerator(Class<?> type) {
        this.type = type.getName();
        this.methods = sortedMethodList(methodSet(type));
        this.length = methods.length;
    }

    private static Set<Method> methodSet(Class<?> type) {
        Set<Method> methods = Generic.set();
        methods.addAll(methodsOf(type));
        addExtendedInterfacesTo(type, methods);
        return methods;
    }

    public int indexOf(Method method) {
        for (int i = 0; i < length; i++) {
            if (methods[i].equals(method)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Method not enumerated: " + method);
    }

    public Method method(int index) {
        return methods[validIndex(index, length)];
    }

    public static int validIndex(int index, int length) {
        if (index < 0) {
            throw new IllegalArgumentException("Invalid index: " + index);
        }
        if (index < length) {
            return index;
        }
        throw new IllegalArgumentException("Invalid index, length was " + length + ": " + index);

    }

    @Override
    public String toString() {
        return ToString.of(this, "type", type, "#methods", methods.length);
    }

    @Override
    public boolean equals(Object o) {
        TypeEnumerator enumerator = EqHc.retyped(this, o);
        return enumerator == this || enumerator != null && EqHc.eq(type, enumerator.type);
    }

    @Override
    public int hashCode() {
        return EqHc.hc(type);
    }
}

