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
package net.sf.vanadis.ext;

/**
 * <P>Token type, whose sole purpose is to be a default value for Class-typed
 * annotation attributes.  Example: {@link Expose#exposedType() exposed type}.  In
 * short, it tells the container to <em>derive</em> the actual type
 * from some well-documented (hear, hear) procedure.  For instance, an exposed
 * field will default to the type of the field.</P>
 */
public final class Derived {

    private Derived() {
        // Don't make me.
    }
}
