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
package vanadis.core.io;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;

public class BytesTest {

    @Test
    public void stringFrom() {
        Assert.assertEquals("uhf", Bytes.stringFrom("uhf".getBytes()));
        Assert.assertEquals("hf", Bytes.stringFrom("uhf".getBytes(), 1));
        Assert.assertEquals("h", Bytes.stringFrom("uhf".getBytes(), 1, 1));
    }

    @Test
    public void intFrom() {
        assertInt(72235);
        assertInt(1);
        assertInt(-1);
        assertInt(0);
        assertInt(Integer.MAX_VALUE);
        assertInt(Integer.MIN_VALUE);
    }

    @Test
    public void intFromShortOrLongArray() {
        byte[] one = Bytes.bytesFrom(1);
        Assert.assertEquals(1, Bytes.intFrom(one));

        Assert.assertEquals(1, Bytes.intFrom(one, 1));
        Assert.assertEquals(1, Bytes.intFrom(one, 2));
        Assert.assertEquals(1, Bytes.intFrom(one, 3));

        Assert.assertEquals(1, Bytes.intFrom(Bytes.suffix(1, one)));
        Assert.assertEquals(1, Bytes.intFrom(Bytes.suffix(2, one)));
        Assert.assertEquals(1, Bytes.intFrom(Bytes.suffix(3, one)));

        Assert.assertEquals(1, Bytes.intFrom(Bytes.add(new byte[]{ 0 }, one), 1));
        Assert.assertEquals(1, Bytes.intFrom(Bytes.add(new byte[]{ 0, 0 }, one), 2));
        Assert.assertEquals(1, Bytes.intFrom(Bytes.add(new byte[]{ 0, 0, 0 }, one), 3));
        Assert.assertEquals(1, Bytes.intFrom(Bytes.add(one, new byte[]{ 0, 0 })));
        Assert.assertEquals(1, Bytes.intFrom(Bytes.add(one, new byte[]{ 0, 0, 0 })));
        Assert.assertEquals(1, Bytes.intFrom(Bytes.add(one, new byte[]{ 0 })));
        Assert.assertEquals(1, Bytes.intFrom(Bytes.add(one, new byte[]{ 0, 0 })));
        Assert.assertEquals(1, Bytes.intFrom(Bytes.add(one, new byte[]{ 0, 0, 0 })));
    }

    @Test
    public void suffix() {
        Assert.assertTrue(Arrays.equals
            (new byte[]{ 4, 5 },
             Bytes.suffix(3, new byte[]{ 1, 2, 3, 4, 5 })));
        Assert.assertTrue(Arrays.equals
            (new byte[]{ 3, 4, 5 },
             Bytes.suffix(2, new byte[]{ 1, 2, 3, 4, 5 })));
    }

    private void assertInt(int i) {
        Assert.assertEquals(i, Bytes.intFrom(Bytes.bytesFrom(i)));
    }
}
