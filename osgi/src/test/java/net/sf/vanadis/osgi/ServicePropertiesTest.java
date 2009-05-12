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
package net.sf.vanadis.osgi;

import static junit.framework.Assert.*;
import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.properties.PropertySet;
import net.sf.vanadis.core.properties.PropertySets;
import org.junit.Test;
import org.osgi.framework.Constants;

import java.util.Arrays;

public class ServicePropertiesTest {

    @Test
    public void untypedServiceProperties() {
        ServiceProperties<?> sp = ServiceProperties.create(Object.class);
        assertEquals(Object.class, sp.getMainClass());
        assertNull(Object.class.getName(), sp.getMainClassName());
    }

    @Test
    public void typedServicePropertiesWithObjectClasses() {
        ServiceProperties<?> sp = ServiceProperties.create(String.class);
        ServiceProperties<?> serviceProperties = sp.with(extraProperties());
        assertEquals(String.class.getName(), serviceProperties.getMainClassName());
        assertObjectClasses(serviceProperties.getObjectClasses());
        assertEquals(new Long(5), serviceProperties.getServiceId());
        assertEquals("string2", serviceProperties.getServicePid());
    }

    @Test
    public void servicePropertiesType() {
        ServiceProperties<String> strPrp = ServiceProperties.create
                (String.class, Integer.class);
        assertEquals(String.class, strPrp.getMainClass());
        assertEquals(String.class.getName(), strPrp.getMainClassName());
        assertTrue(strPrp.isTyped(String.class));
        assertTrue(strPrp.isTyped(Integer.class));
    }

    @Test
    public void servicePropertiesProperties() {
        ServiceProperties<String> strPrp = ServiceProperties.create
                (String.class,
                 PropertySets.create("foo", true));
        assertTrue((Boolean) strPrp.getPropertySet().get("foo"));
    }

    @Test
    public void servicePropertiesOSGiStuff() {
        ServiceProperties<String> strPrp = ServiceProperties.create
                (String.class,
                 stringTwoProperties());
        assertEquals(new Long(2), strPrp.getServiceId());
        assertEquals("string", strPrp.getServicePid());
    }

    @Test
    public void servicePropertiesWithObjectClasses() {
        ServiceProperties<String> strPrp = ServiceProperties.create
                (String.class, Integer.class, Long.class);
        assertObjectClasses(strPrp.getObjectClasses());
    }

    @Test
    public void servicePropertiesOSGiStuffAndObjectClasses() {
        ServiceProperties<String> strPrp = ServiceProperties.create
                (String.class,
                 stringTwoProperties(),
                 Integer.class,
                 Long.class);
        assertEquals(new Long(2), strPrp.getServiceId());
        assertEquals("string", strPrp.getServicePid());
        assertObjectClasses(strPrp.getObjectClasses());
    }

    private static PropertySet stringTwoProperties() {
        return PropertySets.create(Generic.map(Constants.SERVICE_ID, 2,
                                                 Constants.SERVICE_PID, "string"));
    }

    @Test
    public void adoptServiceReferences() {
        ServiceProperties<String> strPrp = ServiceProperties.create(String.class).with
                (extraProperties());
        assertEquals(new Long(5), strPrp.getServiceId());
        assertEquals("string2", strPrp.getServicePid());
        String[] classes = strPrp.getObjectClasses();
        assertObjectClasses(classes);
    }

    private static PropertySet extraProperties() {
        return PropertySets.create(Generic.map
                (Constants.SERVICE_ID, 5L,
                 Constants.SERVICE_PID, "string2",
                 Constants.OBJECTCLASS, new String[]{String.class.getName(),
                                                     Integer.class.getName(),
                                                     Long.class.getName()}));
    }

    @Test
    public void rejectAdoptServiceReference() {
        ServiceProperties<String> serviceProperties = ServiceProperties.create(String.class, Short.class);
        PropertySet extraPropertySet = extraProperties();
        try {
            fail(serviceProperties.with(extraPropertySet) + " should not exist!");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void adoptObjectClassesWithoutObjectClasses() {
        ServiceProperties<String> serviceProperties = ServiceProperties.create(String.class);
        ServiceProperties<String> appendedServiceProperties =
                serviceProperties.with(PropertySets.create("foo", "bar"));
        assertNotNull(appendedServiceProperties);
        assertEquals(String.class, appendedServiceProperties.getMainClass());
    }

    private static void assertObjectClasses(String[] classes) {
        assertEquals(Arrays.toString(classes), 3, classes.length);
        assertEquals(String.class.getName(), classes[0]);
        assertEquals(Integer.class.getName(), classes[1]);
        assertEquals(Long.class.getName(), classes[2]);
    }
}
