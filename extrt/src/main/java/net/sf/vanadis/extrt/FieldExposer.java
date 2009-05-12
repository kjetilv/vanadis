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

package net.sf.vanadis.extrt;

import net.sf.vanadis.core.properties.PropertySet;
import net.sf.vanadis.core.reflection.Invoker;
import net.sf.vanadis.ext.Expose;

import java.lang.reflect.Field;

final class FieldExposer<T> extends Exposer<T> {

    private final Field field;

    FieldExposer(FeatureAnchor<T> featureAnchor, Field field, Expose annotation) {
        super(featureAnchor, annotation);
        this.field = field;
    }

    @Override
    protected Object resolveExposedObject(PropertySet runtimePropertySet) {
        Object managed = getObjectManager().getManagedObject();
        return field == null ? managed : Invoker.getByForce(this, managed, field);
    }
}