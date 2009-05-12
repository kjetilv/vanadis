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

package net.sf.vanadis.blueprints;

import net.sf.vanadis.core.lang.EqHc;
import net.sf.vanadis.core.lang.Not;
import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.core.properties.PropertySet;
import net.sf.vanadis.core.properties.PropertySets;

public final class ModuleSpecificationFeature {

    private final String name;

    private final ModuleSpecificationFeatureType type;

    private final PropertySet propertySet;

    public ModuleSpecificationFeature(String name, ModuleSpecificationFeatureType type) {
        this(name, type, PropertySets.EMPTY);
    }

    public ModuleSpecificationFeature(String name, ModuleSpecificationFeatureType type, PropertySet propertySet) {
        this.name = Not.nil(name, "name").toLowerCase();
        this.type = Not.nil(type, "type");
        this.propertySet = propertySet.copy(false);
    }

    public String getName() {
        return name;
    }

    public ModuleSpecificationFeatureType getType() {
        return type;
    }

    public PropertySet getPropertySet() {
        return propertySet;
    }

    @Override
    public boolean equals(Object obj) {
        ModuleSpecificationFeature feature = EqHc.retyped(this, obj);
        return feature != null && EqHc.eq(name, feature.name,
                                          type, feature.type);
    }

    @Override
    public int hashCode() {
        return EqHc.hc(name, type);
    }

    @Override
    public String toString() {
        return ToString.of(this,
                           "name", name,
                           "type", type.name().toLowerCase(),
                           "propertySet", propertySet);
    }
}
