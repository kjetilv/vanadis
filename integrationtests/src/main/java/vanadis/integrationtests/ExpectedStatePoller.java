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
package net.sf.vanadis.integrationtests;

import net.sf.vanadis.core.reflection.Invoker;
import net.sf.vanadis.ext.ManagedState;

import java.util.concurrent.Callable;

class ExpectedStatePoller implements Callable<Object> {

    private final FelixTestSession testSession;

    private final Object objectManager;

    private final ManagedState[] states;

    private int tries;

    ExpectedStatePoller(FelixTestSession testSession, Object manager, ManagedState... states) {
        this.testSession = testSession;
        this.objectManager = manager;
        this.states = states;
    }

    @Override
    public Object call() throws Exception {
        String objectManagerState = stateOf(objectManager);
        for (ManagedState state : states) {
            if (objectManagerState.equalsIgnoreCase(state.toString())) {
                return objectManagerState;
            }
        }
        if (objectManagerState.equals(FAILED)) {
            FelixTestSession.fail(testSession, objectManager + " failed");
        }
        tries++;
        System.out.print(tries % 16 == 0 ? objectManagerState : ".");
        return null;
    }

    static String stateOf(Object target) {
        return Invoker.getByForce(FelixTestSession.class, target, "managedState").toString();
    }

    private static final String FAILED = ManagedState.FAILED.toString();
}
