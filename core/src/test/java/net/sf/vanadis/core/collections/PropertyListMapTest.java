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

import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class PropertyListMapTest {

    private static final String[] keys = {"a", "b", "c", "d", "e"};

    @Test
    public void testGrowth() {
        PropertyListMap<String, Integer> plm = new PropertyListMap<String, Integer>(3, 65);
        assertEquals(3, plm.getCapacity());
        plm.put("a", 1);
        assertEquals(3, plm.getCapacity());
        plm.put("b", 2);
        assertEquals(6, plm.getCapacity());
        assertEquals(2, plm.size());
        assertEquals((Integer) 1, plm.get("a"));
        assertEquals((Integer) 2, plm.get("b"));

        assertEquals((Integer) 2, plm.remove("b"));
        assertEquals(3, plm.getCapacity());
        assertEquals((Integer) 1, plm.get("a"));
    }

    @Test
    public void assertPutAll() {
        Map<String, Integer> map = create();
        Map<String, Integer> source = fiveByTwo();
        map.putAll(source);
        assertEquals(5, map.size());
        assertKeySet(map.keySet());
        assertValues(map.values());
        assertPutGet(map);
    }

    @Test
    public void assertSize() {
        assertEquals(5, fiveByTwo().size());
        assertEquals(0, create().size());
    }

    @Test
    public void keySet() {
        assertKeySet(fiveByTwo().keySet());
    }

    @Test
    public void values() {
        assertValues(fiveByTwo().values());
    }

    @Test
    public void putGet() {
        assertPutGet(fiveByTwo());
    }

    private static PropertyListMap<String, Integer> create() {
        return new PropertyListMap<String, Integer>();
    }

    private static void assertKeySet(Set<String> map) {
        assertNotNull(map);
        assertEquals(5, map.size());
        for (String key : keys) {
            Assert.assertTrue(map.contains(key));
        }
    }

    private static void assertValues(Collection<Integer> map) {
        assertNotNull(map);
        assertEquals(5, map.size());
        for (int i = 0; i < 5; i++) {
            Assert.assertTrue(map.contains(i + 1));
        }
    }

    private static void assertPutGet(Map<String, Integer> map) {
        assertNull(map.get("dave"));
        assertEquals((Integer) 4, map.put("d", 0));
        assertEquals((Integer) 0, map.get("d"));
        assertEquals((Integer) 0, map.remove("d"));
        assertNull(map.put("f", 7));
    }

    private static Map<String, Integer> fiveByTwo() {
        PropertyListMap<String, Integer> map = new PropertyListMap<String, Integer>(5, 10);
        for (int i = 0; i < keys.length; i++) {
            assertNull(map.put(keys[i], i + 1));
        }
        return map;
    }
}
