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

package net.sf.vanadis.core.collections;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

public class ChainableIterableTest {

    @Test
    public void chain() {
        Iterable<String> iterable = Iterables.chain
                (Arrays.asList(Generic.list("foo", "bar"),
                               Collections.<String>emptySet(),
                               Collections.singletonList("zot")));
        assertElements(iterable, "foo", "bar", "zot");
    }

    private static void assertElements(Iterable<?> iterable, Object... objs) {
        Iterator<?> iterator = iterable.iterator();
        for (int i = 0; i < objs.length; i++) {
            Assert.assertEquals("Element " + i + " wrong", objs[i], iterator.next());
        }
        Assert.assertFalse(iterator.hasNext());
    }

}
