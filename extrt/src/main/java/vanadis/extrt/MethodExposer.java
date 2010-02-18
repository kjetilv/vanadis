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
import vanadis.core.reflection.Invoker;
import vanadis.ext.Expose;
import vanadis.jmx.ManagedDynamicMBeans;
import vanadis.osgi.ServiceProperties;

import java.lang.reflect.Method;

final class MethodExposer<T> extends Exposer<T> {

    private final Method method;

    private final boolean runtimeProperties;

    MethodExposer(FeatureAnchor<T> featureAnchor, Method method, Expose annotation, ManagedDynamicMBeans mbeans) {
        super(featureAnchor, annotation, mbeans);
        this.runtimeProperties = method != null && method.getParameterTypes().length == 1;
        this.method = method;
    }

    @Override
    protected PropertySet runtimeProperties() {
        if (!runtimeProperties) {
            return null;
        }
        ServiceProperties<T> properties = getProperties();
        return properties == null
                ? PropertySets.create()
                : properties.getPropertySet().copy(true);
    }

    @Override
    protected Object resolveExposedObject(PropertySet runtimePropertySet) {
        Object managed = getObjectManager().getManagedObject();
        return method == null ? managed
                : runtimePropertySet == null ? Invoker.invoke(this, managed, this.method)
                        : Invoker.invoke(this, managed, this.method, runtimePropertySet);
    }
}
