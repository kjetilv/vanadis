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
import static junit.framework.Assert.*;
import org.junit.Test;
import vanadis.util.mvn.Coordinate;
import vanadis.util.mvn.Repo;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class BlueprintsReaderTest {
    private static final String XML_NS = "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xmlns=\"http://kjetilv.github.com/vanadis/blueprints\"";

    @Test
    public void loadConfiguration() throws URISyntaxException {
        Blueprints vc = loadV1(("<blueprints xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                "            xmlns=\"http://kjetilv.github.com/vanadis/blueprints\">" +
                " <blueprint name=\"base\">" +
                " </blueprint>" +
                " <blueprint name=\"vanadis-basic\" extends=\"base\">" +
                "  <module type=\"remoting\"/>" +
                " </blueprint>" +
                " <blueprint name=\"vanadis-routed\" extends=\"vanadis-basic\">" +
                "  <module type=\"networker\"/>" +
                " </blueprint>" +
                "</blueprints>"));
        assertNotNull(vc);
        assertNotNull(vc.getBlueprint("base"));
        Blueprint template = vc.getBlueprint("vanadis-routed");
        assertNotNull(template);
        assertSame(template.getParent(), vc.getBlueprint("vanadis-basic"));
    }

    private static Blueprints loadV1(String input) {
        return BlueprintsReader.readBlueprints(URI.create("file://test"), new ByteArrayInputStream(input.getBytes()));
    }

    @Test
    public void captureMalformedInputNoParent() {
        try {
            Assert.fail("Illegal: " + new Blueprints
                    (null, new Blueprint(null, "leaf1", "parent1", true)).validate());
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void captureMalformedInputAbstractLeaf() {
        try {
            Assert.fail("Illegal: " + new Blueprints
                    (null, new Blueprint(null, "leaf1", "parent1", true),
                     new Blueprint(null, "parent1", null, true)).validate());
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void captureCycleNodes() {
        try {
            Assert.fail("Illegal: " + new Blueprints
                    (null, new Blueprint(null, "leaf1", "parent1", false),
                     new Blueprint(null, "parent1", "leaf1", false)).validate());
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testBundleProperties() {
        Blueprints blueprints = loadV1("<blueprints " + XML_NS + ">" +
                "<blueprint name=\"foo\">" +
                "  <bundles group-prefix=\"aaa\" artifact-prefix=\"foo\">" +
                "    <bundles artifact-prefix=\"bar\">" +
                "      <bundle artifact=\"zot\">" +
                "        <properties>" +
                "          <property name=\"zip\">" +
                "            zot" +
                "          </property>" +
                "        </properties>" +
                "      </bundle>" +
                "    </bundles>" +
                "  </bundles>" +
                "</blueprint></blueprints>");
        BundleSpecification spec = blueprints.getBlueprint("foo").getDynaBundles().iterator().next();
        Assert.assertNotNull(spec.getPropertySet());
        Assert.assertTrue(spec.getPropertySet().has("zip"));
        Assert.assertEquals("zot", spec.getPropertySet().getString("zip"));
    }

    @Test
    public void testBundleNesting() {
        Blueprints blueprints = loadV1("<blueprints " + XML_NS + ">" +
                "<blueprint name=\"foo\">" +
                "  <bundles group-prefix=\"aaa\" artifact-prefix=\"foo\">" +
                "    <bundles artifact-prefix=\"bar\">" +
                "      <bundle artifact=\"zot\"/>" +
                "    </bundles>" +
                "  </bundles>" +
                "</blueprint></blueprints>");
        Assert.assertTrue(blueprints.getBlueprint("foo").contains(BundleSpecification.create
                (Coordinate.unversioned("aaa", "foo.bar.zot"))));
    }

    @Test
    public void testNodeData() {
        Blueprints blueprints = loadV1
                ("<blueprints " + XML_NS + ">" +
                        " <blueprint name=\"vanadis-routed\">" +
                        "  <module type=\"remoting\"/>" +
                        " </blueprint>" +
                        " <blueprint name=\"base-shell\">" +
                        "  <module type=\"networker\"/>" +
                        " </blueprint>" +
                        "</blueprints>");
        SystemSpecification systemSpecification =
                blueprints.getSystemSpecification(Repo.DEFAULT.toURI(), "vanadis-routed", "base-shell");

        assertTrue(systemSpecification.containsModuleSpecification("remoting"));
        assertTrue(systemSpecification.containsModuleSpecification("networker"));
    }
}
