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

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertSame;

import org.junit.Test;
import vanadis.core.collections.Generic;
import vanadis.core.properties.PropertySet;
import vanadis.ext.*;
import vanadis.objectmanagers.ObjectManager;
import vanadis.objectmanagers.ObjectManagerExposePointMBean;
import vanadis.objectmanagers.ObjectManagerMBean;
import vanadis.osgi.*;
import vanadis.osgi.impl.BonyRegistration;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ObjectManagerImplTest extends ObjectManagementTestCase {

    @Test
    public void simpleObjectManager() {
        Object managed = new RemotableInjectee();
        ObjectManager manager = manage(managed);
        Map<ContextListener<?>, Filter> filterMap = getContext().getListenerFilters();
        assertNotNull("No listeners registered", filterMap);
        assertFalse("Empty listeners set, was not registered: " + managed, filterMap.isEmpty());
        manager.launch();
    }

    @Test
    public void exposeWithProperties() {
        manage(new PropertyExposer());
        assertEquals(4,
                     getContext().registrationCount()); // Includes object manager itself, and jmx whiteboard registrations
        BonyRegistration<TestService> registration = getContext().registrations(TestService.class).get(0);
        Object navn = registration.getServiceProperties().getPropertySet().get("navn");
        assertEquals("ola nordmann", navn);
        assertEquals(1, registration.getServiceProperties().getObjectClasses().length);
        assertEquals(TestService.class.getName(),
                     registration.getServiceProperties().getObjectClasses()[0]);
    }

    @Test
    public void exposeWithPropertiesAndObjectClasses() {
        manage(new ClassyPropertyExposer());
        assertEquals(6, getContext().registrationCount()); // Incl. ObjectManager itself, jmx regs.
        for (Registration<?> registration : getContext()) {
            ServiceProperties<?> serviceProperties = registration.getServiceProperties();
            PropertySet propertySet = serviceProperties.getPropertySet();
            if (propertySet.has("gift")) {
                Boolean gift = propertySet.is("gift", true);
                assertTrue(gift);
            } else if (propertySet.has("navn")) {
                Object name = propertySet.get("navn");
                String[] classes = serviceProperties.getObjectClasses();
                assertEquals(TestService.class.getName(), classes[0]);
                assertEquals(Cloneable.class.getName(), classes[1]);
                assertEquals("ola nordmann", name);
            } else {
                Class<?> mainClass = serviceProperties.getMainClass();
                if (propertySet.has(CoreProperty.OBJECTNAME_NAME)) {
                    assertTrue(ObjectManagerMBean.class == mainClass ||
                            ObjectManagerExposePointMBean.class == mainClass);
                } else {
                    assertEquals(ObjectManager.class, mainClass);
                }
            }
        }
    }

    @Test
    public void remotableExchange() {
        RemotableExposer remotableExposer = new RemotableExposer();
        manage(remotableExposer);
        RemotableInjectee remotableInjectee = new RemotableInjectee();
        manage(remotableInjectee);

        assertSame(remotableExposer.getLastExposed(), remotableInjectee.getLastAdded());
    }

    @Test
    public void interfaceExposer() {
        manage(new VanillaImplExposer());
        List<BonyRegistration<TestService>> registrationList = getContext().registrations(TestService.class);
        assertEquals(1, registrationList.size());
    }

    @Test
    public void specificExposer() {
        manage(new SpecificExposer());
        List<BonyRegistration<TestService>> registrationList = getContext().registrations(TestService.class);
        assertEquals(1, registrationList.size());
        BonyRegistration<?> registration = registrationList.iterator().next();
        assertEquals(TestService.class, registration.getServiceProperties().getMainClass());
    }

    @Test
    public void receiveRegistration() {
        ReferenceInjectee referenceInjectee = new ReferenceInjectee();
        manage(referenceInjectee);
        RemotableExposer exposer = new RemotableExposer();
        manage(exposer);

        assertNotNull(referenceInjectee.getTestService());
        assertNotNull(referenceInjectee.getPropertySet());
        assertNotNull(referenceInjectee.getTestServiceRef());

        assertNotNull(referenceInjectee.getPropertiedTestServiceRef());
        assertNotNull(referenceInjectee.getRefProperties());
    }

    @Test
    public void receiveObject() {
        assertInjectedObject(new ObjectInjectee());
    }

    @Test
    public void receiveFieldObject() {
        assertInjectedObject(new FieldObjectInjectee());
    }

    private void assertInjectedObject(GetObject<?> objectInjectee) {
        manage(objectInjectee);
        manage(new RemotableExposer());
        assertNotNull(objectInjectee.getObject());
    }

    @Test
    public void exposeWithRuntimeProperties() {
        manage(new RuntimePropertiesExposer());
        List<BonyRegistration<TestService>> registrations = getContext().registrations(TestService.class);
        assertEquals(1, registrations.size());
        PropertySet propertySet = registrations.get(0).getServiceProperties().getPropertySet();
        assertTrue(registrations.get(0) + " did not have the foo property provided by the instance of " +
                RuntimePropertiesExposer.class,
                   propertySet.has("foo"));
        assertEquals("bar", propertySet.get("foo"));
    }

    @Test
    public void exposeRequiredAndOptional() {
        manage(new EasyGoingExposer());
        assertEquals(6, getContext().registrationCount()); // Incl. ObjectManager itself, jmx regs...
    }

    @Test
    public void propertiedInjector() {
        PropertiedInjector injector = new PropertiedInjector();
        manage(injector);
        assertEquals(1, getContext().listenerCount());
        assertNull(injector.getHrService());
        getContext().register(new TestService() {
        }, TestService.class);
        assertNull("HR service injected: " + injector.getHrService(), injector.getHrService());
        getContext().register(new TestService() { },
                              ServiceProperties.create(TestService.class,
                                                       Generic.map("foo", 1)));
        assertNull(injector.getHrService());
        getContext().register(new TestService() { },
                              ServiceProperties.create(TestService.class,
                                                       Generic.map("foo", 1,
                                                                   "bar", false)));
        assertNull(injector.getHrService());
        getContext().register(new TestService() { },
                              ServiceProperties.create(TestService.class,
                                                       Generic.map("foo", 2,
                                                                   "bar", true)));
        assertNull(injector.getHrService());
        TestService hit = new TestService() { };
        getContext().register(hit,
                              ServiceProperties.create(TestService.class,
                                                       Generic.map("foo", 1,
                                                                   "bar", true)));
        assertNotNull(injector.getHrService());
        assertSame(hit, injector.getHrService());
    }

    @Test
    public void lordBaltimore() {
        Apache apache = new Apache();
        assertApache(apache, apache.testServices(), TestService.class);
    }

    @Test
    public void lordBaltimoreRef() {
        ApacheReference apache = new ApacheReference();
        assertApache(apache, apache.testServices, Reference.class);
    }

    @Test
    public void lordBaltimoreField() {
        FieldApache apache = new FieldApache();
        assertApache(apache, apache.testServices, TestService.class);
    }

    private void assertApache(Object managed, Collection<?> services, Class<?> type) {
        manage(managed);
        assertTrue("Not empty: " + services, services.isEmpty());
        manage(new VanillaExposer());
        assertFalse("Services still empty: " + services, services.isEmpty());
        assertEquals(1, services.size());
        assertType(services, type);
        ObjectManager objectManager = manage(new VanillaExposer());
        assertEquals(2, services.size());
        assertType(services, type);
        objectManager.shutdown();
        assertEquals(1, services.size());
    }

    private static void assertType(Collection<?> services, Class<?> type) {
        for (Object service : services) {
            assertTrue(type.isInstance(service));
        }
    }

    @Test
    public void injectHeritage() {
        InheritedInject inject = new InheritedInject();
        manage(inject);
        assertEquals(1, getContext().listenerCount());
        assertNull(inject.getService());
        getContext().register(new TestService() {
        }, TestService.class);
        assertNotNull(inject.getService());
    }

    @Test
    public void blotMe() {
        InheritedInject inheritedInject = new InheritedInject();
        manage(inheritedInject);
        assertNull(inheritedInject.getService());
        manage(new Blotter());
        assertNotNull(inheritedInject.getService());
    }

    @Test
    public void autoConstructInject() {
        ConstructorExposer.instance = null;
        manage(null, ConstructorExposer.class);
        assertNull(ConstructorExposer.instance);
        getContext().register(new TestService() {}, TestService.class);
        assertNotNull("Should have instanceof " + ConstructorExposer.class, ConstructorExposer.instance);
        List<BonyRegistration<TestService2>> registrations = getContext().registrations(TestService2.class);
        assertNotNull(registrations);
        assertEquals(1, registrations.size());
    }

    @Test
    public void autoConstructInjectAndDestroyUnject() {
        ConstructorExposer.instance = null;
        manage(null, ConstructorExposer.class);
        Registration<TestService> registration = getContext().register(new TestService() { }, TestService.class);
        assertNotNull("Should have instanceof " + ConstructorExposer.class, ConstructorExposer.instance);
        assertFalse(getContext().registrations(TestService2.class).isEmpty());
        registration.unregister();
        assertTrue(getContext().registrations(TestService2.class).isEmpty());
    }

    @Test
    public void autoConstructInjectAndDestroyUnjectAndAgain() {
        ConstructorExposer.instance = null;
        manage(null, ConstructorExposer.class);
        Registration<TestService> registration = getContext().register(new TestService() { }, TestService.class);
        registration.unregister();
        assertTrue(getContext().registrations(TestService2.class).isEmpty());
        getContext().register(new TestService() { }, TestService.class);
        assertFalse(getContext().registrations(TestService2.class).isEmpty());
    }
}

