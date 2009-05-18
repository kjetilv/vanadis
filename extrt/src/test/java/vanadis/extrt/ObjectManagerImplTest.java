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
import static junit.framework.Assert.*;
import org.junit.Test;
import vanadis.core.collections.Generic;
import vanadis.core.properties.PropertySet;
import vanadis.ext.*;
import vanadis.osgi.*;
import vanadis.osgi.impl.BonyRegistration;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        Assert.assertSame(remotableExposer.getLastExposed(), remotableInjectee.getLastAdded());
    }

    private interface TestService extends Cloneable {

    }

    public static class VanillaExposer {

        @Expose
        public static TestService getTestService() {
            return new TestService() {
            };
        }

    }

    public interface Vanilla {

        @Expose
        TestService getTestService();
    }

    public static class VanillaImplExposer implements Vanilla {

        @Override
        public TestService getTestService() {
            return new TestService() {
            };
        }

    }

    public static class PropertyExposer {

        @Expose(properties = {@Property(name = "navn", value = "ola nordmann")})
        public static TestService getTest() {
            return new TestService() {
            };
        }
    }

    public static class ClassyPropertyExposer {

        @Expose(properties = {@Property(name = "navn", value = "ola nordmann")},
                objectClasses = {Cloneable.class})
        public static TestService getTest() {
            return new TestService() {
            };
        }

        @Expose(properties = {@Property(name = "gift", value = "true", propertyType = Boolean.class)})
        public static TestService getBeautifulService() {
            return new TestService() {
            };
        }
    }

    public static class SpecificExposer {

        @Expose(exposedType = TestService.class)
        public static Cloneable getCloneable() {
            return new TestService() {
            };
        }

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

    public static class RemotableInjectee {

        public Object getLastAdded() {
            return lastAdded;
        }

        private Object lastAdded;

        @SuppressWarnings({"UnusedDeclaration"})
        @Inject(remotable = true)
        public void addX(Object x) {
            lastAdded = x;
        }
    }

    public static class ObjectInjectee implements GetObject<Object> {

        private Object object;

        @Override
        public Object getObject() {
            return object;
        }

        @Inject
        public void setObject(Object object) {
            this.object = object;
        }
    }

    public static class FieldObjectInjectee implements GetObject<Object> {

        @SuppressWarnings({"UnusedDeclaration"})
        @Inject
        private Object object;

        @Override
        public Object getObject() {
            return object;
        }
    }

    public static class RemotableExposer {

        private TestService lastExposed;

        public TestService getLastExposed() {
            return lastExposed;
        }

        @Expose(remotable = true)
        public TestService getTest() {
            lastExposed = new TestService() {
            };
            return lastExposed;
        }
    }

    public static class ReferenceInjectee {

        private Reference<TestService> propertiedTestServiceRef;

        private TestService testService;

        private ServiceProperties<TestService> properties;

        private ServiceProperties<TestService> refProperties;

        public ServiceProperties<TestService> getRefProperties() {
            return refProperties;
        }

        public Reference<TestService> getTestServiceRef() {
            return testServiceRef;
        }

        private Reference<TestService> testServiceRef;

        @Inject(injectType = TestService.class)
        public void setPropertiedTestServiceRef(Reference<TestService> registration) {
            this.testServiceRef = registration;
        }

        @Inject(injectType = TestService.class)
        public void setTestServiceRef(Reference<TestService> registration,
                                      ServiceProperties<TestService> properties) {
            this.propertiedTestServiceRef = registration;
            this.refProperties = properties;
        }

        public Reference<TestService> getPropertiedTestServiceRef() {
            return propertiedTestServiceRef;
        }

        @Inject
        public void setTestService(TestService testService,
                                   ServiceProperties<TestService> properties) {
            this.testService = testService;
            this.properties = properties;
        }

        public TestService getTestService() {
            return testService;
        }

        public ServiceProperties<TestService> getPropertySet() {
            return properties;
        }
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

    public static class RuntimePropertiesExposer {

        @Expose
        public static TestService getTestService(PropertySet propertySet) {
            propertySet.set("foo", "bar");
            return new TestService() {
            };
        }
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

    public static class EasyGoingExposer {

        @Expose(optional = true)
        public static TestService getOptionally() {
            return new TestService() {
            };
        }

        @Expose
        public static TestService getRequiredly() {
            return new TestService() {
            };
        }
    }

    @Test
    public void exposeRequiredAndOptional() {
        manage(new EasyGoingExposer());
        assertEquals(6, getContext().registrationCount()); // Incl. ObjectManager itself, jmx regs...
    }

    public class PropertiedInjector {

        public TestService getHrService() {
            return hrService;
        }

        private TestService hrService;

        @Inject(properties = {
                @Property(name = "foo", value = "1", propertyType = Integer.class),
                @Property(name = "bar", value = "true", propertyType = Boolean.class)})
        public void setFoo(TestService hrService) {
            this.hrService = hrService;
        }
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
        getContext().register(new TestService() {
        },
                              ServiceProperties.create(TestService.class,
                                                       Generic.map("foo", 1)));
        assertNull(injector.getHrService());
        getContext().register(new TestService() {
        },
                              ServiceProperties.create(TestService.class,
                                                       Generic.map("foo", 1,
                                                                   "bar", false)));
        assertNull(injector.getHrService());
        getContext().register(new TestService() {
        },
                              ServiceProperties.create(TestService.class,
                                                       Generic.map("foo", 2,
                                                                   "bar", true)));
        assertNull(injector.getHrService());
        TestService hit = new TestService() {
        };
        getContext().register(hit,
                              ServiceProperties.create(TestService.class,
                                                       Generic.map("foo", 1,
                                                                   "bar", true)));
        assertNotNull(injector.getHrService());
        Assert.assertSame(hit, injector.getHrService());
    }

    public static class ApacheReference {

        @Track(trackedType = TestService.class, trackReferences = true)
        private final Set<Reference<TestService>> testServices = Generic.set();
    }

    public static class Apache {

        private final Set<TestService> testServices = Generic.set();

        @Track(trackedType = TestService.class)
        public Collection<TestService> testServices() {
            return testServices;
        }
    }

    public static class FieldApache {

        @Track(trackedType = TestService.class)
        private final Set<TestService> testServices = Generic.set();
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

    public interface InjectHeritage {

        @Inject
        void setTestService(TestService service);
    }

    public class InheritedInject implements InjectHeritage {
        private TestService service;

        @Override
        public void setTestService(TestService service) {
            this.service = service;
        }

        public TestService getService() {
            return service;
        }
    }

    @Expose(exposedType = TestService.class)
    public class Blotter implements TestService {

    }

    @Test
    public void blotMe() {
        InheritedInject inheritedInject = new InheritedInject();
        manage(inheritedInject);
        assertNull(inheritedInject.getService());
        manage(new Blotter());
        assertNotNull(inheritedInject.getService());
    }
}

