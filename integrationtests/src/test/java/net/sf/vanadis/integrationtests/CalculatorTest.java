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
package net.sf.vanadis.integrationtests;

import static junit.framework.Assert.assertNotNull;
import net.sf.vanadis.blueprints.BundleSpecification;
import net.sf.vanadis.blueprints.ModuleSpecification;
import net.sf.vanadis.core.time.TimeSpan;
import static net.sf.vanadis.ext.ManagedState.ACTIVE;
import static net.sf.vanadis.ext.ManagedState.RESOLVING_DEPENDENCIES;
import net.sf.vanadis.ext.ObjectManager;
import net.sf.vanadis.osgi.Reference;
import net.sf.vanadis.osgi.Registration;
import org.junit.Ignore;
import org.junit.Test;

public class CalculatorTest extends SingleFelixTestCase {

    @Test(timeout = 60000L)
    public void calculator() {
        calculator(TimeSpan.HALF_MINUTE);
    }

    @Test(timeout = 60000L)
    public void calculatorReset() {
        calculator(TimeSpan.HALF_MINUTE, true);
    }

    @Test
    @Ignore
    public void debuggableCalculatorReset() {
        calculator(TimeSpan.hours(24), true);
    }

    @Test
    @Ignore
    public void debuggableCalculator() {
        calculator(TimeSpan.hours(24));
    }

    private void calculator(TimeSpan timeout) {
        calculator(timeout, false);
    }

    private void calculator(TimeSpan timeout, boolean reset) {
        Registration<ModuleSpecification> mulService = registerLaunch("javacalc-mul");
        Registration<ModuleSpecification> divService = registerLaunch("javacalc-div");

        String[] types = new String[]{"add", "sub", "mul", "div"};

        Registration<BundleSpecification> addBundle = registerBundle("net.sf.vanadis.modules.examples.javacalc", "add");
        Registration<BundleSpecification> subBundle = registerBundle("net.sf.vanadis.modules.examples.javacalc", "sub");
        Registration<BundleSpecification> mulBundle = registerBundle("net.sf.vanadis.modules.examples.javacalc", "mul");
        Registration<BundleSpecification> divBundle = registerBundle("net.sf.vanadis.modules.examples.javacalc", "div");
        Registration<?>[] bundles = new Registration<?>[]{addBundle, subBundle, mulBundle, divBundle};

        registerBundle("net.sf.vanadis.modules.examples.javacalc", "calcservices");
        registerBundle("net.sf.vanadis.modules.examples.javacalc", "calculator");

        waitForActiveBundle("net.sf.vanadis.modules.examples.javacalc.add");
        waitForActiveBundle("net.sf.vanadis.modules.examples.javacalc.sub");
        waitForActiveBundle("net.sf.vanadis.modules.examples.javacalc.mul");
        waitForActiveBundle("net.sf.vanadis.modules.examples.javacalc.div");
        waitForActiveBundle("net.sf.vanadis.modules.examples.javacalc.calcservices");
        waitForActiveBundle("net.sf.vanadis.modules.examples.javacalc.calculator");

        waitForObjectManagerFactory("javacalc-calculator");
        waitForObjectManagerFactory("javacalc-add");
        waitForObjectManagerFactory("javacalc-sub");
        waitForObjectManagerFactory("javacalc-mul");
        waitForObjectManagerFactory("javacalc-div");

        waitForObjectManager("javacalc-mul");
        assertObjectManagerState("javacalc-mul", ACTIVE);

        waitForObjectManager("javacalc-div");
        assertObjectManagerState("javacalc-div", ACTIVE);

        registerLaunch("javacalc-calculator");
        waitForObjectManager("javacalc-calculator");
        assertObjectManagerState("javacalc-calculator", RESOLVING_DEPENDENCIES);

        Registration<ModuleSpecification> addService = registerLaunch("javacalc-add");
        waitForObjectManager("javacalc-add");
        assertObjectManagerState("javacalc-add", ACTIVE);

        Registration<ModuleSpecification> subService = registerLaunch("javacalc-sub");
        waitForObjectManager("javacalc-sub");
        assertObjectManagerState("javacalc-sub", ACTIVE);

        Registration<?>[] services = new Registration<?>[]{addService, subService, mulService, divService};

        assertObjectManagerState("javacalc-calculator", ACTIVE);

        Reference<ObjectManager> reference = waitForObjectManager("javacalc-calculator");
        Object service = reference.getRawService();
        assertNotNull(service);

        session().waitForAllActive(timeout, TimeSpan.SECOND);

        if (reset) {
            for (int i = 0; i < types.length; i++) {
                services[i].unregister();
                waitForLostObjectManager(session(), "javacalc-" + types[i]);
                assertObjectManagerState("javacalc-calculator", RESOLVING_DEPENDENCIES);
                bundles[i].unregister();
                waitForLostObjectManager(session(), "javacalc-" + types[i]);
                waitForLostBundle(session(), "net.sf.vanadis.modules.examples.javacalc." + types[i], timeout);

                services[i] = registerLaunch("javacalc-" + types[i]);
                registerBundle("net.sf.vanadis.modules.examples.javacalc", types[i]);
                waitForActiveBundle("net.sf.vanadis.modules.examples.javacalc." + types[i]);
                waitForObjectManagerFactory("javacalc-" + types[i]);
                waitForObjectManager("javacalc-" + types[i]);
                assertObjectManagerState("javacalc-calculator", ACTIVE);
            }
        }
        session().close();
    }
}
