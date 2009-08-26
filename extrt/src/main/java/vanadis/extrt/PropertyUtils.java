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
import vanadis.core.reflection.Retyper;
import vanadis.ext.Property;

class PropertyUtils {

    static void transferProperties(Property[] properties, PropertySet propertySet) {
        for (Property property : properties) {
            setProperty(propertySet, property);
        }
    }

    static PropertySet read(Property[] properties) {
        if (properties == null || properties.length == 0) {
            return null;
        }
        PropertySet propertySet = PropertySets.create();
        transferProperties(properties, propertySet);
        return propertySet;
    }

    private static void setProperty(PropertySet propertySet, Property property) {
        Object value = Retyper.coerce(property.propertyType(), property.value());
        propertySet.set(property.name(), value);
    }
}
