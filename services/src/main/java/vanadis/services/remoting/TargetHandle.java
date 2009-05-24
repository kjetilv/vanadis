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
package vanadis.services.remoting;

import vanadis.core.io.Location;
import vanadis.core.lang.EqHc;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;

import java.io.Serializable;

public final class TargetHandle<T> implements Serializable {

    private static final long serialVersionUID = 6505476894921455484L;

    private final TargetReference<T, ?> reference;

    private final Location location;

    public static <T> TargetHandle<T> create(Location location, TargetReference<T, ?> reference) {
        return new TargetHandle<T>(location, reference);
    }

    public TargetHandle(Location location, TargetReference<T, ?> reference) {
        this.reference = Not.nil(reference, "reference");
        this.location = Not.nil(location, "location");
    }

    public TargetReference<T, ?> getReference() {
        return reference;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object object) {
        TargetHandle<T> handle = EqHc.retyped(this, object);
        return handle == this || handle != null &&
                EqHc.eq(handle.reference, reference,
                        handle.location, location);
    }

    @Override
    public int hashCode() {
        return EqHc.hc(reference, location);
    }

    @Override
    public String toString() {
        return ToString.of(this, new StringBuilder(reference.toString()).append("@").append(location));
    }
}
