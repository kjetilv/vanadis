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
package vanadis.extrt;

import vanadis.common.io.Location;
import org.junit.Assert;
import org.junit.Test;

public class PortUtilsTest {

    @Test
    public void testStraightPort() {
        Assert.assertEquals(Location.parse("foo:8080"), PortUtils.resolveLocation(Location.parse("foo:10000"), "8080"));
    }

    @Test
    public void testBaseLocation() {
        Assert.assertEquals(Location.parse("foo:8080"),
                            PortUtils.resolveLocation(Location.parse("foo:8000"), "baseport + 80"));
    }
}
