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
package vanadis.mvn.xml;

import static org.junit.Assert.*;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import vanadis.common.io.Closeables;
import vanadis.core.system.VM;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

public class XmlTest {

    private static final Element FOO_BAR_2 = fooRoot("<foo bar=\"2\"/>");

    private static final Document NEST = parse
            ("<foo bar=\"2\">" +
             "  <zot zip=\"bar\"/>" +
             "</foo>");

    @Test
    public void parseNest() {
        Element foo = Xml.root(NEST, "foo");
        assertParseInteger(foo);
        Element zot = Xml.child(foo, "zot");
        assertNestZot(zot);
    }

    @Test
    public void parseInteger() {
        assertParseInteger(FOO_BAR_2);
    }

    public enum OneOfTwo {
        ONE, TWO
    }

    @Test
    public void toDocument() {
        Document document = Xml.toDocument(FOO_BAR_2);
        assertParseInteger(document.getDocumentElement());
    }

    @Test
    public void toDocument2() {
        Document document = Xml.toDocument(Xml.child(Xml.root(NEST), "zot"));
        assertNestZot(Xml.root(document));
    }

    @Test
    public void getContent() {
        Document document = parse
                ("<foo>" +
                 "  <bar>" +
                 "    <zot>aha</zot>" +
                 "  </bar>" +
                 "</foo>");
        assertNotNull(Xml.content(document, "foo", "bar", "zot"));
        assertEquals("aha", Xml.content(document, "foo", "bar", "zot"));
    }

    @Test
    public void parseEnum() {
        Element element = fooRoot("<foo bar=\"two\"/>");
        assertEquals(OneOfTwo.TWO, Xml.enumAttribute(element, OneOfTwo.class, "bar"));
    }

    private static void assertNestZot(Element zot) {
        assertEquals("bar", Xml.attribute(zot, "zip"));
    }

    private static void assertParseInteger(Element element) {
        assertEquals(new Integer(2), Xml.integerAttribute(element, "bar"));
    }

    private static Element fooRoot(String xml) {
        Document document = parse(xml);
        return Xml.root(document, "foo");
    }

    private static Document parse(String xml) {
        return Xml.readDocument(xml, Charset.defaultCharset());
    }

    @Test
    public void writeDoc() {
        Document doc = Xml.create("foo");
        Xml.newElement(doc, doc.getDocumentElement(), "bar", "zot", null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Xml.writeDocument(doc, baos);
        Closeables.close(baos);
        String str = new String(baos.toByteArray());
        assertTrue("Wrong doc: " + str, str.contains("<foo>" + VM.LN + "<bar>zot</bar>" + VM.LN + "</foo>"));
    }
}
