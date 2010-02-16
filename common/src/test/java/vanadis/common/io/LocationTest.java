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
package vanadis.common.io;

import static junit.framework.Assert.assertEquals;
import org.junit.Assert;
import org.junit.Test;

public class LocationTest {

    @Test
    public void fromString() {
        assertEquals(new Location("host", 5), Location.parse("host:5"));
    }

    @Test
    public void fromNumberString() {
        assertEquals(new Location("localhost", 5), Location.parse("5"));
    }

    @Test
    public void recognizeLocation() {
        Assert.assertTrue("Should be a location: foo:80", Location.isLocation("foo:80"));
        Assert.assertTrue("Should be a location: foo:80", Location.isLocation("  foo:80"));
        Assert.assertTrue("Should be a location: foo:80", Location.isLocation("  foo :80"));
        Assert.assertTrue("Should be a location: foo:80", Location.isLocation("  foo :80 "));
        Assert.assertTrue("Should be a location: foo:80", Location.isLocation("  foo :  80"));
        Assert.assertTrue("Should be a location: foo:80", Location.isLocation("  foo:  80"));
        Assert.assertTrue("Should be a location: foo:80", Location.isLocation("  foo:  80 "));
        Assert.assertTrue("Should be a location: foo:80", Location.isLocation("foo:80"));
        Assert.assertTrue("Should be a location: foo:80", Location.isLocation("foo :80"));
        Assert.assertTrue("Should be a location: foo:80", Location.isLocation("foo :80 "));
        Assert.assertTrue("Should be a location: foo:80", Location.isLocation("foo :  80"));
        Assert.assertTrue("Should be a location: foo:80", Location.isLocation("foo:  80"));
        Assert.assertTrue("Should be a location: foo:80", Location.isLocation("foo:  80 "));
        Assert.assertFalse("Not a location: 80", Location.isLocation("80"));
        Assert.assertFalse("Not a location: 80", Location.isLocation("foo"));
        Assert.assertFalse("Not a location: 80", Location.isLocation("foo:"));
        Assert.assertFalse("Not a location: 80", Location.isLocation("foo:655360"));
    }
}
