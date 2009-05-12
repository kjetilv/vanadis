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

/**
 * <P>Abstract adapter class for context listeners.</P>
 *
 * All subclasses will use {@link Object#equals(Object) object equality}.
 */
public abstract class AbstractContextListener<T> implements ContextListener<T> {

    /**
     * Do nothing.
     *
     * @param reference Reference
     */
    @Override
    public void serviceRegistered(Reference<T> reference) {
    }

    /**
     * Do nothing.
     *
     * @param reference Reference
     */
    @Override
    public void serviceModified(Reference<T> reference) {
    }

    /**
     * Do nothing.
     *
     * @param reference Reference
     */
    @Override
    public void serviceUnregistering(Reference<T> reference) {
    }

    @Override
    /**
     * Invokes {@link Object#equals(Object) super equals}.
     */
    public final boolean equals(Object o) {
        return super.equals(o);
    }

    /**
     * Invokes {@link Object#hashCode() super hashCode}.
     */
    @Override
    public final int hashCode() {
        return super.hashCode();
    }

}
