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
package net.sf.vanadis.ext;

import net.sf.vanadis.core.properties.PropertySet;
import net.sf.vanadis.core.properties.PropertySets;
import net.sf.vanadis.core.reflection.Retyper;
import org.junit.Assert;
import org.junit.Test;

public class CorePropertyTest {

    @Test
    public void lookupArray() {
        Retyper.coerce(String.class, 5);
        PropertySet set = PropertySets.create(CoreProperty.OBJECTCLASSES_NAME, new String[] { "foo", "bar" });
        String[] oc = CoreProperty.OBJECTCLASSES.lookupIn(set);
        Assert.assertEquals("foo", oc[0]);
        Assert.assertEquals("bar", oc[1]);
    }
}
