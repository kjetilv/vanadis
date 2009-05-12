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

import vanadis.core.lang.EqHc;

abstract class AbstractFilter implements Filter {

    private static final long serialVersionUID = -6167511130706528182L;

    private Integer cachedHashCode;

    final StringBuilder write(StringBuilder builder) {
        if (isNull()) {
            return builder;
        }
        builder.append("(");
        writeBody(builder);
        builder.append(")");
        return builder;
    }

    protected abstract StringBuilder writeBody(StringBuilder builder);

    @Override
    public String toFilterString() {
        return isNull() ? null : write(new StringBuilder()).toString();
    }

    @Override
    public Filter and(Filter... exprs) {
        return compose(CompositeOp.AND, FilterUtils.nonNulls(exprs));
    }

    @Override
    public Filter or(Filter... exprs) {
        return compose(CompositeOp.OR, FilterUtils.nonNulls(exprs));
    }

    @Override
    public Filter and(String attribute) {
        return and(Filters.isTrue(attribute));
    }

    @Override
    public Filter or(String attribute) {
        return this.or(Filters.isTrue(attribute));
    }

    @Override
    public Filter andNot(String attribute) {
        return and(Filters.isFalse(attribute));
    }

    @Override
    public Filter orNot(String attribute) {
        return or(Filters.isFalse(attribute));
    }

    @Override
    public Filter not() {
        return isNull() ? this
                : new CompositeFilter(CompositeOp.NOT, new Filter[]{this});
    }

    @Override
    public final int hashCode() {
        if (cachedHashCode == null) {
            cachedHashCode = EqHc.hc(hashBase());
        }
        return cachedHashCode;
    }

    @Override
    public final boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object == null) {
            return false;
        }
        AbstractFilter expr = EqHc.retyped(this, object);
        return expr != null && eq(expr);
    }

    protected abstract Object[] hashBase();

    protected abstract boolean eq(AbstractFilter expr);

    @Override
    public boolean isNull() {
        return false;
    }

    Filter compose(CompositeOp op, Filter[] filters) {
        return filters.length == 0 ? this : newFilter(op, filters);
    }

    Filter newFilter(CompositeOp op, Filter[] filters) {
        Filter[] combined = combine(filters);
        return combined.length == 1 ? combined[0] : new CompositeFilter(op, combined);
    }

    private Filter[] combine(Filter[] nonNullArray) {
        if (isNull()) {
            return nonNullArray;
        }
        Filter[] combined = new Filter[1 + nonNullArray.length];
        combined[0] = this;
        System.arraycopy(nonNullArray, 0, combined, 1, nonNullArray.length);
        return combined;
    }

    @Override
    public boolean isTyped() {
        return false;
    }

    @Override
    public final String toString() {
        return toFilterString();
    }
}
