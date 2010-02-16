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

package vanadis.core.reflection;

import vanadis.core.collections.Generic;
import vanadis.core.lang.Not;
import vanadis.core.reflection.Enums;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A static utility class that handles uniform value-to-string and string-to-value conversion.
 */
public final class Retyper {

    private static String nonNull(String string) {
        return Not.nil(string, "string to coerce");
    }

    public static Object coerceArray(String propertyType, String[] value) {
        return coerceRawArray(type(propertyType), value);
    }

    public static Object[] coerceArray(Class<?> propertyType, String[] value) {
        return (Object[])coerceRawArray(propertyType, value);
    }

    private static Object coerceRawArray(Class<?> propertyType, String[] value) {
        Object array = Array.newInstance(propertyType, value.length);
        for (int i = 0; i < value.length; i++) {
            Array.set(array, i, coerce(propertyType, value[i]));
        }
        return array;
    }

    private static final Map<Class<?>, Coercer<?>> coercers = Generic.map();

    private static final Map<String, Class<?>> coercedTypeNames = Generic.map();

    /**
     * True iff the class coercable, i.e. we have a {@link Retyper.Coercer coercer}
     * for it.
     *
     * @param clazz Class
     * @return True if we can convert and coerce it
     */
    public static boolean isMappable(Class<?> clazz) {
        return getCoercer(Not.nil(clazz, "clazz"), false) != null;
    }

    /**
     * Iterable over the set of coercable classes.
     *
     * @return Coercable classes
     */
    public static Iterable<Class<?>> mappableClasses() {
        return Collections.unmodifiableCollection(Generic.set(coercers.keySet()));
    }

    static {
        map(new IdentityCoercer());
        map(new ObjectNameCoercer());
        map(new FileCoercer());
        map(new URLCoercer());
        map(new URICoercer());
        map(new LongCoercer(), long.class);
        map(new IntegerCoercer(), int.class);
        map(new ShortCoercer(), short.class);
        map(new DoubleCoercer(), double.class);
        map(new ByteCoercer(), byte.class);
        map(new BooleanCoercer(), boolean.class);
        map(new FloatCoercer(), float.class);
    }

    public static String nameOf(Class<?> clazz) {
        for (Map.Entry<String, Class<?>> entry : coercedTypeNames.entrySet()) {
            if (clazz.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static void map(Coercer<?> coercer, Class<?>... otherTypes) {
        doMap(null, coercer);
        for (Class<?> other : otherTypes) {
            doMap(other, coercer);
        }
    }

    public static void doMap(Class<?> clazz, Coercer<?> coercer) {
        Class<?> type = clazz == null ? coercer.coercedType() : clazz;
        if (type.isInterface() && Modifier.isAbstract(type.getModifiers())) {
            throw new IllegalArgumentException("Unable to map non-concrete " + type);
        }
        if (getCoercer(type, false) != null) {
            throw new IllegalArgumentException("Already mapping " + type);
        }
        storeTypeName(type.getName().toLowerCase(), type);
        storeTypeName(type.getSimpleName().toLowerCase(), type);
        coercers.put(type, coercer);
    }

    private static void storeTypeName(String name, Class<?> type) {
        if (!coercedTypeNames.containsKey(name)) {
            coercedTypeNames.put(name, type);
        }
    }

    public static String toString(Object object) {
        Class<?> clazz = Not.nil(object, "object").getClass();
        Coercer<?> coercer = getCoercer(clazz, false);
        return coercer == null ? object.toString() : coerce(object, coercer);
    }

    public static Class<?> type(String type) {
        return coercedTypeNames.get(type.toLowerCase());
    }

    public static Object coerce(String type, Object arg) {
        Not.nil(type, "type");
        Not.nil(arg, "arg");
        Class<?> realType = type(type);
        if (realType == null) {
            throw new IllegalArgumentException
                    ("Unknown type name '" + type + "' to convert value '" + arg + "'");
        }
        return arrayOrSingleCoerce(realType, arg);
    }

    public static <T> T coerceSingle(Class<T> type, Object arg) {
        if (arg.getClass().isArray()) {
            throw new IllegalArgumentException("Array input for single coerce to " + type + ": " + toString(arg));
        }
        if (type == Object.class) {
            return type.cast(arg);
        }
        Coercer<T> coercer = Retyper.getCoercer(type, true);
        return toInstance(type, arg, coercer);
    }

    public static <T> Object coerce(Class<T> type, Object arg) {
        Not.nil(type, "type");
        Not.nil(arg, "arg");
        return arrayOrSingleCoerce(type, arg);
    }

    @SuppressWarnings({"unchecked"})
    private static <T> Coercer<T> getCoercer(Class<T> clazz, boolean required) {
        if (Enum.class.isAssignableFrom(clazz)) {
            return new EnumCoercer(clazz);
        }
        Coercer<?> coercer = coercers.get(clazz);
        if (coercer != null) {
            return (Coercer<T>) coercer;
        }
        for (Class<?> mapped : coercers.keySet()) {
            if (mapped.isAssignableFrom(clazz)) {
                return (Coercer<T>) coercers.get(mapped);
            }
        }
        if (required) {
            throw new IllegalArgumentException("Not mapped: " + clazz);
        }
        return null;
    }

    private static <T> String coerce(Object object, Coercer<T> coercer) {
        return coercer.toString(coercer.coercedType().cast(object));
    }

    private static <T> Object arrayOrSingleCoerce(Class<T> type, Object arg) {
        if (type.isInstance(arg)) {
            return type.cast(arg);
        }
        return type.isArray()
                ? toArray((Class<T>) type.getComponentType(), arg, getCoercer((Class<T>) type.getComponentType(), true))
                : toInstance(type, arg, getCoercer(type, true));
    }

    private static <T, A> T toInstance(Class<T> type, A arg, Coercer<T> coercer) {
        Coercer<A> argCoercer = Retyper.getCoercer((Class<A>) arg.getClass(), false);
        String string = nonNull(argCoercer == null ? String.valueOf(arg).trim()
                : argCoercer.toString(arg));
        try {
            return coercer.coerce(string);
        } catch (Exception e) {
            return failCoercion(type, arg, e);
        }
    }

    private static <T> T failCoercion(Class<T> type, Object arg, Throwable e) {
        throw new IllegalArgumentException
                ("Unable to coerce object '" + arg + "' of " + arg.getClass() +
                        " into instance of " + type, e);
    }

    private static <T> Object toArray(Class<T> type, Object arg, Coercer<T> coercer) {
        if (arg.getClass().isArray()) {
            int length = Array.getLength(arg);
            Object targetArray = Array.newInstance(type, length);
            for (int i = 0; i < length; i++) {
                T t = toInstance(type, Array.get(arg, i), coercer);
                Array.set(targetArray, i, t);
            }
            return targetArray;
        } else if (Collection.class.isInstance(arg)) {
            Collection<?> collection = (Collection<?>) arg;
            int length = collection.size();
            Object targetArray = Array.newInstance(type, length);
            int i = 0;
            for (Object object : collection) {
                T t = toInstance(type, object, coercer);
                Array.set(targetArray, i, t);
            }
            return targetArray;
        } else {
            String string = String.valueOf(arg);
            List<?> instances;
            try {
                instances = coercer.coerceMulti(nonNull(string));
            } catch (Exception e) {
                return failCoercion(type, arg, e);
            }
            Object array = Array.newInstance(type, instances.size());
            return instances.toArray((Object[]) array);
        }
    }

    /**
     * SPI for coercion.  Register instances for new types using
     * {@link Retyper#map(Retyper.Coercer, Class[])}
     */
    public interface Coercer<T> {

        Class<T> coercedType();

        List<T> coerceMulti(String string);

        T coerce(String string);

        String toString(T t);
    }

    private static class FloatCoercer extends AbstractCoercer<Float> {

        @Override
        public Float coerce(String string) {
            return Float.parseFloat(nonNull(string));
        }
    }

    private static class BooleanCoercer extends AbstractCoercer<Boolean> {

        @Override
        public Boolean coerce(String string) {
            return Boolean.parseBoolean(nonNull(string));
        }
    }

    private static class ByteCoercer extends AbstractCoercer<Byte> {

        @Override
        public Byte coerce(String string) {
            return Byte.parseByte(nonNull(string));
        }
    }

    private static class DoubleCoercer extends AbstractCoercer<Double> {

        @Override
        public Double coerce(String string) {
            return Double.parseDouble(nonNull(string));
        }
    }

    private static class ShortCoercer extends AbstractCoercer<Short> {

        @Override
        public Short coerce(String string) {
            return Short.parseShort(nonNull(string));
        }
    }

    private static class IntegerCoercer extends AbstractCoercer<Integer> {

        @Override
        public Integer coerce(String string) {
            return Integer.parseInt(nonNull(string));
        }
    }

    private static class LongCoercer extends AbstractCoercer<Long> {

        @Override
        public Long coerce(String string) {
            return Long.parseLong(nonNull(string));
        }
    }

    private static class URLCoercer extends AbstractCoercer<URL> {

        @Override
        public URL coerce(String string) {
            try {
                return new URL(nonNull(string));
            } catch (MalformedURLException e) {
                return failCoercion(URL.class, string, e);
            }
        }
    }

    private static class URICoercer extends AbstractCoercer<URI> {

        @Override
        public URI coerce(String string) {
            try {
                return new URI(nonNull(string));
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Failed to parse to URI : + string", e);
            }
        }
    }

    private static class FileCoercer extends AbstractCoercer<File> {

        @Override
        public File coerce(String string) {
            return new File(nonNull(string));
        }
    }

    private static class ObjectNameCoercer extends AbstractCoercer<ObjectName> {

        @Override
        public ObjectName coerce(String string) {
            try {
                return new ObjectName(nonNull(string));
            } catch (MalformedObjectNameException e) {
                throw new IllegalArgumentException("Not a passable object name: " + string, e);
            }
        }

        @Override
        public String toString(ObjectName objectName) {
            return objectName.getCanonicalName();
        }
    }

    private static class IdentityCoercer extends AbstractCoercer<String> {

        @Override
        public String coerce(String string) {
            return string;
        }
    }

    @SuppressWarnings({"RawUseOfParameterizedType"})
    // So it is discovered by the superclass constructor!
    private static class EnumCoercer<E extends Enum<?>> extends AbstractCoercer<Enum> {

        private final Class<E> enumType;

        private EnumCoercer(Class<E> enumType) {
            this.enumType = enumType;
        }

        @Override
        public Enum<?> coerce(String string) {
            return enumType.cast(Enums.get(enumType, string));
        }
    }

    private Retyper() { }
}
