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

package vanadis.extrt;

import junit.framework.Assert;
import vanadis.blueprints.ModuleSpecification;
import vanadis.core.collections.Generic;
import vanadis.core.io.Location;
import vanadis.core.properties.PropertySet;
import vanadis.core.properties.PropertySets;
import vanadis.ext.Configure;
import vanadis.ext.Expose;
import vanadis.ext.Inject;
import vanadis.osgi.Context;
import vanadis.osgi.ContextListener;
import vanadis.osgi.impl.BareBonesContext;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"RawUseOfParameterizedType"})
@RunWith(JUnit4ClassRunner.class)
public class ModuleTest {

    private final List<ContextListener<Map>> mapListeners = Generic.list();

    private final Map<?, ?> map = Generic.map();

    private final List<?> list = Generic.list();

    @Before
    public void setup() {
    }

    @After
    public void teardown()
            throws Exception {
        mapListeners.clear();
        map.clear();
    }

    public static class ListPusher {

        private List<?> list;

        public ListPusher(List<?> list) {
            this.list = list;
        }

        @Expose
        public List<?> getList() {
            return list;
        }

    }

    public static class MapJunkie {

        private Map<?, ?> map;

        @Inject
        public void setMap(Map<?, ?> map) {
            this.map = map;
        }

        public boolean isMapped() {
            return map != null;
        }

    }

    @Test
    public void injected() {
        BareBonesContext context = new BareBonesContext();
        MapJunkie mapJunkie = new MapJunkie();
        ObjectManagerImpl.create(context, null, null, mapJunkie,
                                 null, null);
        Set<ContextListener<Map>> listeners = context.listeners(Map.class);
        assertFalse("No listeners for " + Map.class, listeners.isEmpty());
        context.register(map, Map.class);
        assertTrue(mapJunkie.isMapped());
    }

    @Test
    public void exposed() {
        BareBonesContext context = new BareBonesContext();
        ObjectManagerImpl.create(context, null, null, new ListPusher(list),
                                 null, null);
        assertEquals(4, context.registrationCount());
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static final class FieldSetObject {

        @Configure
        private String string;

        @Configure
        private int number;

        public String getString() {
            return string;
        }

        public int getNumber() {
            return number;
        }
    }

    @Test
    public void setFieldConfigured() {
        PropertySet vars = PropertySets.create("foo", "5");
        BareBonesContext ctx = new BareBonesContext(vars);
        FieldSetObject fieldSetObject = new FieldSetObject();
        ObjectManagerImpl.create(ctx, ModuleSpecification.create("fielder", "fielder",
                                                                 PropertySets.create("string", "kjetil",
                                                                                     "number", "${foo}")),
                                 null, fieldSetObject,
                                 null, null);
        Assert.assertEquals("kjetil", fieldSetObject.getString());
        Assert.assertEquals(5, fieldSetObject.getNumber());
    }

    public static final class Configured {

        private String integer;

        private Object zot;

        private Element document;

        private String namedZip;

        private EnumPenum fooOrBar;

        public Element getDirectDocument() {
            return directDocument;
        }

        private Element directDocument;

        public Object getZot() {
            return zot;
        }

        public Object getZip() {
            return zip;
        }

        private Object zip;

        public Element getDocument() {
            return document;
        }

        @Configure
        public void setXml(Element document) {
            this.document = document;
        }

        @Configure
        public void setDirectXml(Element document) {
            this.directDocument = document;
        }

        @Configure(name = "theInteger")
        public void setInteger(String integer) {
            this.integer = integer;
        }

        public String getInteger() {
            return integer;
        }

        @Configure
        public void setZot(String zot) {
            this.zot = zot;
        }

        @Configure
        public void setZip(String zip) {
            this.zip = zip;
        }

        @Configure
        public void setNamedZip(String namedZip) {
            this.namedZip = namedZip;
        }

        @Configure
        public void setFooOrBar(EnumPenum fooOrBar) {
            this.fooOrBar = fooOrBar;
        }

        public EnumPenum getFooOrBar() {
            return fooOrBar;
        }

        public String getNamedZip() {
            return namedZip;
        }
    }

    @Test
    public void configurationLaunchSpec()
            throws ParserConfigurationException {
        Element document =
                DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("foo");
        Element directDocument =
                DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("foo");

        PropertySet propertySet = PropertySets.create
                ("theInteger", "5").set
                ("directXml", directDocument).set
                ("xml", document).set
                ("zot", "ZOT").set
                ("zip", "ZIP").set
                ("fooOrBar", "BAR").set
                ("namedZip", "namedZIP");
        Context ctx = new BareBonesContext(propertySet);

        Configured configured = new Configured();
        ObjectManagerImpl.create(ctx, ModuleSpecification.create("configred", "configuree", propertySet), null,
                                 configured,
                                 null, null);

        assertNotNull(configured + " has not received xml document", configured.getDocument());
        assertSame(document, configured.getDocument());
        assertNotNull(configured.getDirectDocument());
        assertSame(directDocument, configured.getDirectDocument());
        assertNotNull(configured.getZip());
        assertEquals("ZIP", configured.getZip());
        assertNotNull(configured.getZot());
        assertNotNull(configured.getInteger());
        assertNotNull(configured.getNamedZip());
        assertNotNull(configured.getFooOrBar());
        assertEquals(EnumPenum.BAR, configured.getFooOrBar());
    }

    public static final class Located {

        private Location absolute;

        private Location relative;

        private Location replacedRelative;

        private Location[] list;

        @Configure(required = true)
        public void setAbsolute(Location absolute) {
            this.absolute = absolute;
        }

        @Configure(required = true)
        public void setReplacedRelative(Location replacedRelative) {
            this.replacedRelative = replacedRelative;
        }

        @Configure(required = true)
        public void setRelative(Location relative) {
            this.relative = relative;
        }

        @Configure(required = true)
        public void setList(Location[] list) {
            this.list = list;
        }

        public Location[] getList() {
            return list;
        }

        public Location getAbsolute() {
            return absolute;
        }

        public Location getRelative() {
            return relative;
        }

        public Location getReplacedRelative() {
            return replacedRelative;
        }

    }

    @Test
    public void getLocationsFromLaunchSpec() {
        PropertySet propertySet = locationsResource();
        Location location = Location.parse("host2:8000");

        BareBonesContext bareBonesContext = new BareBonesContext
                (PropertySets.create(Generic.map("delta", "80")), location);
        Located managed = new Located();

        ObjectManagerImpl.create(bareBonesContext, ModuleSpecification.create("located", "located", propertySet),
                                 null, managed,
                                 null, null);

        assertEquals(new Location("host1", 8080), managed.getAbsolute());
        assertEquals(new Location("host2", 8080), managed.getReplacedRelative());
        assertEquals(new Location("host2", 8080), managed.getRelative());
        assertEquals(new Location("host1", 1000), managed.getList()[0]);
        assertEquals(new Location("host2", 2000), managed.getList()[1]);
    }

    private static PropertySet locationsResource() {
        return PropertySets.create
                (Generic.map("absolute", "host1:8080",
                             "replacedRelative", "baseport + ${delta}",
                             "relative", "baseport+ 80",
                             "list", "host1:1000,host2:2000"));
    }

}
