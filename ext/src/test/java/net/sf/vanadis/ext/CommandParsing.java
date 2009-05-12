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
package net.sf.vanadis.ext;

import junit.framework.Assert;
import net.sf.vanadis.core.collections.Pair;
import org.junit.Test;

import java.util.Arrays;

public class CommandParsing {

    @Test
    public void testSimpleParsing() {
        assertParsed("foo bar zot", "foo", 2, "bar", "zot");
    }

    @Test
    public void testSpacedOutParsing() {
        assertParsed("foo  bar    zot", "foo", 2, "bar", "zot");
    }

    private void assertParsed(String command, String realCommand, int length, String... parsed) {
        Pair<String,String[]> parse = Parse.args(command);
        Assert.assertEquals(realCommand, parse.getOne());
        Assert.assertEquals(length, parse.getTwo().length);
        Assert.assertTrue(Arrays.equals(parsed, parse.getTwo()));
    }
}
