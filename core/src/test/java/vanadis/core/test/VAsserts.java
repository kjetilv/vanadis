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
package vanadis.core.test;

import junit.framework.Assert;
import vanadis.core.lang.AccessibleHelper;
import vanadis.core.lang.EqHc;
import vanadis.core.lang.Not;
import vanadis.core.lang.VarArgs;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class VAsserts {

    public static boolean hasSameBehavior(Iterable<?> c1, Iterable<?> c2) {
        Iterator<?> it1 = c1.iterator();
        Iterator<?> it2 = c2.iterator();
        while (it1.hasNext()) {
            if (!it2.hasNext()) {
                return false;
            }
            Object o1 = it1.next();
            Object o2 = it2.next();
            if (!VAsserts.equal(o1, o2)) {
                return false;
            }
        }
        return !it2.hasNext();
    }

    public static boolean equal(Object o1, Object o2) {
        return EqHc.eq(o1, o2);
    }

    public static <T> boolean equalState(T o1, T o2) {
        return equalState(o1, o2, Collections.<Field>emptySet());
    }

    public static <T> boolean equalState(T o1, T o2, Set<Field> fieldsToExclude) {
        if (o1 == null) {
            return o2 == null;
        }
        for (Class<?> type = o1.getClass(); type.getSuperclass() != Object.class; type = type.getSuperclass()) {
            if (!equalState(o1, o2, type, fieldsToExclude)) {
                return false;
            }
        }
        return true;
    }

    private static boolean equalState(Object o1, Object o2, Class<?> type, Set<Field> fieldsToExclude) {
        for (Field field : type.getDeclaredFields()) {
            if (!fieldsToExclude.contains(field) && isOrdinaryInstanceField(field)) {
                AccessibleHelper.openSesame(field);
                try {
                    if (!equalState(field.get(o1), field.get(o2), fieldsToExclude)) {
                        return false;
                    }
                }
                catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return true;
    }

    private static boolean isOrdinaryInstanceField(Field field) {
        return !isStatic(field);
    }

    private static boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    public String stateToString(Object o) {
        if (o == null) {
            return String.valueOf(null);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(o.getClass().getName());
        sb.append("[");
        for (Class<?> type = o.getClass(); type.getSuperclass() != Object.class; type = type.getSuperclass()) {
            printState(o, type, sb);
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("]");
        return sb.toString();
    }

    private static void printState(Object o, Class<?> type, StringBuilder sb) {
        for (Field field : type.getDeclaredFields()) {
            if (isOrdinaryInstanceField(field)) {
                AccessibleHelper.openSesame(field);
                try {
                    sb.append(field.getName());
                    sb.append("=");
                    sb.append(field.get(o));
                    sb.append(",");
                }
                catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void assertImplementsToString(Object object) {
        Assert.assertFalse(Not.nil(object, "object").getClass() + " does not implement toString()",
                           unmodifiedToString(object));
    }

    public static boolean unmodifiedToString(Object object) {
        String toString = object.toString();
        return toString.equals(object.getClass().getName() + "@" + System.identityHashCode(object));
    }

    public static void assertEqHcMatch(Object one, Object two) {
        Assert.assertEquals("Objects not equal", one, two);
        Assert.assertEquals("Different hashCode(), but .equals(): " + one + " and " + two,
                            one.hashCode(), two.hashCode());
    }

    public static void assertEqHcMatchDistinct(Object one, Object two) {
        assertEqHcMatch(one, two);
        Assert.assertNotSame("Same instance: " + one, one, two);
    }

    public static void assertEqHcMismatch(Object one, Object two) {
        Assert.assertFalse(one.equals(two));
        Assert.assertNotSame(one.hashCode(), two.hashCode());
    }

    public static void assertArrayNotEquals(Object[] a1, Object[] a2) {
        Assert.assertFalse("Expected different arrays: " + Arrays.toString(a1), Arrays.equals(a1, a2));
    }

    public static void assertArrayEquals(Object[] a1, Object[] a2) {
        Assert.assertTrue("Expected " + Arrays.toString(a1) + ", got " + Arrays.toString(a2),
                          Arrays.equals(a1, a2));
    }

    public static void assertArrayNotEquals(int[] a1, int[] a2) {
        Assert.assertFalse("Expected different arrays: " + Arrays.toString(a1),
                           Arrays.equals(a1, a2));
    }

    public static void assertArrayEquals(int[] a1, int[] a2) {
        Assert.assertTrue("Expected " + Arrays.toString(a1) + ", got " + Arrays.toString(a2),
                          Arrays.equals(a1, a2));
    }

    public static void assertArrayNotEquals(long[] a1, long[] a2) {
        Assert.assertFalse("Expected different arrays: " + Arrays.toString(a1),
                           Arrays.equals(a1, a2));
    }

    public static void assertArrayEquals(long[] a1, long[] a2) {
        Assert.assertTrue("Expected " + Arrays.toString(a1) + ", got " + Arrays.toString(a2),
                          Arrays.equals(a1, a2));
    }

    public static void assertArrayNotEquals(boolean[] a1, boolean[] a2) {
        Assert.assertFalse("Expected different arrays: " + Arrays.toString(a1),
                           Arrays.equals(a1, a2));
    }

    public static void assertArrayEquals(boolean[] a1, boolean[] a2) {
        Assert.assertTrue("Expected " + Arrays.toString(a1) + ", got " + Arrays.toString(a2),
                          Arrays.equals(a1, a2));
    }

    public static void assertArrayNotEquals(byte[] a1, byte[] a2) {
        Assert.assertFalse("Expected different arrays: " + Arrays.toString(a1),
                           Arrays.equals(a1, a2));
    }

    public static void assertArrayEquals(byte[] a1, byte[] a2) {
        Assert.assertTrue("Expected " + Arrays.toString(a1) + ", got " + Arrays.toString(a2),
                          Arrays.equals(a1, a2));
    }

    public static void assertArrayNotEquals(short[] a1, short[] a2) {
        Assert.assertFalse("Expected different arrays: " + Arrays.toString(a1),
                           Arrays.equals(a1, a2));
    }

    public static void assertArrayEquals(short[] a1, short[] a2) {
        Assert.assertTrue("Expected " + Arrays.toString(a1) + ", got " + Arrays.toString(a2),
                          Arrays.equals(a1, a2));
    }

    public static void assertNotEquals(Object o1, Object o2) {
        assertNotEquals("Did not expect equals: " + o1 + " equals " + o2, o1, o2);
    }

    public static void assertNotEquals(String msg, Object o1, Object o2) {
        Assert.assertFalse(msg, EqHc.eq(o1, o2));
    }

    public static <T extends Comparable<T>> void assertComparablesSequenced(T... comparables) {
        if (VarArgs.present(comparables) && comparables.length > 1) {
            for (int i = 0; i < comparables.length - 1; i++) {
                T t1 = comparables[i];
                T t2 = comparables[i + 1];
                Assert.assertTrue(t1 + " should be BEFORE " + t2, t1.compareTo(t2) < 0);
                Assert.assertTrue(t2 + " should be AFTER " + t1, t2.compareTo(t1) > 0);
            }
        }
    }

    public static void assertEmpty(String msg, Collection<?> objects) {
        Assert.assertTrue(msg, objects.isEmpty());
    }

    public static void assertSize(String msg, int size, Collection<?> objects) {
        Assert.assertEquals(msg, size, objects.size());
    }
}
