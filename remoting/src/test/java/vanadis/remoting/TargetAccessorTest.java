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
package net.sf.vanadis.remoting;

import junit.framework.Assert;
import net.sf.vanadis.core.collections.Generic;
import org.junit.Test;

import java.util.Map;

public class TargetAccessorTest extends Assert {

    private static Map<String, ?> fooMap = Generic.map("foo", new TestTargetImpl() {
    });

    private static Accessor accessor = Accessor.getSingleton();

    static {
        accessor.registerAccessPoint(fooMap);
    }

    @Test
    public void getClient() {
        assertNotNull(accessor.get(null, new MapTargetReference<TestTarget>("foo", TestTarget.class)));
    }

    public void testDontGetMistypedClient() {
        TargetMatch<?> object = accessor.get(null, new MapTargetReference<String>("foo", String.class));
        assertNull("Should be null: " + object, object);
    }

    @Test
    public void dontGetMisnamedClient() {
        assertNull(accessor.get(null, new MapTargetReference<TestTarget>("foobar", TestTarget.class)));
    }

}
