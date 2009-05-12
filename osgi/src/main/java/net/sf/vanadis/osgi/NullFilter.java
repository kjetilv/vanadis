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

import java.io.ObjectStreamException;

final class NullFilter extends AbstractFilter {

    private static final long serialVersionUID = -2033765423441855268L;

    private static final Object[] HC = new Object[]{new Object()};

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    protected StringBuilder writeBody(StringBuilder builder) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object[] hashBase() {
        return HC;
    }

    @Override
    protected boolean eq(AbstractFilter expr) {
        return expr == Filters.NULL;
    }

    @Override
    public boolean matches(ServiceProperties<?> properties) {
        return true;
    }

    private Object readResolve()
        throws ObjectStreamException {
        return Filters.NULL;
    }
}
