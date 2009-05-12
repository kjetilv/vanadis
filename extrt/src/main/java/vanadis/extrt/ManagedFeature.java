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

import vanadis.core.properties.PropertySet;
import vanadis.core.properties.PropertySets;
import vanadis.ext.ObjectManager;
import vanadis.osgi.Context;
import vanadis.util.concurrent.OperationQueuer;

import java.util.concurrent.atomic.AtomicBoolean;

abstract class ManagedFeature<T, M> {

    private final FeatureAnchor<T> featureAnchor;

    private final Class<M> mbeanInterface;

    private final AtomicBoolean teardownMode = new AtomicBoolean();

    private final JmxRegistration<?> registration;

    ManagedFeature(FeatureAnchor<T> featureAnchor, Class<M> mbeanInterface) {
        this.featureAnchor = featureAnchor;
        this.mbeanInterface = mbeanInterface;
        this.registration = registration();
    }

    final String getFeatureName() {
        return getFeatureData().getFeatureName();
    }

    final PropertySet getFeaturePropertySet() {
        return getFeatureData().getPropertySet();
    }

    final Class<T> getServiceInterface() {
        return getFeatureData().getServiceInterface();
    }

    final boolean isRequired() {
        return getFeatureData().isRequired();
    }

    final String getObjectManagerName() {
        return getFeatureData().getObjectManager().getName();
    }

    final void setupMode() {
        teardownMode.set(false);
    }

    final void teardownMode() {
        teardownMode.set(true);
    }

    protected abstract M mbean();

    abstract boolean isComplete();

    abstract boolean isMulti();

    abstract void activate();

    abstract long[] getServiceIds();

    final void deactivate() {
        try {
            performDeactivate();
        } finally {
            registration.unregister();
        }
    }

    protected ObjectManager getObjectManager() {
        return getFeatureData().getObjectManager();
    }

    protected abstract void performDeactivate();

    protected final boolean isTeardownMode() {
        return teardownMode.get();
    }

    protected final boolean isActive() {
        return !isTeardownMode();
    }

    protected final Context getContext() {
        return getFeatureData().getContext();
    }

    protected final OperationQueuer getQueuer() {
        return getFeatureData().getQueuer();
    }

    private JmxRegistration<?> registration() {
        return JmxRegistration.create
                (getFeatureData().getContext(), mbeanInterface,
                 mbean(), getObjectManager().getManagedClass().getName(),
                 PropertySets.create("type", getObjectManager().getType(),
                                     "name", getObjectManager().getName(),
                                     "feature", getFeatureName()));
    }

    protected FeatureAnchor<T> getFeatureData() {
        return featureAnchor;
    }
}
