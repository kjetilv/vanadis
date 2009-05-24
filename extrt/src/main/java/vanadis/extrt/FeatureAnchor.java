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

import vanadis.blueprints.ModuleSpecificationFeature;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;
import vanadis.core.properties.PropertySet;
import vanadis.core.properties.PropertySets;
import vanadis.objectmanagers.ObjectManager;
import vanadis.osgi.Context;
import vanadis.util.concurrent.OperationQueuer;

class FeatureAnchor<T> {

    static <T> FeatureAnchor<T> create(ObjectManager objectManager, String featureName, Class<T> serviceInterface,
                                       Context context, OperationQueuer queuer) {
        return new FeatureAnchor<T>(featureName, serviceInterface, objectManager, context, queuer);
    }

    static <T> FeatureAnchor<T> create(ObjectManager objectManager, String featureName, Class<T> serviceInterface,
                                       boolean required, Context context, OperationQueuer queuer) {
        return new FeatureAnchor<T>(featureName, serviceInterface, objectManager, required, context, queuer);
    }

    private final String featureName;

    private final Class<T> serviceInterface;

    private final ObjectManager objectManager;

    private final boolean required;

    private final ModuleSpecificationFeature specificationFeature;

    private final Context context;

    private final OperationQueuer queuer;

    FeatureAnchor(String featureName, Class<T> serviceInterface, ObjectManager objectManager,
                  Context context, OperationQueuer queuer) {
        this(featureName, serviceInterface, objectManager, true, context, queuer);
    }

    FeatureAnchor(String featureName, Class<T> serviceInterface, ObjectManager objectManager,
                  boolean required,
                  Context context, OperationQueuer queuer) {
        this.featureName = Not.nil(featureName, "feature name");
        this.specificationFeature = objectManager.getModuleSpecification().getFeature(this.featureName);
        this.serviceInterface = Not.nil(serviceInterface, "service interface");
        this.objectManager = Not.nil(objectManager, "object manager");
        this.required = required;
        this.context = Not.nil(context, "context");
        this.queuer = queuer;
    }

    ModuleSpecificationFeature getModuleSpecificationFeature() {
        return specificationFeature;
    }

    FeatureAnchor<T> asRequired(boolean required) {
        return new FeatureAnchor<T>(featureName, serviceInterface, objectManager, required, context, queuer);
    }

    String getFeatureName() {
        return featureName;
    }

    public PropertySet getPropertySet() {
        return specificationFeature == null ? PropertySets.EMPTY
                : specificationFeature.getPropertySet();
    }

    Class<T> getServiceInterface() {
        return serviceInterface;
    }

    ObjectManager getObjectManager() {
        return objectManager;
    }

    boolean isRequired() {
        return required;
    }

    Context getContext() {
        return context;
    }

    OperationQueuer getQueuer() {
        return queuer;
    }

    @Override
    public String toString() {
        return ToString.of(this, objectManager.getName(),
                           "feature", featureName,
                           "type", serviceInterface.getName(),
                           "context", context);
    }
}
