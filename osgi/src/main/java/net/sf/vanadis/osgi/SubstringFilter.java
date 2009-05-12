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
import net.sf.vanadis.core.properties.PropertySet;

final class SubstringFilter extends AbstractFilter {

    private static final long serialVersionUID = 1932954024217439912L;

    private static final String PRESENT = "=";

    private final String attribute;

    private final String substring;

    SubstringFilter(String attribute, String substring) {
        this.attribute = Not.nil(attribute, "attribute");
        this.substring = Not.nil(substring, "substring");
    }

    @Override
    protected StringBuilder writeBody(StringBuilder builder) {
        return builder.append(attribute).append(PRESENT).append(substring);
    }

    @Override
    protected Object[] hashBase() {
        return new Object[]{attribute, substring};
    }

    @Override
    protected boolean eq(AbstractFilter expr) {
        SubstringFilter se = (SubstringFilter) expr;
        return EqHc.eq(se.attribute, attribute,
                       se.substring, substring);
    }

    @Override
    public boolean matches(ServiceProperties<?> serviceProperties) {
        PropertySet propertySet = serviceProperties.getPropertySet();
        return propertySet.has(attribute) &&
                propertySet.getString(attribute).contains(substring);
    }

}