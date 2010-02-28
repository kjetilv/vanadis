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

import vanadis.ext.Inject;
import vanadis.osgi.AbstractContextListener;
import vanadis.osgi.ContextListener;
import vanadis.osgi.Reference;

import java.util.Map;

abstract class AccessibleInjector<T> extends Injector<T> {

    private final boolean replaceUnregistered;

    private final boolean replaceWithoutNull;

    private final ContextListener<T> contextListener = new InjectorContextListener();

    AccessibleInjector(FeatureAnchor<T> featureAnchor,
                       Inject annotation,
                       boolean multi,
                       boolean passReference,
                       InjectionListener listener) {
        super(featureAnchor,
              annotation.properties(),
              annotation.remotable(),
              annotation.minimum(),
              multi,
              annotation.retained(),
              annotation.updates(),
              passReference,
              listener);
        replaceUnregistered = annotation.retained() && !multi && annotation.replace();
        replaceWithoutNull = replaceUnregistered && annotation.replaceAtOnce();
    }

    @Override
    protected final void doDeactivate() {
        getContext().removeContextListener(contextListener);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    protected final void doActivate() {
        ContextListener<?> listener = getQueuer() == null
                ? this.contextListener
                : getQueuer().createAsynch(this.contextListener, ContextListener.class, true);
        getContext().addContextListener(getServiceInterface(), (ContextListener<T>) listener, filter(), true);
    }

    final boolean isReplaceUnregistered() {
        return replaceUnregistered;
    }

    final boolean isReplaceWithoutNull() {
        return replaceWithoutNull;
    }

    final Map.Entry<Reference<T>, T> setReplacement() {
        Map.Entry<Reference<T>, T> replacement = getReplacement();
        inject(replacement.getKey(), replacement.getValue(), false);
        return replacement;
    }

    private class InjectorContextListener extends AbstractContextListener<T> {

        @Override
        public void serviceRegistered(Reference<T> reference) {
            inject(reference, null, false);
        }

        @Override
        public void serviceUnregistering(Reference<T> reference) {
            uninject(reference, null);
        }
    }
}
