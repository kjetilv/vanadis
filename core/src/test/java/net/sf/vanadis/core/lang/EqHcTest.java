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
package net.sf.vanadis.core.lang;

import junit.framework.TestCase;
import net.sf.vanadis.core.test.VAsserts;

public class EqHcTest extends TestCase {

    public EqHcTest(String name) {
        super(name);
    }

    public void testTypedAs() {
        assertNull(EqHc.retyped("a string", 5));
        assertNull(EqHc.retyped("a string", null));
        assertNotNull(EqHc.retyped("a string", "another string"));
        assertNotNull(EqHc.retyped("same string", "same string"));
    }

    @SuppressWarnings({ "RedundantStringConstructorCall" })
    public void testEq() {
        Object foo = null;
        assertTrue(EqHc.eq(foo, foo));
        assertTrue(EqHc.eq("same string", "same string"));
        assertTrue(EqHc.eq("same string", "same string",
                           "another same string", "another same string"));
        assertTrue(EqHc.eq("a string", new String("a string")));
        assertTrue(EqHc.eq("a string", new String("a string"),
                           "a string", new String("a string")));
    }

    public void testNonEq() {
        assertFalse(EqHc.eq(null, 5));
        assertFalse(EqHc.eq(5, null));
    }

    @SuppressWarnings({ "PrimitiveArrayArgumentToVariableArgMethod" }) // It's a test
    public void testInvalidEq() {
        try {
            fail("Should not be able to compare to " + EqHc.eq(5));
        } catch (AssertionError ignore) { }
        try {
            fail("Should not be able to compare to " + EqHc.eq(5, 5, 5));
        } catch (AssertionError ignore) { }
        try {
            fail("Should not be able to compare to " + EqHc.eq(new int[] { 5, 5, 5 }));
        } catch (AssertionError ignore) { }
    }

    @SuppressWarnings({ "PrimitiveArrayArgumentToVariableArgMethod" }) // It's a test
    public void testArrays() {
        assertEquals(EqHc.hc(new int[] { 2, 2 }), EqHc.hc(new int[] { 2, 2 }));
    }

    public void testHashing() {
        assertEquals(EqHc.hc(0), EqHc.hc(0));
        assertEquals(EqHc.hc(1, 2, 3), EqHc.hc(1, 2, 3));
        VAsserts.assertNotEquals(EqHc.hc(1, 2, 3), EqHc.hc(3, 2, 1));
    }

    public void testHashArrays() {
        assertEquals(EqHc.hcA(new Object[] { 1, 2, 3 }, new Object[] { 1, 2, 3 }),
                     EqHc.hcA(new Object[] { 1, 2, 3 }, new Object[] { 1, 2, 3 }));
        assertEquals(EqHc.hc(new int[] { 1, 2, 3 }, new int[] { 1, 2, 3 }),
                     EqHc.hc(new int[] { 1, 2, 3 }, new int[] { 1, 2, 3 }));
        assertEquals(EqHc.hc(new long[] { 1, 2, 3 }, new long[] { 1, 2, 3 }),
                     EqHc.hc(new long[] { 1, 2, 3 }, new long[] { 1, 2, 3 }));
        assertEquals(EqHc.hc(new boolean[] { true, false }, new boolean[] { true, false }),
                     EqHc.hc(new boolean[] { true, false }, new boolean[] { true, false }));
        assertEquals(EqHc.hc(new short[] { 1, 2, 3 }, new short[] { 1, 2, 3 }),
                     EqHc.hc(new short[] { 1, 2, 3 }, new short[] { 1, 2, 3 }));
        assertEquals(EqHc.hc(new byte[] { 1, 2, 3 }, new byte[] { 1, 2, 3 }),
                     EqHc.hc(new byte[] { 1, 2, 3 }, new byte[] { 1, 2, 3 }));
        assertEquals(EqHc.hc(new char[] { 1, 2, 3 }, new char[] { 1, 2, 3 }),
                     EqHc.hc(new char[] { 1, 2, 3 }, new char[] { 1, 2, 3 }));
    }

    public void testEqArrays() {
        assertTrue(EqHc.eq(new Object[] { 1, 2 }, new Object[] { 1, 2 }));
        assertTrue(EqHc.eqA(new Object[] { 1, 2 }, new Object[] { 1, 2 },
                            new Object[] { 1, 2 }, new Object[] { 1, 2 }));
        assertTrue(EqHc.eq(new int[] { 1, 2 }, new int[] { 1, 2 }));
        assertTrue(EqHc.eq(new int[] { 1, 2 }, new int[] { 1, 2 },
                            new int[] { 1, 2 }, new int[] { 1, 2 }));
        assertTrue(EqHc.eq(new long[] { 1, 2 }, new long[] { 1, 2 }));
        assertTrue(EqHc.eq(new long[] { 1, 2 }, new long[] { 1, 2 },
                            new long[] { 1, 2 }, new long[] { 1, 2 }));
        assertTrue(EqHc.eq(new boolean[] { true, false }, new boolean[] { true, false }));
        assertTrue(EqHc.eq(new boolean[] { true, false }, new boolean[] { true, false },
                            new boolean[] { true, false }, new boolean[] { true, false }));
        assertTrue(EqHc.eq(new short[] { 1, 2 }, new short[] { 1, 2 }));
        assertTrue(EqHc.eq(new short[] { 1, 2 }, new short[] { 1, 2 },
                            new short[] { 1, 2 }, new short[] { 1, 2 }));
        assertTrue(EqHc.eq(new byte[] { 1, 2 }, new byte[] { 1, 2 }));
        assertTrue(EqHc.eq(new byte[] { 1, 2 }, new byte[] { 1, 2 },
                            new byte[] { 1, 2 }, new byte[] { 1, 2 }));
        assertTrue(EqHc.eq(new char[] { 1, 2 }, new char[] { 1, 2 }));
        assertTrue(EqHc.eq(new char[] { 1, 2 }, new char[] { 1, 2 },
                            new char[] { 1, 2 }, new char[] { 1, 2 }));
    }

    public void testNotEqArrays() {
        assertFalse(EqHc.eq(new Object[] { 1, 2 }, new Object[] { 1, 3 }));
        assertFalse(EqHc.eqA(new Object[] { 1, 2 }, new Object[] { 1, 2 },
                            new Object[] { 1, 2 }, new Object[] { 1, 3 }));
        assertFalse(EqHc.eq(new int[] { 1, 2 }, new int[] { 1, 3 }));
        assertFalse(EqHc.eq(new int[] { 1, 2 }, new int[] { 1, 2 },
                            new int[] { 1, 2 }, new int[] { 1, 3 }));
        assertFalse(EqHc.eq(new long[] { 1, 2 }, new long[] { 1, 3 }));
        assertFalse(EqHc.eq(new long[] { 1, 2 }, new long[] { 1, 2 },
                            new long[] { 1, 2 }, new long[] { 1, 3 }));
        assertFalse(EqHc.eq(new boolean[] { true, false }, new boolean[] { true, true }));
        assertFalse(EqHc.eq(new boolean[] { true, false }, new boolean[] { true, false },
                            new boolean[] { true, false }, new boolean[] { true, true }));
        assertFalse(EqHc.eq(new short[] { 1, 2 }, new short[] { 1, 3 }));
        assertFalse(EqHc.eq(new short[] { 1, 2 }, new short[] { 1, 2 },
                            new short[] { 1, 2 }, new short[] { 1, 3 }));
        assertFalse(EqHc.eq(new byte[] { 1, 2 }, new byte[] { 1, 3 }));
        assertFalse(EqHc.eq(new byte[] { 1, 2 }, new byte[] { 1, 2 },
                            new byte[] { 1, 2 }, new byte[] { 1, 3 }));
        assertFalse(EqHc.eq(new char[] { 1, 2 }, new char[] { 1, 3 }));
        assertFalse(EqHc.eq(new char[] { 1, 2 }, new char[] { 1, 2 },
                            new char[] { 1, 2 }, new char[] { 1, 3 }));
    }

    public void testDifferenceToRegularComparison() {
        int[] intA1 = { 1, 2 };
        int[] intA2 = { 1, 2 };
        int[] intA3 = { 1, 2 };
        assertArrayCharacteristics(intA1, intA2);
        assertArrayCharacteristics(intA1, intA3);
        assertArrayCharacteristics(intA2, intA3);

        assertFalse(EqHc.eq(intA1, new int[] { 1, 3 }));
        assertTrue(EqHc.eq(intA2, new int[] { 1, 2 }));
        assertFalse(EqHc.eq(intA2, new int[] { 1, 2 },
                            intA3, new int[] { 1, 3 }));
    }

    private static void assertArrayCharacteristics(int[] intA1, int[] intA2) {
        VAsserts.assertNotEquals(intA1, intA2);
        VAsserts.assertNotEquals(intA1.hashCode(), intA2.hashCode());
    }

}
