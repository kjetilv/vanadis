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

import vanadis.blueprints.ModuleSpecification;
import vanadis.common.io.Location;
import vanadis.core.properties.PropertySet;
import vanadis.core.properties.PropertySets;
import vanadis.core.system.VM;
import vanadis.ext.Configuration;
import vanadis.ext.Configure;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import java.io.File;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;

@SuppressWarnings({"UnusedDeclaration"})
public class ConfigureTest extends ObjectManagementTestCase {

    static class ConfiguredLocation {

        @Configure
        private Location location;

        public Location getLocation() {
            return location;
        }
    }

    static class ConfiguredFile {

        @Configure
        private File file;
    }

    static class ConfiguredFileMethod {

        @Configure
        private File file;
    }

    @Test
    public void configureFile() {
        ConfiguredFile object = new ConfiguredFile();
        manageConf(object, PropertySets.create("file", VM.TMP.toString()));
        assertEquals(VM.TMP, object.file);
    }

    private void manageConf(Object object, PropertySet propertySet) {
        manage(ModuleSpecification.create
                ("cnf", "cnf",
                 propertySet),
               object);
    }

    @Test
    public void configureFileMethod() {
        ConfiguredFileMethod object = new ConfiguredFileMethod();
        manageConf(object, PropertySets.create("file", VM.TMP.toString()));
        assertEquals(VM.TMP, object.file);
    }

    static class ConfiguredInt {

        @Configure
        private int foo;
    }

    @Test
    public void configureIntegerMethod() {
        ConfiguredInt object = new ConfiguredInt();
        getContext().setProperty("two", "2");
        manageConf(object, PropertySets.create("foo", "1${two}"));
        assertEquals(12, object.foo);
    }

    static class PropertiesConfigured {

        @Configuration
        private PropertySet propertySet;
    }

    @Test
    public void propertiesConfigured() {
        PropertiesConfigured object = new PropertiesConfigured();
        manageConf(object, PropertySets.create("foo", "bar"));
        assertNotNull(object.propertySet);
        assertEquals("bar", object.propertySet.get("foo"));
    }

    static class JavaPropertiesConfigured {

        @Configuration
        private Properties properties;
    }

    @Test
    public void javaPropertiesConfigured() {
        JavaPropertiesConfigured object = new JavaPropertiesConfigured();
        manageConf(object, PropertySets.create("foo", "bar"));
        assertNotNull(object.properties);
        assertEquals("bar", object.properties.getProperty("foo"));
    }

    static class DictionaryConfigured {

        @Configuration
        private Dictionary<String, String> dictionary;
    }

    @Test
    public void dictionaryConfigured() {
        DictionaryConfigured object = new DictionaryConfigured();
        manageConf(object, PropertySets.create("foo", "bar"));
        assertNotNull(object.dictionary);
        assertEquals("bar", object.dictionary.get("foo"));
    }

    static class MapConfigured {

        @Configuration
        private Map<String, String> map;
    }

    @Test
    public void mapConfigured() {
        MapConfigured object = new MapConfigured();
        manageConf(object, PropertySets.create("foo", "bar"));
        assertNotNull(object.map);
        assertEquals("bar", object.map.get("foo"));
    }

    @Test
    public void mapConfiguredWithReplace() {
        MapConfigured object = new MapConfigured();
        getContext().setProperty("oz", "ar");
        manageConf(object, PropertySets.create("foo", "b${oz}"));
        assertNotNull(object.map);
        assertEquals("bar", object.map.get("foo"));
    }

    @Test
    public void locationResolved() {
        ConfiguredLocation configuredLocation = new ConfiguredLocation();
        getContext().setProperty("properties.location", location.toLocationString());
        manageConf(configuredLocation, PropertySets.create("location", "${properties.location}"));
        assertNotNull("No location set", configuredLocation.getLocation());
        assertEquals(location, configuredLocation.getLocation());
    }

    @Test
    public void locationResolvedFallback() {
        ConfiguredLocation configuredLocation = new ConfiguredLocation();
        System.setProperty("properties.location", location.toLocationString());
        try {
            manageConf(configuredLocation, PropertySets.create("location", "${properties.location}"));
            assertNotNull("No location set", configuredLocation.getLocation());
            assertEquals(location, configuredLocation.getLocation());
        } finally {
            System.getProperties().setProperty("properties.location", "");
        }
    }

    @Test
    public void locationResolvedWithPlus() {
        ConfiguredLocation configuredLocation = new ConfiguredLocation();
        manageConf(configuredLocation, PropertySets.create("location", "baseport+80"));
        assertEquals(new Location("localhost", location.getPort() + 80), configuredLocation.getLocation());
    }
}
