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
package net.sf.vanadis.osgi;

/**
 * <P>A {@link Mediator} can be
 * {@link Context#createMediator(Class, MediatorListener) created}
 * with a MediatorListener.  This listener will be notified with
 * the added and removed references, and the service it referenced.</P>
 *
 * <P>The listener may use the service freely as long as it is no removed.</P>
 *
 * <P>The listener is also free to use the reference in valid ways
 * (matching gets and ungets), though it is intended mainly for
 * book-keeping purposes on the listener's part.</P>
 *
 * <P>This listener interface does not reflect the semantics of the
 * ServiceTrackerListener, which is able to veto instances progammatically.
 * Filters must match.</P>
 */
public interface MediatorListener<T> {

    void added(Reference<T> reference, T service);

    void removed(Reference<T> reference, T service);
}