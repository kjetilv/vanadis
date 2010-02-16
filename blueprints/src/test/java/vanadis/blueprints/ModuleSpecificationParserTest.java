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

import org.junit.Test;
import org.w3c.dom.Element;
import vanadis.core.properties.PropertySet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;

import static junit.framework.Assert.*;
import static vanadis.blueprints.ModuleSpecificationFeatureType.EXPOSE;
import static vanadis.blueprints.ModuleSpecificationFeatureType.INJECT;

public class ModuleSpecificationParserTest {

    @Test
    public void readFrom() {
        URI uri = getURI("<module name=\"foo\" type=\"bar\"/>");

        ModuleSpecification specification = readModuleSpec(uri);
        assertNotNull(specification);

        assertEquals("foo", specification.getName());
        assertEquals("bar", specification.getType());
    }

    @Test
    public void readPropertiesFrom() {
        URI uri = getURI
                ("<module name=\"foo\" type=\"bar\">" +
                        "      <properties>" +
                        "        <property name=\"p1\" type=\"int\">2</property>" +
                        "      </properties>" +
                        "    </module>");
        ModuleSpecification specification = readModuleSpec(uri);
        assertEquals("bar", specification.getType());
        assertEquals("foo", specification.getName());
        assertNotNull(specification.getPropertySet());
        assertFalse(specification.getPropertySet().isEmpty());
        assertEquals(2, specification.getPropertySet().get("p1"));
    }

    @Test
    public void readMinimalPropertiesFrom() throws IOException {
        URI uri = getURI
                ("<module type=\"bar\">" +
                        "  <properties>" +
                        "    <property name=\"p1\">2</property>" +
                        "  </properties>" +
                        "</module>");
        ModuleSpecification specification = BlueprintsReader.readBlueprints(uri, uri.toURL().openStream()).getBlueprint("untitled").iterator().next();
        assertEquals("bar", specification.getType());
        assertEquals("bar", specification.getName());
        assertNotNull(specification.getPropertySet());
        assertFalse(specification.getPropertySet().isEmpty());
        assertEquals("2", specification.getPropertySet().get("p1"));
    }

    @Test
    public void readXmlProperties() {
        URI uri = getURI
                ("<module name=\"foo\" type=\"bar\">" +
                        "  <properties>" +
                        "    <xml name=\"p1\">" +
                        "<zip/>" +
                        "    </xml>" +
                        "  </properties>" +
                        "</module> ");
        ModuleSpecification moduleSpecification = readModuleSpec(uri);
        assertTrue(moduleSpecification.getPropertySet().has("p1"));
        assertTrue(moduleSpecification.getPropertySet().get("p1") instanceof Element);
    }

    public static void readMoreXmlProperties() {
        URI uri = getURI
                ("<module name=\"foo\" type=\"bar\">" +
                        "  <properties>" +
                        "    <property name=\"p1\">" +
                        "      <zip/>" +
                        "      <zot/>" +
                        "    </property>" +
                        "  </properties>" +
                        "</module> ");
        ModuleSpecification moduleSpecification = readModuleSpec(uri);
        assertTrue(moduleSpecification.getPropertySet().has("p1"));
        Object p1 = moduleSpecification.getPropertySet().get("p1");
        assertTrue(p1 instanceof Element[]);
        Element[] elements = (Element[]) p1;
        assertEquals(2, elements.length);
        assertEquals("zip", elements[0].getNodeName());
        assertEquals("zot", elements[1].getNodeName());
    }

    @Test
    public void readArrayXmlProperties() {
        URI uri = getURI
                ("<module name=\"foo\" type=\"bar\">" +
                        "  <properties>" +
                        "    <multi-property name=\"p1\" type=\"int\">" +
                        "      <value>1</value>" +
                        "      <value>2</value>" +
                        "      <value>3</value>" +
                        "    </multi-property>" +
                        "  </properties>" +
                        "</module> ");
        ModuleSpecification moduleSpecification = readModuleSpec(uri);
        assertTrue(moduleSpecification.getPropertySet().has("p1"));
        Object p1 = moduleSpecification.getPropertySet().get("p1");
        assertTrue(p1 instanceof int[]);
        int[] ints = (int[]) p1;
        assertEquals(3, ints.length);
        assertEquals(1, ints[0]);
        assertEquals(2, ints[1]);
        assertEquals(3, ints[2]);
    }

    private static ModuleSpecification readModuleSpec(URI uri) {
        return BlueprintsReader.readBlueprints(uri).getBlueprint("untitled").iterator().next();
    }

    @Test
    public void readSingleArrayXmlProperties() {
        URI uri = getURI
                ("<module name=\"foo\" type=\"bar\">" +
                        "  <properties>" +
                        "    <multi-property name=\"p1\" type=\"int\">" +
                        "      <value>2</value>" +
                        "    </multi-property>" +
                        "  </properties>" +
                        "</module> ");
        ModuleSpecification moduleSpecification = readModuleSpec(uri);
        assertTrue(moduleSpecification.getPropertySet().has("p1"));
        Object p1 = moduleSpecification.getPropertySet().get("p1");
        assertNotNull(p1);
        assertTrue(p1 + " was " + p1.getClass(), p1 instanceof int[]);
        assertEquals(1, ((int[]) p1).length);
    }

    @Test
    public void readFeatureProperties() {
        URI uri = getURI
                ("<module name=\"foo\" type=\"bar\">" +
                        "  <inject name=\"feat.\">" +
                        "    <properties>" +
                        "      <property name=\"star1\">lc</property>" +
                        "      <property name=\"producer\">ridikulos</property>" +
                        "    </properties>" +
                        "  </inject>" +
                        "  <expose name=\"foot\">" +
                        "    <properties>" +
                        "      <property name=\"start1\">db</property>" +
                        "    </properties>" +
                        "  </expose>" +
                        "</module> ");
        ModuleSpecification moduleSpecification = readModuleSpec(uri);
        assertNull(moduleSpecification.getFeatureProperties("featuring"));

        PropertySet propertySet = moduleSpecification.getFeatureProperties("feat.");
        assertNotNull(propertySet);
        assertEquals("lc", propertySet.get("star1"));
        assertEquals("ridikulos", propertySet.get("producer"));

        PropertySet properties2 = moduleSpecification.getFeatureProperties("foot");
        assertNotNull(properties2);
        assertEquals("db", properties2.get("start1"));
    }

    @Test
    public void readFeatures() {
        URI uri = getURI
                ("<module name=\"foo\" type=\"bar\">" +
                        "  <inject name=\"inj1\">" +
                        "    <properties>" +
                        "      <property name=\"star1\">lc</property>" +
                        "      <property name=\"producer\">ridikulos</property>" +
                        "    </properties>" +
                        "  </inject>" +
                        "  <expose name=\"exp1\">" +
                        "    <properties>" +
                        "      <property name=\"star1\">db</property>" +
                        "    </properties>" +
                        "  </expose>" +
                        "</module> ");
        ModuleSpecification moduleSpecification = readModuleSpec(uri);

        assertTrue(moduleSpecification.hasFeature(new ModuleSpecificationFeature("inj1", INJECT)));
        assertTrue(moduleSpecification.hasFeature(new ModuleSpecificationFeature("exp1", EXPOSE)));

        assertFalse(moduleSpecification.hasFeature(new ModuleSpecificationFeature("inj2", INJECT)));
        assertFalse(moduleSpecification.hasFeature(new ModuleSpecificationFeature("exp2", EXPOSE)));
    }

    private static URI getURI(String xml) {
        try {
            File file = File.createTempFile("service", ".xml");
            FileOutputStream outputStream = new FileOutputStream(file);
            PrintWriter writer = new PrintWriter(outputStream);
            writer.println("<blueprints xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                " xmlns=\"http://kjetilv.github.com/vanadis/blueprints\"><blueprint>");
            writer.println(xml);
            writer.println("</blueprint></blueprints>");
            assertFalse(writer.checkError());
            outputStream.close();
            return new URI("file://" + file.getAbsolutePath());
        } catch (Exception e) {
            fail(e.toString());
        }
        throw new IllegalStateException("Should not get here");
    }
}
