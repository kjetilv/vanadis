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

import vanadis.core.lang.ToString;
import vanadis.ext.ManagedLifecycle;
import vanadis.objectmanagers.ManagedState;
import vanadis.objectmanagers.ObjectManager;
import vanadis.util.log.Log;
import vanadis.util.log.Logs;

class ObjectManagerState {

    private ManagedState managedState = ManagedState.NEWBORN;

    private final ManagedLifecycle lifecycle;

    private final ObjectManagerObserver observer;

    private final ObjectManager objectManager;

    private final ObjectManagerFailureTracker failureTracker;

    ObjectManagerState(ObjectManager objectManager,
                       ObjectManagerObserver observer,
                       ObjectManagerFailureTracker failureTracker) {
        this.observer = observer;
        this.objectManager = objectManager;
        this.failureTracker = failureTracker;
        this.lifecycle = objectManager.getManagedObject() instanceof ManagedLifecycle
                ? (ManagedLifecycle) objectManager.getManagedObject()
                : null;
    }


    boolean is(ManagedState... states) {
        for (ManagedState state : states) {
            if (this.managedState == state) {
                return true;
            }
        }
        return false;
    }

    boolean afterOrIn(ManagedState... states) {
        for (ManagedState state : states) {
            if (managedState.afterOrIn(state)) {
                return true;
            }
        }
        return false;
    }

    ManagedState getManagedState() {
        return managedState;
    }

    void fail() {

    }

    void transition(Transition transition) {
        if (managedState == transition.getTargetState()) {
            return;
        }
        try {
            if (lifecycle != null) {
                transition.perform(lifecycle);
            }
            stateTransition(transition.getTargetState());
        } catch (Throwable e) {
            updateState(e, "{0} failed transition {1}", this, transition);
        }
    }

    private void stateTransition(ManagedState targetObjectState) {
        updateState(targetObjectState);
    }

    private void updateState(ManagedState targetState, Object... msg) {
        updateState(targetState, null, msg);
    }

    void updateState(Throwable cause, Object... msg) {
        updateState(ManagedState.FAILED, cause, msg);
    }

    private void updateState(ManagedState targetState, Throwable cause, Object... msg) {
        managedState = targetState;
        try {
            String message = failureTracker.fail(cause, msg);
            if (targetState == ManagedState.FAILED) {
                log.error(message == null ? this + " failed" : message, cause);
            } else if (message != null) {
                log.info(message);
            }
        } finally {
            log.info(this + " completed transition to " + targetState);
            if (observer != null && objectManager != null) {
                observer.updated(objectManager);
            }
        }
    }

    private static final Log log = Logs.get(ObjectManagerState.class);

    @Override
    public String toString() {
        return ToString.of(this, objectManager.getName(), "state", managedState);
    }
}
