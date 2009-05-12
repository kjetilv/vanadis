/*
 * Copyright 2008 Kjetil Valstadsve
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
package vanadis.osgi;

import java.util.Collection;

/**
 * <P>A Mediator wraps a ServiceTracker.  It adds genericity,
 * and is conveniently {@link Iterable} over its current tracked
 * set.  Created with {@link vanadis.osgi.Context#createMediator(Class, MediatorListener)},
 * passing a {@link vanadis.osgi.MediatorListener}.</P>
 *
 * @see vanadis.osgi.MediatorListener
 */
public interface Mediator<T> extends Iterable<T> {

    T getService();

    Reference<T> getReference();

    Collection<T> getServices();

    Collection<Reference<T>> getReferences();

    boolean isEmpty();

    void close();
}
