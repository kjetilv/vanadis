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

import vanadis.core.properties.PropertySet;
import vanadis.core.properties.PropertySets;

import java.io.Serializable;

abstract class AbstractSpecification implements Serializable {

    private final PropertySet propertySet;

    private final boolean globalProperties;

    AbstractSpecification(PropertySet propertySet, Boolean globalProperties) {
        this.propertySet = propertySet == null || propertySet.isEmpty() ? PropertySets.EMPTY
                : propertySet;
        this.globalProperties = !this.propertySet.isEmpty() && globalProperties != null && globalProperties;
    }

    public final boolean isGlobalProperties() {
        return globalProperties;
    }

    public final PropertySet getPropertySet() {
        return propertySet;
    }
}
