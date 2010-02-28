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
package vanadis.blueprints;

import vanadis.core.collections.Generic;
import vanadis.core.lang.EqHc;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;
import vanadis.core.properties.CaseString;
import vanadis.core.properties.PropertySet;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public final class ModuleSpecification extends AbstractSpecification {

    private static final AtomicLong counter = new AtomicLong();

    public static ModuleSpecification create(String type) {
        return new ModuleSpecification(type, type, null, null, null, null);
    }

    public static ModuleSpecification create(String type, String name) {
        return new ModuleSpecification(type, name, null, null, null, null);
    }

    public static ModuleSpecification create(String type, String name, PropertySet propertySet) {
        return new ModuleSpecification(type, name, propertySet, null, null, null);
    }

    public static ModuleSpecification create(String type, String name,
                                             PropertySet propertySet, Boolean globalProperties) {
        return new ModuleSpecification(type, name, propertySet, globalProperties, null, null);
    }

    public static ModuleSpecification create(String type, String name, PropertySet propertySet,
                                             Iterable<ModuleSpecificationFeature> features) {
        return new ModuleSpecification(type, name, propertySet, null, null, features);
    }

    public static ModuleSpecification create(String type, String name,
                                             PropertySet propertySet, Boolean globalProperties,
                                             Iterable<ModuleSpecificationFeature> features) {
        return create(type, name, propertySet, globalProperties, null, features);
    }

    public static ModuleSpecification create(String type, String name,
                                             PropertySet propertySet,
                                             Boolean globalProperties, String configPropertiesPid,
                                             Iterable<ModuleSpecificationFeature> features) {
        return new ModuleSpecification(type, name, propertySet, globalProperties, configPropertiesPid, features);
    }

    public static ModuleSpecification createFrom(URI uri) {
        return BlueprintsReader.readModuleSpecification(uri);
    }

    public static ModuleSpecification createDefault(Object managed) {
        return doCreateDefault(Not.nil(managed, "managed"), managed.getClass());
    }

    public static ModuleSpecification createDefault(Class<?> managedType) {
        return doCreateDefault(null, Not.nil(managedType, "managed type"));
    }

    public static ModuleSpecification createDefault(Object managed, Class<?> managedType) {
        return doCreateDefault(managed, managedType == null ? Not.nil(managed, "managed").getClass() : managedType);
    }

    private static ModuleSpecification doCreateDefault(Object managed, Class<?> managedType) {
        String typeName = managedType.getName();
        String instanceName = managedType.getSimpleName() + "@" +
                (managed == null ? "T" + counter.incrementAndGet() : System.identityHashCode(managed));
        return new ModuleSpecification(typeName, instanceName, null, null, null, null);
    }

    private final String name;

    private final String type;

    private final Map<CaseString, ModuleSpecificationFeature> serviceFeatures;

    private ModuleSpecification(String type, String name, PropertySet propertySet,
                                Boolean globalProperties,
                                String configPropertiesPid, Iterable<ModuleSpecificationFeature> features) {
        super(propertySet, globalProperties, configPropertiesPid);
        this.type = Not.nilOrEmpty(type, "type");
        this.name = Not.nilOrEmpty(name, "name");
        this.serviceFeatures = features == null ? NO_FEATURES : indexFeatures(features);
    }

    public ModuleSpecificationFeature getFeature(String name) {
        return serviceFeatures.get(new CaseString(name));
    }

    public boolean hasFeature(ModuleSpecificationFeature feature) {
        ModuleSpecificationFeature existing = serviceFeatures.get(name(feature));
        return existing != null && existing.equals(feature);
    }

    private static CaseString name(ModuleSpecificationFeature feature) {
        return new CaseString(feature.getName());
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return getName() + ":" + getType();
    }

    public PropertySet getFeatureProperties(String name) {
        ModuleSpecificationFeature feature = serviceFeatures.get(new CaseString(name));
        return feature == null ? null
                : feature.getPropertySet();
    }

    private static final long serialVersionUID = -5076166235323989101L;

    private static final Map<CaseString, ModuleSpecificationFeature> NO_FEATURES = Collections.emptyMap();

    private static Map<CaseString, ModuleSpecificationFeature> indexFeatures(Iterable<ModuleSpecificationFeature> features) {
        Map<CaseString, ModuleSpecificationFeature> map = Generic.map();
        for (ModuleSpecificationFeature feature : features) {
            ModuleSpecificationFeature duplicate = map.put(name(feature), feature);
            if (duplicate != null) {
                throw new IllegalArgumentException("Duplicate feature '" + feature.getName() +
                        "': " + duplicate + " and " + feature);
            }
        }
        return map;
    }

    @Override
    public int hashCode() {
        return EqHc.hc(type, name);
    }

    @Override
    public boolean equals(Object obj) {
        ModuleSpecification specification = EqHc.retyped(this, obj);
        return specification != null &&
                EqHc.eq(name, specification.name,
                        type, specification.type);
    }

    @Override
    public String toString() {
        return ToString.of(this, name, "type", type);
    }
}
