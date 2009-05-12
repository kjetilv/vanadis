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
package net.sf.vanadis.util.xml;

import junit.framework.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
    public void parseEnum() {
        Element element = fooRoot("<foo bar=\"two\"/>");
        Assert.assertEquals(OneOfTwo.TWO, Xml.enumAttribute(element, OneOfTwo.class, "bar"));
    }

    private static void assertNestZot(Element zot) {
        Assert.assertEquals("bar", Xml.attribute(zot, "zip"));
    }

    private static void assertParseInteger(Element element) {
        Assert.assertEquals(new Integer(2), Xml.integerAttribute(element, "bar"));
    }

    private static Element fooRoot(String xml) {
        Document document = parse(xml);
        return Xml.root(document, "foo");
    }

    private static Document parse(String xml) {
        return Xml.readDocument(xml, Charset.defaultCharset());
    }
}
