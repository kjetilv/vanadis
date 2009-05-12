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

import net.sf.vanadis.core.reflection.Invoker;
import net.sf.vanadis.ext.Inject;
import net.sf.vanadis.osgi.Reference;

import java.lang.reflect.Field;

final class FieldInjector<T> extends AccessibleInjector<T> {

    private final Field field;

    FieldInjector(FeatureAnchor<T> featureAnchor,
                  Field field,
                  Inject annotation,
                  InjectionListener listener) {
        super(featureAnchor.asRequired(annotation.required()),
              annotation,
              false,
              field.getType().equals(Reference.class),
              listener);
        this.field = field;
    }

    @Override
    protected void performInject(Reference<T> reference, T service) {
        Invoker.assign(this, getManaged(), field, isPassReference() ? reference : service);
    }

    @Override
    protected void performUninject(Reference<T> reference, T service) {
        Invoker.assign(this, getManaged(), field, null);
    }
}
