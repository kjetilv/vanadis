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
package vanadis.core.lang;

import junit.framework.TestCase;

import java.util.Arrays;

public class VarArgsTest extends TestCase {

    public VarArgsTest(String name) {
        super(name);
    }

    private static void ohYeahGotVarArgs(int count, Object... aFew) {
        assertTrue(count > 0);
        assertTrue("No varargs: " + Arrays.toString(aFew), VarArgs.present(aFew));
        assertEquals(count, aFew.length);
    }

    private static void noVarArgsHere(Object... none) {
        assertTrue(VarArgs.notPresent(none));
    }

    public void testVarArgs() {
        ohYeahGotVarArgs(1, 2);
        ohYeahGotVarArgs(1, 2);
        ohYeahGotVarArgs(2, 3, 4);
        ohYeahGotVarArgs(2, null, null);
        ohYeahGotVarArgs(3, "these", "are the", "three varargs");
        ohYeahGotVarArgs(3, (Object[])new String[] {"these", "are the", "three varargs too!"});
        ohYeahGotVarArgs(3, null, null, 2);
    }

    @SuppressWarnings({ "NullArgumentToVariableArgMethod" })
    public void testNoVarags() {
        noVarArgsHere();
        noVarArgsHere(null); // I know there's a compiler warning here.
    }

}
