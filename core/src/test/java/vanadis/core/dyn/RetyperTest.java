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
package vanadis.core.dyn;

import static junit.framework.Assert.assertEquals;
import org.junit.Assert;
import org.junit.Test;
import vanadis.core.reflection.Retyper;

import java.lang.reflect.Array;

public class RetyperTest {

    @Test
    public void coerce() {
        assertEquals(2, Retyper.coerce(int.class, "2"));
        assertEquals(2, Retyper.coerce("int", "2"));
        assertEquals(2, Retyper.coerce("java.lang.Integer", "2"));
    }

    @Test
    public void coerceLong() {
        String longString = String.valueOf(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, Retyper.coerce(long.class, longString));
        assertEquals(Long.MAX_VALUE, Retyper.coerce("long", longString));
        assertEquals(Long.MAX_VALUE, Retyper.coerce("java.lang.Long", longString));
    }

    public enum Eeenoom {
        EEE, OOO
    }

    @Test
    public void coerceEnum() {
        assertEquals(Eeenoom.OOO, Retyper.coerce(Eeenoom.class, "OOO"));
        assertEquals(Eeenoom.EEE, Retyper.coerce(Eeenoom.class, "EEE"));
    }

    @Test
    public void coerceArrays() {
        Object o = Retyper.coerce(long[].class, new Object[]{1L, 2L});
        Assert.assertTrue(o instanceof long[]);
        Assert.assertEquals(1L, Array.get(o, 0));
        Assert.assertEquals(2L, Array.get(o, 1));
    }

    @Test
    public void typeIntFromString() {
        Assert.assertEquals(5, Retyper.coerce("int", "5"));
        Assert.assertEquals(5, Retyper.coerce("integer", "5"));
    }

    @Test
    public void typeLongFromString() {
        Assert.assertEquals(5L, Retyper.coerce("long", "5"));
        Assert.assertEquals(5L, Retyper.coerce("Long", "5"));
        Assert.assertEquals(5L, Retyper.coerce("LONG", "5"));
    }
}
