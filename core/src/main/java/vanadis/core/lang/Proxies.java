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

package vanadis.core.lang;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * Factory utility class for creating a proxy with genericized return type.
 */
public final class Proxies {

    public static <T> T genericProxy(ClassLoader classLoader, Class<T> type, InvocationHandler handler,
                                     Class<?>... moreClasses) {
        Not.nil(type, "Generic proxy type");
        ClassLoader loader = classLoader == null ? ClassLoader.getSystemClassLoader()
                : classLoader;
        Class<?>[] types = assembleTypes(type, moreClasses);
        Object object = newProxyInstance(handler, loader, types);
        assert type.isInstance(object) : "Expected proxy to be instance of " + type + ": " + object;
        return type.cast(object);
    }

    private static Object newProxyInstance(InvocationHandler handler, ClassLoader loader, Class<?>[] types) {
        try {
            return Proxy.newProxyInstance(loader, types, handler);
        } catch (Exception e) {
            throw new IllegalArgumentException
                    ("Failed to create instance of " + Arrays.toString(types) + " from " + loader +
                            ", handler: " + handler, e);
        }
    }

    private static <T> Class<?>[] assembleTypes(Class<T> genericType, Class<?>... otherTypes) {
        if (!VarArgs.present(otherTypes)) {
            return new Class<?>[]{ genericType };
        }
        return alreadyListed(genericType, otherTypes) ? otherTypes : combinedTypes(genericType, otherTypes);
    }

    private static boolean alreadyListed(Class<?> type, Class<?>... types) {
        for (Class<?> listedType : types) {
            if (listedType.equals(type)) {
                return true;
            }
        }
        return false;
    }

    private static Class<?>[] combinedTypes(Class<?> type, Class<?>... moreClasses) {
        Class<?>[] classes = new Class<?>[moreClasses.length + 1];
        classes[0] = type;
        System.arraycopy(moreClasses, 0, classes, 1, classes.length);
        return classes;
    }

    public static <T extends InvocationHandler> T handler(Class<T> type, Object object) {
        if (Proxy.isProxyClass(object.getClass())) {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(object);
            return type.isInstance(invocationHandler) ? type.cast(invocationHandler) : null;
        }
        return null;
    }

    private Proxies() { }
}
