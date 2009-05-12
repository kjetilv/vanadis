/*
 * Copyright 2009 Kjetil Valstadsve
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

import org.junit.Assert;
import org.junit.Test;

public class NotATest {

    @Test
    public void notNullNotEmpty() {
        try {
            Not.nilOrEmpty("   ", "foo");
        } catch (IllegalArgumentException e) {
            junit.framework.Assert.assertEquals("Expected non-empty: foo", e.getMessage());
        }
    }

    @Test
    public void notNull() {
        try {
            Not.nil(null, "foo");
        } catch (NullPointerException e) {
            junit.framework.Assert.assertEquals("Expected non-null: foo", e.getMessage());
        }
    }

    @Test
    public void testNullMessage() {
        try {
            Not.nil(null, "foo");
            Assert.fail();
        } catch (RuntimeException ignored) {
        }
    }
}
