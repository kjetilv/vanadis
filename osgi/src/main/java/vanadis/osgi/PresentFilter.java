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

import net.sf.vanadis.core.lang.EqHc;
import net.sf.vanadis.core.lang.Not;

final class PresentFilter extends AbstractFilter {

    private static final long serialVersionUID = 5352409167530667787L;

    private static final String PRESENT = "=*";

    private final String attribute;

    PresentFilter(String attribute) {
        this.attribute = Not.nil(attribute, "attribute");
    }

    @Override
    protected StringBuilder writeBody(StringBuilder builder) {
        return builder.append(attribute).append(PRESENT);
    }

    @Override
    protected Object[] hashBase() {
        return new Object[]{attribute};
    }

    @Override
    protected boolean eq(AbstractFilter expr) {
        return EqHc.eq(((PresentFilter) expr).attribute, attribute);
    }

    @Override
    public boolean matches(ServiceProperties<?> serviceProperties) {
        return serviceProperties.getPropertySet().has(attribute);
    }
}
