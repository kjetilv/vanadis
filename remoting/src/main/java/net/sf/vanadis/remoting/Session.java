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
package net.sf.vanadis.remoting;

import net.sf.vanadis.core.lang.EqHc;
import net.sf.vanadis.core.lang.ToString;

public final class Session {

    private final String id;

    public Session(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    @Override
    public String toString() {
        return ToString.of(this, id);
    }

    @Override
    public int hashCode() {
        return EqHc.hc(id);
    }

    @Override
    public boolean equals(Object object) {
        Session session = EqHc.retyped(this, object);
        return session == this || session != null && EqHc.eq(id, session.id);
    }

}
