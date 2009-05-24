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

import vanadis.core.collections.Generic;
import vanadis.core.lang.ToString;
import vanadis.core.lang.VarArgs;
import vanadis.ext.ManagedLifecycle;
import vanadis.objectmanagers.ManagedState;

import java.util.Set;

enum Transition {

    CONFIGURE(ManagedState.CONFIGURED, ManagedState.NEWBORN) {
        @Override
        public void perform(ManagedLifecycle lifecycle) {
            lifecycle.configured();
        }
    },

    INITIALIZE(ManagedState.RESOLVING_DEPENDENCIES, ManagedState.CONFIGURED) {
        @Override
        public void perform(ManagedLifecycle lifecycle) {
            lifecycle.initialized();
        }
    },

    BECOME_RESOLVED(ManagedState.DEPENDENCIES_RESOLVED, ManagedState.RESOLVING_DEPENDENCIES) {
        @Override
        public void perform(ManagedLifecycle lifecycle) {
            lifecycle.dependenciesResolved();
        }
    },

    COMPLETE_EXPOSURE(ManagedState.SERVICES_EXPOSED, ManagedState.DEPENDENCIES_RESOLVED) {
        @Override
        public void perform(ManagedLifecycle lifecycle) {
            lifecycle.servicesExposed();
        }
    },

    ACTIVATE(ManagedState.ACTIVE, ManagedState.SERVICES_EXPOSED) {
        @Override
        public void perform(ManagedLifecycle lifecycle) {
            lifecycle.activate();
        }
    },

    BECOME_UNRESOLVED(ManagedState.RESOLVING_DEPENDENCIES,
                      ManagedState.ACTIVE, ManagedState.DEPENDENCIES_RESOLVED, ManagedState.SERVICES_EXPOSED) {
        @Override
        public void perform(ManagedLifecycle lifecycle) {
            lifecycle.dependenciesLost();
        }
    },

    FAIL(ManagedState.FAILED) {
        @Override
        public void perform(ManagedLifecycle lifecycle) {
            lifecycle.activate();
        }
    },

    DISPOSE(ManagedState.DISPOSED) {
        @Override
        public void perform(ManagedLifecycle lifecycle) {
            lifecycle.closed();
        }
    };

    private final ManagedState targetState;

    private final Set<ManagedState> acceptableFromStates;

    public ManagedState getTargetState() {
        return targetState;
    }

    Transition(ManagedState targetState, ManagedState... fromStates) {
        this.targetState = targetState;
        this.acceptableFromStates = VarArgs.notPresent(fromStates) ? null
                : Generic.set(fromStates);
    }

    public abstract void perform(ManagedLifecycle lifecycle);

    public boolean verifyInitialState(ManagedState state) {
        return acceptableFromStates == null || acceptableFromStates.contains(state);
    }

    @Override
    public String toString() {
        return ToString.of(this, "target", targetState);
    }
}
