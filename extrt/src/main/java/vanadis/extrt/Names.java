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

import vanadis.annopro.AnnotationDatum;
import vanadis.core.properties.PropertySet;
import vanadis.core.reflection.GetNSet;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

class Names {

    private static final String NAME_PROPERTY = "name";

    static String nameOfType(AnnotationDatum<Class<?>> annotationData, String name) {
        PropertySet propertySet = annotationData.getPropertySet();
        return propertySet.has(NAME_PROPERTY) ? propertySet.getString(NAME_PROPERTY) : name;
    }

    static String nameOfMethod(AnnotationDatum<Method> annotationData) {
        PropertySet propertySet = annotationData.getPropertySet();
        return propertySet.has(NAME_PROPERTY) ? propertySet.getString(NAME_PROPERTY)
                : GetNSet.resolveByPrefix(annotationData.getElement().getName(), "get", "add", "set");
    }

    static String nameOfField(AnnotationDatum<Field> annotationData) {
        PropertySet propertySet = annotationData.getPropertySet();
        return propertySet.has(NAME_PROPERTY) ? propertySet.getString(NAME_PROPERTY)
                : annotationData.getElement().getName();
    }
}
