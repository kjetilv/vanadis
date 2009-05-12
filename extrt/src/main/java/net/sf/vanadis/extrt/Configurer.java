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

package net.sf.vanadis.extrt;

import net.sf.vanadis.annopro.AnnotationDatum;
import net.sf.vanadis.core.io.Location;
import net.sf.vanadis.core.lang.Strings;
import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.core.properties.PropertySet;
import net.sf.vanadis.core.properties.PropertySets;
import net.sf.vanadis.core.reflection.GetNSet;
import net.sf.vanadis.core.reflection.Invoker;
import net.sf.vanadis.ext.Configuration;
import net.sf.vanadis.ext.Configure;
import net.sf.vanadis.ext.ModuleSystemException;
import net.sf.vanadis.util.log.Log;
import net.sf.vanadis.util.log.Logs;
import net.sf.vanadis.util.xml.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;

class Configurer {

    private static final Log log = Logs.get(Configurer.class);

    private final AnnotationDatum<?> datum;

    private final Object managed;

    private final Location location;

    private final PropertySet moduleSpecificationPropertySet;

    private final String propertyName;

    private final String defaultValue;

    private final Class<?> propertyType;

    private final AnnotatedElement element;

    private final boolean required;

    private final boolean setNull;

    private Object finalValue;

    Configurer(Object managed, Location location, AnnotationDatum<?> datum,
               PropertySet moduleSpecificationPropertySet) {
        this.managed = managed;
        this.location = location;
        this.moduleSpecificationPropertySet = moduleSpecificationPropertySet == null
                ? PropertySets.EMPTY
                : moduleSpecificationPropertySet.copy(false);
        this.datum = datum;
        element = datum.getElement();
        propertyType = typeOf(element);
        if (datum.isType(Configure.class)) {
            Configure configure = datum.createProxy(getClass().getClassLoader(), Configure.class);
            required = configure.required();
            propertyName = propertyName(element, configure.name());
            defaultValue = defaultValue(configure);
            setNull = configure.setNull();
        } else {
            Configuration configuration = datum.createProxy(getClass().getClassLoader(), Configuration.class);
            required = configuration.required();
            propertyName = propertyName(element, configuration.name());
            defaultValue = null;
            setNull = true;
        }
    }

    void set(PropertySet variables) {
        if (datum.isType(Configure.class)) {
            setValue(managed, variables);
        } else {
            PropertySet propertySet = moduleSpecificationPropertySet.expand(variables);
            setProperties(managed, propertySet, datum);
        }
    }

    Object getFinalValue() {
        return finalValue;
    }

    String getPropertyName() {
        return propertyName;
    }

    Class<?> getPropertyType() {
        return propertyType;
    }

    private <A extends AnnotatedElement> void setProperties(Object configured,
                                                            PropertySet propertySet,
                                                            AnnotationDatum<A> datum) {
        A element = datum.getElement();
        Class<?> type = typeOf(element);
        Object properties = convert(propertySet, type);
        set(configured, null, properties);
    }

    private void setValue(Object configured, PropertySet variables) {
        boolean xml = Node.class.isAssignableFrom(propertyType);
        String annotatedDefault = portAdjusted(defaultValue);
        if (xml) {
            Node document = getXml();
            if (document != null) {
                setPropertyXml(configured, document);
            }
        } else {
            Object resolvedValue = resolveValue(variables);
            if (resolvedValue != null) {
                setPropertyValue(configured, false, variables, resolvedValue);
                return;
            }
        }
        if (!(annotatedDefault == null && required)) {
            if (annotatedDefault != null || setNull) {
                setPropertyValue(configured, xml, variables, annotatedDefault);
            } else {
                log.info(this + " found no value for " + configured + "." + propertyName);
            }
        } else {
            throw new ModuleSystemException
                    ("Required property " + propertyName + " not set for " + configured);
        }
    }

    private Object resolveValue(PropertySet variables) {
        Object resolvedValue;
        if (propertyType == String.class) {
            resolvedValue = moduleSpecificationPropertySet.get(propertyName, variables);
        } else if (propertyType == Location.class) {
            String stringValue = Strings.neitherNullNorEmpty
                    (moduleSpecificationPropertySet.get(propertyName, variables));
            String value = stringValue == null ? defaultValue : stringValue;
            resolvedValue = portAdjusted(value);
        } else {
            resolvedValue = moduleSpecificationPropertySet.get(propertyName);
        }
        return resolvedValue;
    }

    private Object convert(PropertySet set, Class<?> type) {
        if (type == PropertySet.class) {
            return set;
        }
        if (type == Map.class || type == HashMap.class) {
            return set.toMap();
        }
        if (type == Properties.class) {
            return set.toProperties();
        }
        if (type == Dictionary.class) {
            return set.toDictionary();
        }
        if (type == Hashtable.class) {
            return set.toHashtable();
        }
        throw new IllegalStateException(this + " could not convert properties to " + type + ": " + set);
    }

    private static <A extends AnnotatedElement> Class<?> typeOf(A element) {
        return element instanceof Method
                ? ((Method) element).getParameterTypes()[0]
                : ((Field) element).getType();
    }

    private Node getXml() {
        return moduleSpecificationPropertySet.has(Node.class, propertyName)
                ? moduleSpecificationPropertySet.get(Node.class, propertyName)
                : null;
    }

    private void setPropertyXml(Object configured, Node node) {
        boolean isElement = Element.class.isAssignableFrom(propertyType);
        boolean isDocument = Document.class.isAssignableFrom(propertyType);
        if (log.isDebug()) {
            log.debug(MessageFormat.format
                    ("{0}.{1}({2}) => target: {3})",
                     configured.getClass().getSimpleName(), propertyName, isElement, configured));
        }
        Object value = isDocument && node instanceof Document ? node
                : isDocument && node instanceof Element ? toDocument((Element) node)
                        : isElement && node instanceof Element ? node
                                : isElement && node instanceof Document ? documentElement(node)
                                        : null;
        if (value != null) {
            set(configured, null, value);
        } else {
            throw new IllegalArgumentException
                    (this + " got value of " + node.getClass() + " for argument of " + propertyType);
        }
    }

    private static Document toDocument(Element original) {
        return Xml.toDocument(original);
    }

    private static Element documentElement(Node node) {
        return ((Document) node).getDocumentElement();
    }

    private void setPropertyValue(Object configured, boolean xml, PropertySet variables,
                                  Object finalValue) {
        if (log.isDebug()) {
            log.debug(MessageFormat.format
                    ("{0}.{1}({2}) => target: {3})",
                     configured.getClass().getSimpleName(), propertyName, finalValue, configured));
        }
        if (xml) {
            set(configured, null, finalValue);
        } else {
            set(configured, variables, finalValue);
        }
    }

    private void set(Object configured, PropertySet variables, Object finalValue) {
        try {
            if (element instanceof Method) {
                Method method = (Method) element;
                if (variables == null) {
                    Invoker.invoke(this, configured, method, finalValue);
                } else {
                    Invoker.invokeReplacedCoerced(this, configured, method, variables, finalValue);
                }
            } else {
                Field field = (Field) element;
                if (variables == null) {
                    Invoker.assign(this, configured, field, finalValue);
                } else {
                    Invoker.assignReplacedCoerced(this, configured, field, variables, finalValue);
                }
            }
        } finally {
            this.finalValue = finalValue;
        }
    }

    private String portAdjusted(String value) {
        if (propertyType.equals(Location.class) && value != null) {
            Location adjustedPort = PortUtils.resolveLocation(this.location, value);
            return adjustedPort.toLocationString();
        }
        return value;
    }

    private static String propertyName(AnnotatedElement object, String annotatedName) {
        String name = object instanceof Method ? ((Method) object).getName() : ((Field) object).getName();
        return annotatedName.trim().length() == 0
                ? GetNSet.resolveByPrefix(name, true, "set")
                : annotatedName;
    }

    public static String defaultValue(Configure configure) {
        String value = configure.def().trim();
        return value.length() == 0 ? null : value;
    }

    @Override
    public String toString() {
        return ToString.of(this, moduleSpecificationPropertySet);
    }
}