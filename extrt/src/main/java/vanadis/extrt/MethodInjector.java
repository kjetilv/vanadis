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

import vanadis.core.lang.ToString;
import static vanadis.core.reflection.Invoker.invoke;
import vanadis.ext.Inject;
import vanadis.ext.ModuleSystemException;
import vanadis.osgi.Reference;
import vanadis.osgi.ServiceProperties;
import vanadis.util.log.Log;
import vanadis.util.log.Logs;

import java.lang.reflect.Method;
import java.util.Map;

final class MethodInjector<T> extends AccessibleInjector<T> {

    private final Method injectPoint;

    private final Method retractPoint;

    private final boolean passProperties;

    MethodInjector(FeatureAnchor<T> featureAnchor,
                   Method injectPoint, Method retractPoint,
                   Inject annotation,
                   InjectionListener listener) {
        super(featureAnchor.asRequired(annotation.required()),
              annotation,
              injectPoint != retractPoint,
              injectPoint.getParameterTypes()[0].equals(Reference.class),
              listener);
        this.injectPoint = injectPoint;
        this.retractPoint = retractPoint;
        this.passProperties = takesProperties(injectPoint);
        verifyState();
    }

    @Override
    protected void performInject(Reference<T> reference, T service) {
        Object object = isPassReference() ? reference : service;
        if (passProperties) {
            invoke(this, getManaged(), injectPoint, object, reference.getServiceProperties());
        } else {
            invoke(this, getManaged(), injectPoint, object);
        }
    }

    @Override
    protected void performUninject(Reference<T> reference, T service) {
        boolean replacementsReady = containsReferences() && isActive();
        if (reuseInjectionPoint() && isReplaceWithoutNull() && replacementsReady) {
            Map.Entry<Reference<T>, T> replacement = setReplacement();
            log.info(this + " uninjected " + service + ", replaced it directly with " + replacement);
        } else {
            T uninjected = reuseInjectionPoint() ? null : service;
            Method method = retractPoint;
            invoke(this, getManaged(), method, uninjected);
            if (isReplaceUnregistered() && replacementsReady) {
                Map.Entry<Reference<T>, T> replacement = setReplacement();
                log.info(this + " uninjected " + service + ", replaced it with " + replacement);
            } else {
                log.info(this + " uninjected " + service + ", no replacement found");
            }
        }
    }

    private void verifyState() {
        if (isMulti()) {
            if (reuseInjectionPoint()) {
                throw new ModuleSystemException
                        (this + " is multi-value, requires dedicated retration point!  " +
                                "Injection point: " + injectPoint);
            }
        } else {
            if (getMinimum() > 1) {
                throw new ModuleSystemException
                        (this + " is single-value, minimum cannot be " + getMinimum());
            }
        }
    }

    private boolean reuseInjectionPoint() {
        return injectPoint == retractPoint;
    }

    private static final Log log = Logs.get(MethodInjector.class);

    private static boolean takesProperties(Method point) {
        Class<?>[] types = point.getParameterTypes();
        return types.length == 2 && types[1].equals(ServiceProperties.class);
    }

    @Override
    public String toString() {
        return ToString.of(this, getFeatureName(), "@", getManaged());
    }
}
