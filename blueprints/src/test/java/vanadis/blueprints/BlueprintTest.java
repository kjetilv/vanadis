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
package vanadis.blueprints;

import junit.framework.Assert;
import org.junit.Test;
import vanadis.core.lang.Not;
import vanadis.core.properties.PropertySet;
import vanadis.mvn.Coordinate;
import vanadis.mvn.Repo;

import java.util.Arrays;

public class BlueprintTest {

    private static final Coordinate FOO_BAR = Coordinate.at("foo.bar:foo.bar:jar:1.0");

    private static final Coordinate ZIP_ZOT = Coordinate.at("zip.zot:zip.zot:jar:1.0");

    @Test
    public void combineNodeData() {
        SystemSpecification nd1 = new SystemSpecification
                (null, "test1", Repo.DEFAULT.toURI(), null, Arrays.asList(bs(FOO_BAR, 1, null)),
                 null);
        SystemSpecification nd2 =
                new SystemSpecification(null, "test2", Repo.DEFAULT.toURI(), null,
                                        Arrays.asList(bs(ZIP_ZOT, 1, null)),
                                        null);
        SystemSpecification nd = nd1.combinedWith(nd2);
        Assert.assertNotNull(nd);
    }

    public static BundleSpecification bs(Coordinate coordinate,
                                         Integer startLevel, PropertySet propertySet) {
        return BundleSpecification.create(null, Not.nil(coordinate, "coordinate"), startLevel,
                                          propertySet, null, null);
    }
}
