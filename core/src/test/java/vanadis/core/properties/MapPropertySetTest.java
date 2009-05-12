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
package net.sf.vanadis.core.properties;

import junit.framework.Assert;
import net.sf.vanadis.core.collections.Generic;
import org.junit.Test;

import java.util.Map;

public class MapPropertySetTest extends Assert {

    private static final String P1 = "things_are";

    private static final String P2 = "communism_is";

    private static final String P3 = "summer_means_walking";

    private static final String VARIABLE1 = "v1";

    private static final String VARIABLE2 = "v2";

    @Test
    public void empty() {
        PropertySet propertySet = PropertySets.create();
        assertTrue(propertySet.isEmpty());
        propertySet.set("foo", "bar");
        assertFalse(propertySet.isEmpty());
    }

    @Test
    public void unchainedProperties() {
        PropertySet pInner = PropertySets.create("foo", "bar",
                                                 "zip", 1);
        PropertySet pOuter = PropertySets.create("zip", 2,
                                                 "zot", false);
        assertNull(pOuter.get("foo"));
        PropertySet chained = pOuter.withParent(pInner, true);

        assertNotSame(chained, pOuter);
        assertEquals("bar", chained.get("foo"));
        assertEquals((Integer) 2, chained.getInt("zip"));

        PropertySet unchained = chained.orphan();
        assertNotSame(pOuter, unchained);
        assertNotSame(chained, unchained);

        assertNull(unchained.get("foo"));
        assertEquals((Integer) 2, unchained.getInt("zip"));
    }

    @Test
    public void variableProperties() {
        String unreplaced1 = "a${v1}t";
        String replaced1 = "afoot";

        String unreplaced2 = "${v2}${v2}ic";
        String replaced2 = "barbaric";

        String unreplaced3 = "${v2}e${v1}t";
        String halfReplaced3 = "${v2}efoot";
        String replaced3 = "barefoot";

        Map<String, ?> map = Generic.map
                (P1, unreplaced1,
                 P2, unreplaced2,
                 P3, unreplaced3);
        PropertySet p = PropertySets.create(map);

        PropertySet variables = PropertySets.create(VARIABLE1, "foo");

        assertEquals(replaced1, p.get(P1, variables));
        assertEquals(unreplaced2, p.get(P2, variables));
        assertEquals(halfReplaced3, p.get(P3, variables));

        variables.set(VARIABLE2, "bar");
        assertEquals(replaced1, p.get(P1, variables));
        assertEquals(replaced2, p.get(P2, variables));
        assertEquals(replaced3, p.get(P3, variables));
    }

    @Test
    public void with() {
        PropertySet p = PropertySets.create("foo", "bar").set("zip", 5);
        assertEquals(5, p.get("zip"));
        assertNull(p.get("akka"));

        PropertySet pWrite = p.withParent(PropertySets.create("akka", "bakka"), true);
        assertTrue(pWrite.isWritable());
        assertEquals(5, p.get("zip"));
        assertNull("Original should not have akka!", p.get("akka"));
        assertEquals(5, pWrite.get("zip"));

        PropertySet pImmut = p.withParent(PropertySets.create("akka", "bakka"), true).copy(false);
        assertFalse(pImmut.isWritable());
        assertEquals(5, p.get("zip"));
        assertNull("Original should not have akka!", p.get("akka"));
        assertEquals(5, pImmut.get("zip"));

        p.set("zip", 6);
        assertEquals(6, p.get("zip"));
        assertEquals(5, pWrite.get("zip"));
        assertEquals(5, pImmut.get("zip"));
    }

    @Test
    public void copy() {
        PropertySet p = PropertySets.create("foo", "bar");
        assertFalse(p.copy(false).isWritable());
        PropertySet unwritable = p.copy(false);
        PropertySet copy = unwritable.set("foo", "baz");
        assertNotSame(copy, unwritable);
        assertEquals("bar", unwritable.get("foo"));
        assertEquals("baz", copy.get("foo"));
        assertTrue(copy.isWritable());
        assertFalse(unwritable.isWritable());
    }

    @Test
    public void collapse() {
        PropertySet set = PropertySets.create("foo", "bar");
        PropertySet child = PropertySets.create("zot", "zip").withParent(set, true);

        Map<String, Object> uncoll = child.toMap(false);
        assertEquals(1, uncoll.size());
        assertEquals("zip", uncoll.get("zot"));

        Map<String, Object> coll = child.toMap();
        assertEquals(2, coll.size());
        assertEquals("bar", coll.get("foo"));
        assertEquals("zip", coll.get("zot"));

    }

    @Test
    public void arrays() {
        assertEquals(PropertySets.create("foo", "bar"),
                     PropertySets.create("foo", "bar"));
        assertEquals(PropertySets.create("foo", new String[]{"bar"}),
                     PropertySets.create("foo", new String[]{"bar"}));
        assertEquals(PropertySets.create("foo", new String[]{"bar"}),
                     PropertySets.create("foo", new Object[]{"bar"}));
        assertEquals(PropertySets.create("foo", new String[]{"bar"},
                                         "zip", "zot"),
                     PropertySets.create("foo", new Object[]{"bar"},
                                         "zip", "zot"));
        assertEquals(PropertySets.create("foo", new int[]{2},
                                         "zip", "zot"),
                     PropertySets.create("foo", new int[]{2},
                                         "zip", "zot"));
        assertEquals(PropertySets.create("foo", new int[]{2},
                                         "zip", "zot"),
                     PropertySets.create("foo", new int[]{2},
                                         "zip", "zot"));
    }

    @Test
    public void parents() {
        PropertySet parent = PropertySets.create("foo", 1);
        PropertySet propertySet = PropertySets.create("bar", 2);
        PropertySet child = propertySet.withParent(parent, true);
        assertNotSame(child, propertySet);
        assertEquals(1, child.get("foo"));
        assertEquals(2, child.get("bar"));
    }

    @Test
    public void integerReplace() {
        PropertySet set = PropertySets.create("foo", "${bar}");
        PropertySet var = PropertySets.create("bar", "2");
        assertEquals("2", set.get("foo", var));
        assertEquals((Integer) 2, set.getInt("foo", var));
    }

    @Test
    public void numberCoerce() {
        assertEquals((Integer) 1, PropertySets.create("foo", 1).getInt("foo"));
        assertEquals((Integer) 1, PropertySets.create("foo", 1L).getInt("foo"));
    }

    @Test
    public void expand() {
        PropertySet parent = PropertySets.create("foo", "${bar}");
        PropertySet set = PropertySets.create("zip", "${zot}").withParent(parent);

        PropertySet exp = set.expand(PropertySets.create("bar", "1"), PropertySets.create("zot", "2"));

        assertEquals("1", exp.get("foo"));
        assertEquals("2", exp.get("zip"));
    }
}
