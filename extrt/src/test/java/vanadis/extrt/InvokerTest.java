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

package vanadis.extrt;

import static junit.framework.Assert.*;
import vanadis.core.io.Location;
import vanadis.core.properties.PropertySets;
import vanadis.core.reflection.InvokeException;
import static vanadis.core.reflection.Invoker.*;
import vanadis.core.time.TimeSpan;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings({"UnnecessaryBoxing"})
public class InvokerTest {

    private AtomicReference<Object> lastSet;

    public final class Setter {

        public void setString(String value) {
            lastSet.set(value);
        }

        public void setInteger(Integer value) {
            lastSet.set(value);
        }

        public void setLong(Long value) {
            lastSet.set(value);
        }

        public void setDouble(Double value) {
            lastSet.set(value);
        }

        public void setFloat(Float value) {
            lastSet.set(value);
        }

        public void setByte(Byte value) {
            lastSet.set(value);
        }

        public void setBoolean(Boolean value) {
            lastSet.set(value);
        }

        public void setShort(Short value) {
            lastSet.set(value);
        }

        public void setIntegerPrimitive(int value) {
            lastSet.set(value);
        }

        public void setLongPrimitive(long value) {
            lastSet.set(value);
        }

        public void setDoublePrimitive(double value) {
            lastSet.set(value);
        }

        public void setFloatPrimitive(float value) {
            lastSet.set(value);
        }

        public void setBytePrimitive(byte value) {
            lastSet.set(value);
        }

        public void setBooleanPrimitive(boolean value) {
            lastSet.set(value);
        }

        public void setShortPrimitive(short value) {
            lastSet.set(value);
        }

        public void setTimeSpan(TimeSpan value) {
            lastSet.set(value);
        }

        public void setLocation(Location value) {
            lastSet.set(value);
        }

        public void setFile(File value) {
            lastSet.set(value);
        }

    }

    @Test
    public void invocation() {
        invoke(this, new Setter(), setter(String.class), "foo");
        assertEquals("foo", lastSet.get());
    }

    @Test
    public void invokeBoolean() {
        invoke(this, new Setter(), setter(boolean.class), true);
        assertTrue((Boolean) lastSet.get());
    }

    @Test
    public void invokeLong() {
        invoke(this, new Setter(), setter(long.class), 1);
        assertEquals(new Long(1), lastSet.get());
    }

    @Test
    public void invokeLongWrapper() {
        invoke(this, new Setter(), setter(Long.class), new Long(1));
        assertEquals(new Long(1), lastSet.get());
    }

    @Test
    public void invokeLongCoerced() {
        invokeCoerced(this, new Setter(), setter(long.class), "1");
        assertEquals(new Long(1), lastSet.get());
    }

    @Test
    public void invokeDouble() {
        invoke(this, new Setter(), setter(double.class), 1);
        assertEquals(new Double(1), lastSet.get());
    }

    @Test
    public void invokeDoubleWrapper() {
        invoke(this, new Setter(), setter(Double.class), new Double(1));
        assertEquals(new Double(1), lastSet.get());
    }

    @Test
    public void invokeDoubleCoerced() {
        invokeCoerced(this, new Setter(), setter(Double.class), "1.0");
        assertEquals(new Double(1), lastSet.get());
    }

    @Test
    public void invokeFloat() {
        invoke(this, new Setter(), setter(float.class), 1);
        assertEquals(new Float(1), lastSet.get());
    }

    @Test
    public void invokeFloatWrapper() {
        invoke(this, new Setter(), setter(Float.class), new Float(1));
        assertEquals(new Float(1), lastSet.get());
    }

    @Test
    public void invokeFloatCoerced() {
        invokeCoerced(this, new Setter(), setter(Float.class), "1.0");
        assertEquals(new Float(1), lastSet.get());
    }

    @Test
    public void invokeByte() {
        invoke(this, new Setter(), setter(byte.class), (byte) 1);
        assertEquals(new Byte((byte) 1), lastSet.get());
    }

    @Test
    public void invokeByteWrapper() {
        invoke(this, new Setter(), setter(Byte.class), new Byte((byte) 1));
        assertEquals(new Byte((byte) 1), lastSet.get());
    }

    @Test
    public void invokeShort() {
        invoke(this, new Setter(), setter(short.class), (short) 1);
        assertEquals(new Short((short) 1), lastSet.get());
    }

    @Test
    public void invokeShortWrapper() {
        invoke(this, new Setter(), setter(Short.class), new Short((short) 1));
        assertEquals(new Short((short) 1), lastSet.get());
    }

    @Test
    public void invokeShortCoerced() {
        invokeCoerced(this, new Setter(), setter(Short.class), "1");
        assertEquals(new Short((short) 1), lastSet.get());
    }

    @Test
    public void invokeInteger() {
        invoke(this, new Setter(), setter(int.class), 1);
        assertEquals(new Integer(1), lastSet.get());
    }

    @Test
    public void invokeIntegerWrapped() {
        invoke(this, new Setter(), setter(Integer.class), new Integer(1));
        assertEquals(new Integer(1), lastSet.get());
    }

    @Test
    public void invokeTimeSpan() {
        invoke(this, new Setter(), setter(TimeSpan.class), TimeSpan.HALF_MINUTE);
        assertEquals(TimeSpan.HALF_MINUTE, lastSet.get());
    }

    @Test
    public void invokeTimeSpanCoerced() {
        invokeCoerced(this, new Setter(), setter(TimeSpan.class), "30 seconds");
        assertEquals(TimeSpan.HALF_MINUTE, lastSet.get());
    }

    @Test
    public void invokeFileCoerced() {
        invokeCoerced(this, new Setter(), setter(File.class), "/tmp/bogus");
        assertEquals(new File("/tmp/bogus"), lastSet.get());
    }

    @Test
    public void invokeLocationCoerced() {
        invokeCoerced(this, new Setter(), setter(Location.class), "foo:20");
        assertEquals(Location.parse("foo:20"), lastSet.get());
    }

    @Test
    public void invokeBogusReplaced() {
        try {
            fail(invokeReplacedCoerced(this, new Setter(), setter(String.class),
                                       PropertySets.create("foo", "bar"),
                                       "let's crash a ${foo") + " should not work");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void invokeBogusPropertiesReplaced() {
        invokeReplacedCoerced(this, new Setter(), setter(String.class),
                              PropertySets.create(),
                              "let's crash a ${foo}");
        assertEquals("let's crash a ${foo}", lastSet.get());
    }

    @Test
    public void invokeReplaced() {
        invokeReplacedCoerced(this, new Setter(), setter(String.class),
                              PropertySets.create("foo", "bar"),
                              "let's find a ${foo}");
        assertEquals("let's find a bar", lastSet.get());
        invokeReplacedCoerced(this, new Setter(), setter(String.class),
                              PropertySets.create("foo", "bar"),
                              "${foo}, let's find a ${foo}");
        assertEquals("bar, let's find a bar", lastSet.get());
        invokeReplacedCoerced(this, new Setter(), setter(String.class),
                              PropertySets.create("foo", "bar"),
                              "${foo}, let's ${foo} find a ${foo}");
        assertEquals("bar, let's bar find a bar", lastSet.get());
    }

    @Test
    public void checkedInvoke() {
        invokeTypeChecked(this, new Setter(), setter(String.class), "foo");
        assertEquals("foo", lastSet.get());
        try {
            fail(invokeTypeChecked(this, new Setter(), setter(String.class), "foo", "bar") + ", should not return");
        } catch (InvokeException ignore) {
        }
        try {
            fail(invokeTypeChecked(this, new Setter(), setter(String.class), 1) + ", should not return");
        } catch (InvokeException ignore) {
        }
    }

    @Before
    public void setUp() {
        lastSet = new AtomicReference<Object>();
    }

    private Method setter(Class<?> type) {
        try {
            String className = type.getSimpleName();
            if (type.isPrimitive()) {
                className = className.substring(0, 1).toUpperCase() + className.substring(1);
            }
            if (className.equals("Int")) {
                className += "eger";
            }
            if (type.isPrimitive()) {
                className += "Primitive";
            }
            return Setter.class.getMethod("set" + className, type);
        } catch (NoSuchMethodException e) {
            fail("Test broken! " + e);
            return null;
        }
    }

    @After
    public void tearDown() {
        lastSet.set(null);
    }
}
