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

package vanadis.annopro;

import vanadis.core.lang.Proxies;
import vanadis.core.properties.PropertySet;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Test;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class AnnotationsDigestTest {

    private static final ClassLoader CLASS_LOADER = AnnotationsDigestTest.class.getClassLoader() == null
            ? ClassLoader.getSystemClassLoader()
            : AnnotationsDigestTest.class.getClassLoader();
    private static final long[] NO_LONGS = new long[] {};

    @Test
    public void readInheritedClassAnnotation() {
        AnnotationsDigest digest = AnnotationsDigests.createFullFromType(ModestAnnotatedType.class);
        AnnotationDatum<Class<?>> annotation = digest.getClassDatum(Foo.class);
        assertNotNull("Did not detect inherited annotation " + Foo.class.getSimpleName() +
                " on extending " + ModestAnnotatedType.class, annotation);
    }

    @Test
    public void readInheritedMethodAnnotation() {
        AnnotationsDigest digest = AnnotationsDigests.createFullFromType(ModestAnnotatedType.class);
        List<AnnotationDatum<Method>> as = digest.getMethodData(Bar.class);
        assertEquals("Did not detect annotation " + Bar.class.getSimpleName() +
                " on overriden method in " + ModestAnnotatedType.class,
                     1, as.size());
        Method method;
        try {
            method = ModestAnnotatedType.class.getMethod("method", String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        Iterable<AnnotationDatum<Method>> iterable = digest.methodData(method);
        assertNotNull("Did not detect inherited annotation " + Bar.class.getSimpleName() +
                " on overridden method in " + method,
                      iterable);
        assertTrue(iterable.iterator().hasNext());
    }

    @Test
    public void readExtendedClassAnnotations() {
        AnnotationsDigest digest = AnnotationsDigests.createFullFromType(ExtendedAnotherAnnotatedTypeImpl.class);
        AnnotationDatum<Class<?>> as = digest.getClassDatum(Foo.class);
        assertNotNull(as);
        PropertySet propertySet = as.getPropertySet();
        assertNotNull(propertySet);
        Assert.assertEquals(1, propertySet.size());
        assertTrue(propertySet.has("value"));
        Assert.assertEquals("extend-interface-impl", propertySet.get("value"));
    }

    @Test
    public void readImplementingMethodAnnotions() {
        assertBarValues(2, AnotherAnnotatedTypeImpl.class, Bar.class, "interface-impl", 2);
    }

    @Test
    public void readInterfaceMethodAnnotions() {
        assertBarValues(1, AnotherAnnotatedType.class, Bar.class, "interface", 1);
    }

    @Test
    public void readSimpleMethodAnnotions() {
        assertBarValues(1, AnnotatedType.class, Bar.class, "simple", 1);
    }

    private static InputStream stream(Class<?> type) {
        return CLASS_LOADER.getResourceAsStream(type.getName().replace(".", "/") + ".class");
    }

    private static PropertySet assertBarValues(int count,
                                               Class<?> type,
                                               Class<? extends Annotation> aType,
                                               String value,
                                               int zip) {
        AnnotationsDigest digest = AnnotationsDigests.createFullFromType(type);
        return assertBarValues(digest, count, aType, value, zip);
    }

    private static PropertySet assertBarValues(AnnotationsDigest digest, int count,
                                               Class<? extends Annotation> aType,
                                               String value, int zip) {
        List<AnnotationDatum<Method>> as = digest.getMethodData(aType);
        assertEquals(count, as.size());
        PropertySet propertySet = as.get(0).getPropertySet();
        assertNotNull(propertySet);
        Assert.assertEquals(2, propertySet.size());
        assertTrue(propertySet.has("zot"));
        Assert.assertEquals(value, propertySet.get("zot"));
        assertTrue(propertySet.has("zip"));
        Assert.assertEquals((Integer) zip, propertySet.getInt("zip"));
        return propertySet;
    }

    @Test
    public void readAllMethods() {
        AnnotationsDigest digest = AnnotationsDigests.createFullFromType(ExtendedAnotherAnnotatedTypeImpl.class);
        List<AnnotationDatum<Method>> as = digest.getMethodData(Bar.class);
        assertEquals(3, as.size());
    }

    @Test
    public void readAllFields() {
        AnnotationsDigest digest = AnnotationsDigests.createFullFromType(ExtendedAnotherAnnotatedTypeImpl.class);
        List<AnnotationDatum<Field>> as = digest.getFieldData(Baz.class);
        assertNotNull(as);
        assertEquals(2, as.size());
    }

    @Test
    public void digest() {
        AnnotationsDigest digest = AnnotationsDigests.createFullFromInstance(new ExtendedAnotherAnnotatedTypeImpl());
        Bar bar = digest.getMethodData(Bar.class).get(0).createProxy(classLoader(), Bar.class);
        assertNotNull("Found no bar", bar);
        assertNotNull(bar.zot());
        assertEquals("extend-interface-impl", bar.zot());
        assertEquals(3, bar.zip());

        Baz baz = digest.getFieldData(Baz.class).get(0).createProxy(classLoader(), Baz.class);
        assertNotNull("Found no baz");
        assertNotNull(baz.zot());
        assertEquals(baz.zot(), "extends-field");
        assertEquals(3, baz.zip());
    }

    private static ClassLoader classLoader() {
        return AnnotationsDigestTest.class.getClassLoader();
    }

    @Test
    public void noOverrides() {
        AnnotationsDigest digest = AnnotationsDigests.createFromType(AnnotatedConcreteClass.class);
        assertFalse(digest.classData().iterator().hasNext());
        assertTrue(digest.getMethodData(Bar.class).isEmpty());
        assertTrue(digest.getMethodData(Bar2.class).isEmpty());
    }

    @Test
    public void oneOverride() {
        assertInherited(AnnotationsDigests.createFullFromType(AbstractAnnotatedClass.class));
    }

    @Test
    public void twoOverride() {
        assertInherited(AnnotationsDigests.createFullFromType(AnnotatedConcreteClass.class));
    }

    private static void assertInherited(AnnotationsDigest digest) {
        assertCount(2, digest.classData());
        Assert.assertEquals(1, digest.getMethodData(Bar.class).size());
        Assert.assertEquals(1, digest.getMethodData(Bar2.class).size());
    }

    @Test
    public void withOverrides() {
        AnnotationsDigest digest = AnnotationsDigests.createFullFromType(AnnotatedConcreteClass.class);
        Iterable<AnnotationDatum<Class<?>>> it = digest.classData();
        assertCount(2, it);
    }

    private static void assertCount(int count, Iterable<?> iterable) {
        Iterator<?> it = iterable.iterator();
        for (int i = 0; i < count; i++) {
            assertTrue("Expected " + count + ", stopped at " + i + ": " + iterable, it.hasNext());
            it.next();
        }
        assertFalse("Too many items, expected " + count + " in " + iterable, it.hasNext());
    }

    @Test
    public void nestedAnnotated() {
        assertNestedAnnotated(TheNestingType.class, null);
    }

    @Test
    public void nestedAnnotatedFromStream() {
        assertNestedAnnotated(null, stream(TheNestingType.class));
    }

    private static void assertNestedAnnotated(Class<?> nestingType, InputStream is) {
        Nest nest = denest(nestingType, is, NestType.NORMAL);
        Nestsed[] nestseds = nest.arrayNested();
        assertNotNull("No nested array", nestseds);
        assertValues(nestseds, new String[]{ "level1.0" });
        assertNestedData(nest, "level0", NO_LONGS);
    }

    @Test
    public void complexNestedAnnotated() {
        assertComplexNestedAnnotated(TheBadlyNestingType.class, null);
    }

    @Test
    public void complexNestedAnnotatedFromStream() {
        assertComplexNestedAnnotated(null, stream(TheBadlyNestingType.class));
    }

    private static void assertComplexNestedAnnotated(Class<?> nestingType, InputStream is) {
        Nest nest = denest(nestingType, is, NestType.COMPLEX);
        Nestsed[] nestseds = nest.arrayNested();
        assertNotNull(nestseds);
        assertValues(nestseds, new String[]{ "level1.0", "level1.1", "level1.2" });
        assertNestedData(nest, "level0", new long[] { 1L, 2L });
    }

    private static void assertValues(Nestsed[] nestseds, String[] strings) {
        assertEquals("Expected " + Arrays.toString(strings) + " got " + Arrays.toString(nestseds),
                     strings.length, nestseds.length);
        for (int i = 0; i < nestseds.length; i++) {
            assertNotNull(nestseds[i]);
            assertHandledProxy(nestseds[i]);
            assertEquals(strings[i], nestseds[i].value());
        }
    }

    private static Nest denest(Class<?> nestingType, InputStream is, NestType type) {
        AnnotationsDigest digest = is == null
                ? AnnotationsDigests.createFullFromType(nestingType)
                : AnnotationsDigests.createFromStream(is);
        AnnotationDatum<Class<?>> annotation = digest.getClassDatum(Nest.class);
        assertNotNull("No annotation", annotation);
        Nest nest = annotation.createProxy(classLoader(), Nest.class);
        assertHandledProxy(nest);
        assertEquals(type, nest.type());
        return nest;
    }

    @Test
    public void simpleNestedAnnotated() {
        assertSimpleNestedAnnotated(null, TheSimpleNestingType.class);
    }

    @Test
    public void simpleNestedAnnotatedFromStream() {
        assertSimpleNestedAnnotated(stream(TheSimpleNestingType.class), null);
    }

    private static void assertSimpleNestedAnnotated(InputStream is, Class<TheSimpleNestingType> nestingType) {
        Nest nest = denest(nestingType, is, NestType.SIMPLE);
        assertNotNull(nest.nested());
        assertNestedData(nest, "level0", NO_LONGS);
    }

    private static void assertNestedData(Nest nest, String nestedValue, long[] nestedLongs) {
        Nested nested = nest.nested();
        assertHandledProxy(nested);
        assertNotNull(nested.value());
        assertEquals(nestedValue, nested.value());
        assertTrue(Arrays.equals(nestedLongs, nested.longs()));
    }

    private static void assertHandledProxy(Object object) {
        assertNotNull("Expected " + AnnotationHandler.class + ": " + Proxy.getInvocationHandler(object),
                      Proxies.handler(AnnotationHandler.class, object));
    }

    @Test
    public void singleValue() {
        AnnotationsDigest digest = AnnotationsDigests.createFullFromType(AnnotatedType.class);
        assertNotNull(digest.getClassDatum(Foo.class).getValue());
        assertEquals("simpleType", digest.getClassDatum(Foo.class).getValue());
    }
}
