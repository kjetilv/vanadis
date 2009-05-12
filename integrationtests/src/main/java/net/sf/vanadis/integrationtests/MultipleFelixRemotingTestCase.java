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

import junit.framework.Assert;
import net.sf.vanadis.core.io.Location;
import net.sf.vanadis.core.properties.PropertySet;
import net.sf.vanadis.core.properties.PropertySets;
import net.sf.vanadis.core.reflection.Invoker;
import net.sf.vanadis.core.time.TimeSpan;
import static net.sf.vanadis.core.time.TimeSpan.*;
import static net.sf.vanadis.ext.ManagedState.ACTIVE;
import static net.sf.vanadis.ext.ManagedState.RESOLVING_DEPENDENCIES;
import net.sf.vanadis.osgi.Reference;

import java.lang.reflect.Method;

public class MultipleFelixRemotingTestCase extends FelixTestCase {

    private static final PropertySet ROUTING = PropertySets.create("routing", true);

    protected static final TimeSpan SHORT_TIMEOUT = MINUTE;

    protected static final TimeSpan LONG_TIMEOUT = hours(24);

    private static final String CALCULATOR = "javacalc-calculator";

    protected void rDC(TimeSpan timeout, boolean startRouterFirst, boolean failover) {
        FelixTestSession[] allTestSessions = new FelixTestSession[]{newFelixSession(timeout, true),
                                                                    newFelixSession(timeout, true),
                                                                    newFelixSession(timeout, true),
                                                                    newFelixSession(timeout, true),
                                                                    newFelixSession(timeout, true)};
        FelixTestSession master = allTestSessions[0];
        FelixTestSession[] slaves = new FelixTestSession[]{allTestSessions[1],
                                                           allTestSessions[2],
                                                           allTestSessions[3],
                                                           allTestSessions[4]};
        String[] types = new String[]{"add", "sub", "mul", "div"};

        Location masterLocation = master.startRemoting(1);
        for (FelixTestSession slave1 : slaves) {
            slave1.startRemoting(1);
        }

        for (FelixTestSession testSession : allTestSessions) {
            testSession.waitForAllActive(HALF_MINUTE, SECOND);
        }

        if (startRouterFirst) {
            master.startNetworking(ROUTING);
        }

        for (FelixTestSession slave : slaves) {
            slave.startNetworking(routedTo(masterLocation));
        }

        if (!startRouterFirst) {
            HALF_MINUTE.sleep();
            master.startNetworking(ROUTING);
        }

        for (FelixTestSession felix : allTestSessions) {
            felix.waitForObjectManager("networker");
        }

        master.registerVBundle("net.sf.vanadis.modules.examples.javacalc", "calcservices");
        master.registerVBundle("net.sf.vanadis.modules.examples.javacalc", "calculator");

        for (int i = 0; i < slaves.length; i++) {
            slaves[i].registerVBundle("net.sf.vanadis.modules.examples.javacalc", "calcservices");
            slaves[i].registerVBundle("net.sf.vanadis.modules.examples.javacalc", types[i]);
        }

        master.registerLaunch(CALCULATOR);
        master.waitForObjectManager(CALCULATOR);
        master.waitForObjectManagerState(CALCULATOR, RESOLVING_DEPENDENCIES);

        for (int i = 0; i < slaves.length; i++) {
            String type = types[i];
            FelixTestSession slave = slaves[i];
            startService(type, slave);
        }

        master.waitForObjectManagerState(CALCULATOR, ACTIVE); // Dependencies resolved

        SECOND.sleep();

        runCalcs(master);
        if (failover) {
            for (int i = 0; i < slaves.length; i++) {
                restart(timeout, master, masterLocation, slaves, i, types[i]);
                runCalcs(master);
            }
        }
    }

    private static void startService(String type, FelixTestSession slave) {
        String serviceType = arithmeticService(type);
        slave.registerLaunch(serviceType);
        slave.waitForObjectManager(serviceType);
        slave.waitForObjectManagerState(serviceType, ACTIVE);
    }

    private void restart(TimeSpan timeout,
                         FelixTestSession master, Location masterLocation,
                         FelixTestSession[] slaves, int i,
                         String type) {
        freeFelixSession(slaves[i]);
        slaves[i] = newFelixSession(timeout, true);
        FelixTestSession freshSlave = slaves[i];
        freshSlave.startRemoting(1);
        freshSlave.startNetworking(routedTo(masterLocation));
        freshSlave.registerVBundle("net.sf.vanadis.modules.examples.javacalc", "calcservices");
        freshSlave.registerVBundle("net.sf.vanadis.modules.examples.javacalc", type);
        startService(type, freshSlave);
        master.waitForObjectManagerState(CALCULATOR, ACTIVE);
    }

    private static String arithmeticService(String type) {
        return "javacalc-" + type;
    }

    private void runCalcs(FelixTestSession master) {
        runCalcs(master, master);
    }

    private static PropertySet routedTo(Location location) {
        return PropertySets.create
                ("routing", false).set
                ("routerLocations", location.toLocationString());
    }

    @SuppressWarnings({"UnusedDeclaration"})
    // failover parameter
    protected void distCalc(TimeSpan timeout, boolean failover) {
        FelixTestSession felix1 = newFelixSession(timeout, true);
        FelixTestSession felix2 = newFelixSession(timeout, true);

        Location location1 = felix1.startRemoting(1);
        felix2.startRemoting(1);

        felix1.waitForAllActive(HALF_MINUTE, SECOND);
        felix2.waitForAllActive(HALF_MINUTE, SECOND);

        felix1.startNetworking(ROUTING);
        felix2.startNetworking(routedTo(location1));

        felix1.waitForObjectManager("networker");
        felix2.waitForObjectManager("networker");

        felix1.registerLaunch("javacalc-add");
        felix1.registerLaunch("javacalc-sub");

        felix1.registerVBundle("net.sf.vanadis.modules.examples.javacalc", "add");
        felix1.registerVBundle("net.sf.vanadis.modules.examples.javacalc", "sub");
        felix1.registerVBundle("net.sf.vanadis.modules.examples.javacalc", "mul");
        felix1.registerVBundle("net.sf.vanadis.modules.examples.javacalc", "div");
        felix1.registerVBundle("net.sf.vanadis.modules.examples.javacalc", "calcservices");

        felix2.registerVBundle("net.sf.vanadis.modules.examples.javacalc", "calcservices");
        felix2.registerVBundle("net.sf.vanadis.modules.examples.javacalc", "calculator");

        felix2.registerLaunch(CALCULATOR);
        felix2.waitForObjectManager(CALCULATOR);
        felix2.waitForObjectManagerState(CALCULATOR, RESOLVING_DEPENDENCIES);

        felix1.waitForObjectManager("javacalc-add");
        felix1.waitForObjectManagerState("javacalc-add", ACTIVE);
        felix2.waitForObjectManagerState(CALCULATOR, RESOLVING_DEPENDENCIES);

        felix1.waitForObjectManager("javacalc-sub");
        felix1.waitForObjectManagerState("javacalc-sub", ACTIVE);
        felix2.waitForObjectManagerState(CALCULATOR, RESOLVING_DEPENDENCIES);

        felix1.registerLaunch("javacalc-mul");
        felix1.waitForObjectManager("javacalc-mul");
        felix1.waitForObjectManagerState("javacalc-mul", ACTIVE);
        felix2.waitForObjectManagerState(CALCULATOR, RESOLVING_DEPENDENCIES);

        felix1.registerLaunch("javacalc-div");
        felix1.waitForObjectManager("javacalc-div");
        felix1.waitForObjectManagerState("javacalc-div", ACTIVE);

        felix2.waitForObjectManagerState(CALCULATOR, ACTIVE); // Dependencies resolved

        SECOND.sleep();

        runCalcs(felix1, felix2);
    }

    private void runCalcs(FelixTestSession felix1, FelixTestSession felix2) {
        calc(felix2, "(add 2 2)", 4);
        calc(felix2, "(sub 4 2)", 2);
        calc(felix2, "(mul 4 2)", 8);
        calc(felix2, "(div 4 2)", 2);

        op(felix1, "add", "Adder", 2, 2, 4);
        op(felix2, "add", "Adder", 2, 2, 4);

        op(felix1, "sub", "Subtractor", 2, 2, 0);
        op(felix2, "sub", "Subtractor", 2, 1, 1);
    }

    private void op(FelixTestSession felix, String op, String service, int arg1, int arg2, int answer) {
        Reference<?> reference = felix.getContext().getReference
                ("net.sf.vanadis.modules.examples.javacalc.calcservices." + service);
        Object oper = reference.getRawService();
        try {
            Method method = oper.getClass().getMethod(op, int[].class);
            //noinspection PrimitiveArrayArgumentToVariableArgMethod
            Object result =
                    Invoker.invoke(this, oper, method, new int[]{arg1, arg2});
            Assert.assertEquals(answer, result);
        } catch (NoSuchMethodException e) {
            FelixTestSession.fail(felix, e.getMessage(), e);
        } finally {
            reference.unget();
        }
    }

    private void calc(FelixTestSession felix, String expr, int answer) {
        Reference<?> reference = felix.getContext().getSingleReference
                ("net.sf.vanadis.modules.examples.javacalc.calcservices.PocketCalculator", null);
        Object calc = reference.getRawService();
        try {
            Method method = calc.getClass().getMethod("calculate", String.class);
            Object result = Invoker.invoke(this, calc, method, expr);
            Assert.assertEquals(answer, result);
        } catch (NoSuchMethodException e) {
            e.printStackTrace(System.out);
            FelixTestSession.fail(felix, e.getMessage(), e);
        } finally {
            reference.unget();
        }
    }
}
