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
package vanadis.integrationtests;

import static junit.framework.Assert.assertNotNull;
import org.junit.Ignore;
import org.junit.Test;
import vanadis.blueprints.BundleSpecification;
import vanadis.blueprints.ModuleSpecification;
import vanadis.common.time.TimeSpan;
import vanadis.objectmanagers.ManagedState;
import vanadis.objectmanagers.ObjectManager;
import vanadis.osgi.Reference;
import vanadis.osgi.Registration;

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

        Registration<BundleSpecification> addBundle = registerBundle("vanadis.modules.examples.javacalc", "add");
        Registration<BundleSpecification> subBundle = registerBundle("vanadis.modules.examples.javacalc", "sub");
        Registration<BundleSpecification> mulBundle = registerBundle("vanadis.modules.examples.javacalc", "mul");
        Registration<BundleSpecification> divBundle = registerBundle("vanadis.modules.examples.javacalc", "div");
        Registration<?>[] bundles = new Registration<?>[]{addBundle, subBundle, mulBundle, divBundle};

        registerBundle("vanadis.modules.examples.javacalc", "calcservices");
        registerBundle("vanadis.modules.examples.javacalc", "calculator");

        waitForActiveBundle("vanadis.modules.examples.javacalc.add");
        waitForActiveBundle("vanadis.modules.examples.javacalc.sub");
        waitForActiveBundle("vanadis.modules.examples.javacalc.mul");
        waitForActiveBundle("vanadis.modules.examples.javacalc.div");
        waitForActiveBundle("vanadis.modules.examples.javacalc.calcservices");
        waitForActiveBundle("vanadis.modules.examples.javacalc.calculator");

        waitForObjectManagerFactory("javacalc-calculator");
        waitForObjectManagerFactory("javacalc-add");
        waitForObjectManagerFactory("javacalc-sub");
        waitForObjectManagerFactory("javacalc-mul");
        waitForObjectManagerFactory("javacalc-div");

        waitForObjectManager("javacalc-mul");
        assertObjectManagerState("javacalc-mul", ManagedState.ACTIVE);

        waitForObjectManager("javacalc-div");
        assertObjectManagerState("javacalc-div", ManagedState.ACTIVE);

        registerLaunch("javacalc-calculator");
        waitForObjectManager("javacalc-calculator");
        assertObjectManagerState("javacalc-calculator", ManagedState.RESOLVING_DEPENDENCIES);

        Registration<ModuleSpecification> addService = registerLaunch("javacalc-add");
        waitForObjectManager("javacalc-add");
        assertObjectManagerState("javacalc-add", ManagedState.ACTIVE);

        Registration<ModuleSpecification> subService = registerLaunch("javacalc-sub");
        waitForObjectManager("javacalc-sub");
        assertObjectManagerState("javacalc-sub", ManagedState.ACTIVE);

        Registration<?>[] services = new Registration<?>[]{addService, subService, mulService, divService};

        assertObjectManagerState("javacalc-calculator", ManagedState.ACTIVE);

        Reference<ObjectManager> reference = waitForObjectManager("javacalc-calculator");
        Object service = reference.getRawService();
        assertNotNull(service);

        session().waitForAllActive(timeout, TimeSpan.SECOND);

        if (reset) {
            for (int i = 0; i < types.length; i++) {
                services[i].unregister();
                waitForLostObjectManager(session(), "javacalc-" + types[i]);
                assertObjectManagerState("javacalc-calculator", ManagedState.RESOLVING_DEPENDENCIES);
                bundles[i].unregister();
                waitForLostObjectManager(session(), "javacalc-" + types[i]);
                waitForLostBundle(session(), "vanadis.modules.examples.javacalc." + types[i], timeout);

                services[i] = registerLaunch("javacalc-" + types[i]);
                registerBundle("vanadis.modules.examples.javacalc", types[i]);
                waitForActiveBundle("vanadis.modules.examples.javacalc." + types[i]);
                waitForObjectManagerFactory("javacalc-" + types[i]);
                waitForObjectManager("javacalc-" + types[i]);
                assertObjectManagerState("javacalc-calculator", ManagedState.ACTIVE);
            }
        }
        session().close();
    }
}
