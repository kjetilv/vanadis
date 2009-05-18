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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import org.junit.Test;
import vanadis.blueprints.ModuleSpecification;
import vanadis.ext.ObjectManager;
import vanadis.osgi.impl.BareBonesContext;

import java.io.InputStream;
import java.util.Collection;

public class ModulesProcessorTest {

    @Test
    public void detectSingleManagedType() {
        assertSimpleCarrier(SimpleMOCarrier.class);
    }

    @Test
    public void detectSingleResource() {
        assertSimpleCarrierResource(SimpleMOCarrier.class);
    }

    @Test
    public void detectAutoLaunches() {
        assertAutoLaunch(AutoLaunchMOsCarrier.class);
    }

    @Test
    public void detectSingleAutoLaunches() {
        assertAutoLaunch(AutoLaunchMOCarrier.class);
    }

    private static void assertAutoLaunch(Class<?> type) {
        ObjectManagerFactory factory = singleFactory(type);
        assertEquals("manidged", factory.getType());
        Collection<ObjectManager> objectManagers = factory.autoLaunch();
        assertEquals(2, objectManagers.size());
//        assertTrue(factory.isAutoLaunch());

//        Collection<ModuleSpecification> serviceSpecifications = factory.getAutoLaunches();
//        assertNotNull(serviceSpecifications);
//        assertEquals(2, serviceSpecifications.size());
    }

    private static void assertSimpleCarrier(Class<?> type) {
        ObjectManagerFactory factory = singleFactory(type);
        ObjectManager objectManager = factory.launch(ModuleSpecification.create(factory.getType(), "manidged"));
        assertEquals(type, objectManager.getManagedObject().getClass());
    }

    private static void assertSimpleCarrierResource(Class<?> type) {
        ObjectManagerFactory factory = singleFactory(type);
        assertEquals(type.getPackage().getName(), factory.getType());
    }

    public static ObjectManagerFactory singleFactory(Class<?> carrierClass) {
        ObjectManagerFactory factory = ModulesProcessor.objectManagerFactory
                (new BareBonesContext(),
                 ModulesProcessorTest.class.getClassLoader(),
                 carrierClass.getName(),
                 stream(carrierClass),
                 null,
                 null);
        assertNotNull(factory);
        return factory;
    }

    private static InputStream stream(Class<?> type) {
        return type.getClassLoader().getResourceAsStream(type.getName().replace('.', '/') + ".class");
    }
}
