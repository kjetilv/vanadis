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

package vanadis.util.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import vanadis.core.collections.Generic;
import vanadis.core.lang.Not;
import vanadis.core.lang.Strings;
import vanadis.core.lang.VarArgs;
import vanadis.core.reflection.Enums;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Xml {

    public static Document readDocument(String string, Charset charset) {
        Not.nil(string, "xml string");
        return readDocument(new ByteArrayInputStream(string.getBytes(charset)));
    }

    public static Document readDocument(String string, String encoding)
            throws UnsupportedEncodingException {
        Not.nil(string, "xml string");
        return readDocument(new ByteArrayInputStream(string.getBytes(encoding)));
    }

    private static DocumentBuilder documentBuilder() {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            return documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new XmlException("Failed to create document builder", e);
        }
    }

    public static Document toDocument(Element root) {
        Document document = documentBuilder().newDocument();
        Node clone = root.cloneNode(true);
        document.adoptNode(clone);
        document.appendChild(clone);
        return document;
    }

    public static Document create(String nodeName, Element... children) {
        return create(nodeName, null, children);
    }

    public static Document create(String nodeName, Map<String, String> attributes, Element... children) {
        Document document = newDocument(nodeName);
        Element documentElement = document.getDocumentElement();
        setAttributes(documentElement, attributes);
        for (Element element : children) {
            documentElement.appendChild(element);
        }
        return document;
    }

    public static void writeDocument(Document doc, OutputStream stream) {
        Source source = new DOMSource(doc);
        Result result = new StreamResult(stream);
        Transformer transformer;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new IllegalStateException("Failed to setup transformer", e);
        }
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new IllegalArgumentException("Failed to write " + doc + " to " + stream, e);
        }
    }

    public static Document readDocument(InputStream stream) {
        Not.nil(stream, "input stream");    
        try {
            return documentBuilder().parse(stream);
        } catch (SAXException e) {
            throw new XmlException("Failed to parse document", e);
        } catch (IOException e) {
            throw new XmlException("Failed to read document from stream", e);
        }
    }

    public static Element requiredChild(Element element, String childName) {
        Not.nil(element, "element");
        Not.nil(childName, "child name");
        Element child = child(element, childName);
        if (child == null) {
            throw new IllegalArgumentException("Expected child element: " + childName);
        }
        return child;
    }

    public static Element child(Element element, String childName) {
        Not.nil(element, "element");
        List<Element> list = childName == null ? list(element.getChildNodes())
                : children(element, childName);
        if (list.size() > 1) {
            throw new IllegalArgumentException
                    ("Expected at most 1 child of " + element.getNodeName() +
                            " named " + childName + ", found " + list.size());
        }
        if (list.isEmpty()) {
            return null;
        }
        Node node = list.get(0);
        return node instanceof Element ? (Element)node
                : Xml.failNonElement(node);
    }

    public static List<Element> list(NodeList childNodes) {
        return Generic.list(new Elements(childNodes));
    }

    public static String requiredAttribute(Element element, String name) {
        return getAttribute(element, name, true, null);
    }

    public static String attribute(Element element, String name) {
        return getAttribute(element, name, false, null);
    }

    public static String attribute(Element element, String name, String defaultValue) {
        return getAttribute(element, name, false, defaultValue);
    }

    public static List<Element> children(Element element) {
        return children(element, null);
    }

    public static List<Element> requiredChildren(Element element, String childName) {
        Not.nil(element, "element");
        Not.nil(childName, "child name");
        List<Element> elements = children(element, childName);
        if (elements.isEmpty()) {
            throw new IllegalArgumentException
                    ("Expected to find children of type " + childName + " in element " + element.getNodeName());
        }
        return elements;
    }

    public static List<Element> children(Element element, String childName) {
        Not.nil(element, "service");
        NodeList childNodes = element.getChildNodes();
        if (childNodes.getLength() == 0) {
            return Collections.emptyList();
        }
        List<Element> children = Generic.list(childNodes.getLength());
        for (Element child : new Elements(childNodes)) {
            if (childName == null || child.getNodeName().equals(childName)) {
                children.add(child);
            }
        }
        return children;
    }

    public static String content(Element element) {
        return getContent(element, false, null);
    }

    public static String content(Element element, String defaultValue) {
        return getContent(element, false, defaultValue);
    }

    public static String requiredContent(Element element) {
        return getContent(element, true, null);
    }

    public static Element root(Document document) {
        return root(document, null);
    }

    public static Element root(Document document, String expected) {
        Not.nil(document, "document");
        Element element = document.getDocumentElement();
        if (expected != null) {
            if (!expected.equals(element.getNodeName())) {
                throw new IllegalArgumentException
                        ("Unexpected root element: " + element.getNodeName() + ", expected: " + expected);
            }
        }
        return element;
    }

    public static boolean booleanAttribute(Element element, String name) {
        return booleanAttribute(element, name, false);
    }

    public static boolean booleanAttribute(Element element, String name, boolean defaultValue) {
        String value = attribute(element, name);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    public static <E extends Enum<?>> E enumAttribute(Element element, Class<E> enumType, String name) {
        return enumAttribute(element, enumType, name, null);
    }

    public static <E extends Enum<?>> E enumAttribute(Element element, Class<E> enumType, String name, E defaultValue) {
        String value = attribute(element, name);
        return value == null ? defaultValue : Enums.getEnum(enumType, value);
    }

    public static Integer integerAttribute(Element element, String name) {
        String value = attribute(element, name);
        return value == null ? null : Integer.parseInt(value);
    }

    public static Element newElement(Document document, Node node, String nodeName) {
        return newElement(document, node, nodeName, null, null);
    }

    public static Element newElement(Document document, Node node, String nodeName, Map<String, String> attributes) {
        return newElement(document, node, nodeName, null, attributes);
    }

    public static Element newElement(Document document, Node node,
                                     String nodeName, String contents,
                                     Map<String, String> attributes) {
        Not.nil(document, "document");
        Not.nil(node, "node");
        Element element = document.createElement(nodeName);
        setAttributes(element, attributes);
        if (!Strings.isBlank(contents)) {
            element.setTextContent(contents);
        }
        node.appendChild(element);
        return element;
    }

    private static Element setAttributes(Element element, Map<String, String> attributes) {
        if (attributes != null) {
            for (String name : attributes.keySet()) {
                element.setAttribute(name, attributes.get(name));
            }
        }
        return element;
    }

    public static Schema readSchema(InputStream inputStream) {
        SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        try {
            return schemaFactory.newSchema(new StreamSource(inputStream));
        } catch (SAXException e) {
            throw new XmlException("Failed to create schema with " + schemaFactory, e);
        }
    }

    public static void validate(Document document, Schema schema) {
        validate(document, schema.newValidator());
    }

    public static void validate(Document document, Validator validator) {
        try {
            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) throws SAXException {
                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                }
            });
            validator.validate(new DOMSource(document));
        } catch (Exception e) {
            throw new XmlException("Unable to validate " + document + " using " + validator, e);
        }
    }

    private static Document newDocument(String rootName) {
        Document document = documentBuilder().newDocument();
        Element child = document.createElement(rootName);
        document.appendChild(child);
        return document;
    }

    private static String getAttribute(Element element, String name, boolean req, String defaultValue) {
        Not.nil(element, "element");
        Not.nil(name, "name");
        String attribute = element.getAttribute(name);
        if (nonEmpty(attribute)) {
            return attribute;
        }
        if (nonEmpty(defaultValue)) {
            return defaultValue;
        }
        if (req) {
            throw new IllegalArgumentException
                    ("No attribute " + name + " in element " + element.getNodeName());
        }
        return null;
    }

    private static boolean nonEmpty(String attribute) {
        return !Strings.isEmpty(attribute);
    }

    private static Element failNonElement(Node node) {
        throw new IllegalArgumentException
                ("Found non-Element " + node + (node == null ? "" : " of " + node.getClass()));
    }

    private static String getContent(Element element, boolean req, String defaultValue) {
        Not.nil(element, "element");
        String content = element.getTextContent();
        if (nonEmpty(content)) {
            return content;
        }
        if (nonEmpty(defaultValue)) {
            return defaultValue;
        }
        if (req) {
            throw new IllegalStateException
                    ("No content found in element " + element.getNodeName());
        }
        return null;
    }

    public static String content(Document document, String... path) {
        if (!VarArgs.present(path)) {
            return null;
        }
        Element element = document.getDocumentElement();
        for (int i = 1; i < path.length; i++) {
            element = Xml.child(element, path[i]);
            if (element == null || !path[i].equals(element.getNodeName())) {
                return null;
            }
        }
        return element.getTextContent();
    }
}
