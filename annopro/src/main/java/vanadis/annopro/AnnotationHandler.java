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

package vanadis.annopro;

import vanadis.core.lang.ToString;
import vanadis.core.reflection.Retyper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

@SuppressWarnings({"unchecked"})
class AnnotationHandler implements InvocationHandler {

    private final ClassLoader classLoader;

    private final AnnotationDatum<?> datum;

    AnnotationHandler(ClassLoader classLoader, AnnotationDatum<?> datum) {
        this.classLoader = classLoader;
        this.datum = datum;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
        String propertyName = method.getName();
        Class<?> returnType = method.getReturnType();
        return get(propertyName, returnType);
    }

    public Object get(String propertyName, Class<?> returnType) {
        Object value = datum.getPropertySet().get(propertyName);
        return value == null ? null
            : returnType.isInstance(value) ? value
                : isAnnotation(returnType) ? nestedAnnotationDatum(propertyName, value, returnType)
                    : isAnnotationArray(returnType) ? nestedAnnotationData(propertyName, value, returnType)
                        : typedReturnValue(propertyName, value, returnType);
    }

    private static boolean isAnnotationArray(Class<?> returnType) {
        return returnType.isArray() && isAnnotation(returnType.getComponentType());
    }

    private Object typedReturnValue(String name, Object value, Class<?> returnType) {
        try {
            return Retyper.coerce(returnType, value);
        } catch (Exception e) {
            throw new IllegalStateException
                    ("Could not return property " + name + " using " + datum +
                            ", problematic string value for type " +
                            returnType + ": '" + value + "'", e);
        }
    }

    private Object nestedAnnotationData(String name, Object value, Class<?> returnType) {
        if (value instanceof Collection<?>) {
            return nestedProxies(value, (Class<Annotation>) returnType.getComponentType());
        } else {
            throw new IllegalStateException
                    ("Could not return annotation array property " + name + " using " + datum +
                            ", expected instance of " + AnnotationDatum.class +
                            ": " + ToString.ofObjectOrArray(value));
        }
    }

    private Object nestedAnnotationDatum(String name, Object value, Class<?> returnType) {
        if (value instanceof AnnotationDatum<?>) {
            return nestedProxy(value, (Class<Annotation>) returnType);
        } else {
            throw new IllegalStateException
                    ("Could not return annotation property " + name + " using " + datum +
                            ", expected instance of " + AnnotationDatum.class + ": " + value);
        }
    }

    private <A extends Annotation> Object nestedProxies(Object value, Class<A> returnType) {
        List<AnnotationDatum<?>> data = (List<AnnotationDatum<?>>) value;
        int length = data.size();
        Object array = Array.newInstance(returnType, length);
        for (int i = 0; i < length; i++) {
            Array.set(array, i, nestedProxy(data.get(i), returnType));
        }
        return array;
    }

    private static boolean isAnnotation(Class<?> returnType) {
        return Annotation.class.isAssignableFrom(returnType);
    }

    private <A extends Annotation> A nestedProxy(Object value, Class<A> returnType) {
        AnnotationDatum<?> datum = (AnnotationDatum<?>) value;
        return datum.createProxy(classLoader, returnType);
    }

    @Override
    public String toString() {
        return ToString.of(this, datum);
    }
}
