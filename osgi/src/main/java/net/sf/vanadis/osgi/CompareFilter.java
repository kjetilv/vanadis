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

final class CompareFilter extends AbstractFilter {

    private static final long serialVersionUID = -2482379452163899248L;

    private final CompareOperator op;

    private final String attribute;

    private final Object[] values;

    private final boolean multi;

    CompareFilter(CompareOperator op, String attribute, Object[] values) {
        this.op = Not.nil(op, "op");
        this.attribute = Not.nil(attribute, "attribute");
        this.values = Not.emptyVarArgs(values, "values");
        this.multi = values.length > 1;
    }

    @Override
    protected StringBuilder writeBody(StringBuilder builder) {
        builder.append(attribute).append(op.repr());
        if (multi) {
            builder.append("[");
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    builder.append(",");
                }
                builder.append(values[i]);
            }
            return builder.append("]");
        } else {
            return builder.append(values[0]);
        }
    }

    @Override
    protected Object[] hashBase() {
        Object[] hc = new Object[values.length + 2];
        hc[0] = op;
        hc[1] = attribute;
        System.arraycopy(values, 0, hc, 2, values.length);
        return hc;
    }

    @Override
    protected boolean eq(AbstractFilter expr) {
        CompareFilter fce = (CompareFilter) expr;
        return EqHc.eq(op, fce.op, attribute, fce.attribute) && EqHc.eq(values, fce.values);
    }

    @Override
    public Filter not() {
        if (op == CompareOperator.EQUAL &&
                values.length == 1 &&
                values[0] instanceof Boolean) {
            Object[] inverse = {!(Boolean) values[0]};
            return new CompareFilter
                    (CompareOperator.EQUAL, attribute, inverse);
        } else {
            return super.not();
        }
    }

    @Override
    public boolean matches(ServiceProperties<?> serviceProperties) {
        Object propertyValue = serviceProperties.getPropertySet().get(attribute);
        if (propertyValue == null) {
            return false;
        }
        if (Comparable.class.isInstance(propertyValue)) {
            for (Object value : values) {
                if (value instanceof Comparable<?>) {
                    if (!op.compares(propertyValue, value)) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            throw new IllegalStateException
                    (this + " could not compare non-comparable value " + propertyValue);
        }
    }
}
