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

import vanadis.core.reflection.Invoker;
import vanadis.core.lang.ToString;
import vanadis.ext.Track;
import vanadis.osgi.Mediator;
import vanadis.osgi.MediatorListener;
import vanadis.osgi.Reference;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

final class TrackingInjector<T> extends Injector<T> {

    private Mediator<T> mediator;

    private final Collection<Object> collection;

    TrackingInjector(FeatureAnchor<T> featureAnchor,
                     AccessibleObject trackObject, Track annotation,
                     InjectionListener injectionListener) {
        super(featureAnchor.asRequired(annotation.required()),
              annotation.properties(),
              annotation.remotable(),
              annotation.minimum(),
              true,
              annotation.retained(),
              false,
              annotation.trackReferences(),
              injectionListener);
        this.collection = collection(featureAnchor.getObjectManager().getManagedObject(), trackObject);
    }

    @SuppressWarnings({"unchecked"})
    private Collection<Object> collection(Object managed, AccessibleObject trackObject) {
        return (Collection<Object>) (trackObject instanceof Method
                ? Invoker.invoke(this, managed, (Method) trackObject)
                : Invoker.get(this, managed, (Field) trackObject, true));
    }

    @Override
    protected void doActivate() {
        mediator = getContext().createMediator(getServiceInterface(), filter(), new TrackingMediatorListener());
    }

    @Override
    protected void performInject(Reference<T> reference, T service) {
        collection.add(isPassReference() ? reference : service);
    }

    @Override
    protected void performUninject(Reference<T> reference, T service) {
        collection.remove(isPassReference() ? reference : service);
    }

    @Override
    protected void doDeactivate() {
        if (mediator != null) {
            mediator.close();
        }
    }

    @Override
    public String toString() {
        return ToString.of(this, "services", collection == null ? 0 : collection.size());
    }

    private class TrackingMediatorListener implements MediatorListener<T> {

        @Override
        public void added(Reference<T> reference, T service) {
            inject(reference, service, false);
        }

        @Override
        public void removed(Reference<T> reference, T service) {
            uninject(reference, service);
        }
    }
}
