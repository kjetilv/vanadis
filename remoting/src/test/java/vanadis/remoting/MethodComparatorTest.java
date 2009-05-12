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
package vanadis.remoting;

import junit.framework.Assert;
import vanadis.core.collections.Generic;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class MethodComparatorTest extends Assert {

    private Method m1;

    private Method m2;

    private Method m3;

    private Method m4;

    private Method m5;

    private Method m0;

    private static final MethodComparator CMP = new MethodComparator();

    private interface Type {

        void foo(int one, boolean two);

        void foo(String one, boolean two);

        void foo(String one);

        void bar();

        void bar(String one);

        void bar(boolean one);

    }

    @Before
    public void setUp() throws NoSuchMethodException {
        m0 = Type.class.getMethod("foo", int.class, boolean.class);
        m1 = Type.class.getMethod("foo", String.class, boolean.class);
        m2 = Type.class.getMethod("foo", String.class);
        m3 = Type.class.getMethod("bar");
        m4 = Type.class.getMethod("bar", String.class);
        m5 = Type.class.getMethod("bar", boolean.class);
    }

    @Test
    public void sort() {
        List<Method> list = Generic.list(Type.class.getMethods());
        Collections.sort(list, CMP);
        assertEquals(m3, list.get(0));
        assertEquals(m5, list.get(1));
        assertEquals(m4, list.get(2));
        assertEquals(m2, list.get(3));
        assertEquals(m0, list.get(4));
        assertEquals(m1, list.get(5));
    }

    @Test
    public void equals() {
        assertEquals(0, CMP.compare(m0, m0));
        assertEquals(0, CMP.compare(m1, m1));
        assertEquals(0, CMP.compare(m2, m2));
        assertEquals(0, CMP.compare(m3, m3));
        assertEquals(0, CMP.compare(m4, m4));
        assertEquals(0, CMP.compare(m5, m5));
    }

}
