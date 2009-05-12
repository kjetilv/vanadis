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

public class ToStringTest extends TestCase {

    public ToStringTest(String name) {
        super(name);
    }

    public void testStringBuilder() {
        assertEquals(getClass().getSimpleName() + "[foo bar zot]",
                     ToString.of(this, new StringBuilder("foo ").append("bar ").append("zot")));
    }

    public void testNoBody() {
        assertEquals(getClass().getSimpleName() + "[]", ToString.of(this));
    }

    public void testSimpleBody() {
        assertEquals(getClass().getSimpleName() + "[foo]", ToString.of(this, "foo"));
    }

    public void testTwoPartBody() {
        assertEquals(getClass().getSimpleName() + "[foo:bar]", ToString.of(this, "foo", "bar"));
    }

    public void testTwoPartBodyWithNull() {
        assertEquals(getClass().getSimpleName() + "[foo:null]", ToString.of(this, "foo", null));
    }

    public void testTwoPartBodyWithEmtpyHeader() {
        assertEquals(getClass().getSimpleName() + "[2]", ToString.of(this, "   ", 2));
    }

    public void testTwoPartBodyWithEqualsSign() {
        assertEquals(getClass().getSimpleName() + "[foo=2]", ToString.of(this, "foo=", 2));
        assertEquals(getClass().getSimpleName() + "[foo=null]", ToString.of(this, "foo=", null));
    }

    public void testTwoPartBodyWithAmpersand() {
        assertEquals(getClass().getSimpleName() + "[foo@2]", ToString.of(this, "foo@", 2));
        assertEquals(getClass().getSimpleName() + "[foo@null]", ToString.of(this, "foo@", null));
    }

    public void testNullHeaderBody() {
        assertEquals(getClass().getSimpleName() + "[zot foo:bar]", ToString.of(this, null, "zot", "foo", "bar"));
    }

    public void testNullHeaderLongerBody() {
        assertEquals(getClass().getSimpleName() + "[zot foo:bar zip:zot]", ToString.of(this, null, "zot", "foo", "bar",
                                                                                       "zip", "zot"));
        assertEquals(getClass().getSimpleName() + "[zot foo:bar zip:zot]", ToString.of(this, "zot", "foo", "bar",
                                                                                       "zip", "zot"));
    }

    public void testOddBody() {
        assertEquals(getClass().getSimpleName() + "[zot foo:bar]", ToString.of(this, "zot", "foo", "bar"));
    }

    public void testOddBodyWithNullHeader() {
        assertEquals(getClass().getSimpleName() +
                     "[zot foo:bar end]", ToString.of(this, "zot", "foo", "bar", null, "end"));
    }

}
