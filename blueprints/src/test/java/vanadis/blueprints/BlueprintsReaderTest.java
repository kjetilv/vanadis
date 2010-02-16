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
import vanadis.core.lang.Not;
import vanadis.common.ver.Version;
import vanadis.mvn.Coordinate;
import vanadis.mvn.Repo;

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
        assertTrue(template.getParents().contains(vc.getBlueprint("vanadis-basic")));
        assertEquals("Unexpected # of parents: " + template.getParents(),
                     1, template.getParents().size());
    }

    private static Blueprints loadV1(String input) {
        return BlueprintsReader.readBlueprints(URI.create("file://test"), new ByteArrayInputStream(input.getBytes()));
    }

    @Test
    public void captureMalformedInputNoParent() {
        try {
            Assert.fail("Illegal: " + new Blueprints
                    (null, new Blueprint(null, "leaf1", "parent1", true, null, null, null)).validate());
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void captureMalformedInputAbstractLeaf() {
        try {
            Assert.fail("Illegal: " + new Blueprints
                    (null, new Blueprint(null, "leaf1", "parent1", true, null, null, null),
                     new Blueprint(null, "parent1", (String)null, true, null, null, null)).validate());
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void captureCycleNodes() {
        try {
            Assert.fail("Illegal: " + new Blueprints
                    (null, new Blueprint(null, "leaf1", "parent1", false, null, null, null),
                     new Blueprint(null, "parent1", "leaf1", false, null, null, null)).validate());
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
        Assert.assertTrue(blueprints.getBlueprint("foo").contains(bs
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

    @Test
    public void testBlueprintsVersionAndRepo() {
        Blueprints blueprints = loadV1
                ("<blueprints " + XML_NS + " default-version=\"1.2.3\" repo=\"file:///foo/bar\">" +
                        " <blueprint name=\"vanadis-routed\">" +
                        "  <bundle group=\"vanadis\" artifact=\"vanadis.foo\"/>" +
                        " </blueprint>" +
                        " <blueprint name=\"base-shell\">" +
                        "  <bundle group=\"vanadis\" artifact=\"vanadis.bar\"/>" +
                        " </blueprint>" +
                        "</blueprints>");
        assertNodeAndBundle(blueprints, "vanadis-routed", "1.2.3", "file:///foo/bar");
        assertNodeAndBundle(blueprints, "base-shell", "1.2.3", "file:///foo/bar");
    }

    @Test
    public void testBlueprintsAndBlueprintVersionAndRepo() {
        Blueprints blueprints = loadV1
                ("<blueprints " + XML_NS + " default-version=\"1.2.3\">" +
                        " <blueprint name=\"vanadis-routed\">" +
                        "  <bundle group=\"vanadis\" artifact=\"vanadis.foo\" repo=\"file:///foo/bar\"/>" +
                        " </blueprint>" +
                        " <blueprint name=\"base-shell\" default-version=\"2.4.6\" repo=\"file:///foo/zot\">" +
                        "  <bundle group=\"vanadis\" artifact=\"vanadis.bar\"/>" +
                        " </blueprint>" +
                        "</blueprints>");
        assertNodeAndBundle(blueprints, "vanadis-routed", "1.2.3", "file:///foo/bar");
        assertNodeAndBundle(blueprints, "base-shell", "2.4.6", "file:///foo/zot");
    }

    @Test
    public void testBlueprintVersionAndRepo() {
        Blueprints blueprints = loadV1
                ("<blueprints " + XML_NS + ">" +
                        " <blueprint name=\"vanadis-routed\" default-version=\"1.2.3\" repo=\"file:///foo/bar\">" +
                        "  <bundle group=\"vanadis\" artifact=\"vanadis.foo\"/>" +
                        " </blueprint>" +
                        " <blueprint name=\"base-shell\" default-version=\"2.4.6\" repo=\"file:///foo/zot\">" +
                        "  <bundle group=\"vanadis\" artifact=\"vanadis.bar\"/>" +
                        " </blueprint>" +
                        "</blueprints>");
        assertNodeAndBundle(blueprints, "vanadis-routed", "1.2.3", "file:///foo/bar");
        assertNodeAndBundle(blueprints, "base-shell", "2.4.6", "file:///foo/zot");
    }

    private static void assertNodeAndBundle(Blueprints blueprints, String bpName, String ver, String repo) {
        BundleSpecification bs = blueprints.getBlueprint(bpName).getDynaBundles().iterator().next();
        assertEquals(new Version(ver), bs.getCoordinate().getVersion());
        assertEquals(URI.create(repo), bs.getRepo());
    }

    public static BundleSpecification bs(Coordinate coordinate) {
        return BundleSpecification.create(null, Not.nil(coordinate, "coordinate"), null, null, null, null);
    }
}
