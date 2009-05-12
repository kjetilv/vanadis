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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.SortedMap;

public class TypeEnumerationTest extends Assert {

    private Method method;


    @Test
    public void enumerate() {
        int index = Accessor.indexOf(SortedMap.class, method);
        assertEquals(method, Accessor.methodNo(SortedMap.class, index));
    }

    @Before
    public void setUp() throws NoSuchMethodException {
        method = Map.class.getMethod("isEmpty");
    }

    @After
    public void teardown() {
        method = null;
    }

}
