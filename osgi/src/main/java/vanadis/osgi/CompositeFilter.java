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
package vanadis.osgi;

import vanadis.core.lang.EqHc;

final class CompositeFilter extends AbstractFilter {

    private static final long serialVersionUID = -1806077997439757072L;

    private final CompositeOp op;

    private final Filter[] filters;

    @SuppressWarnings({"AssignmentToCollectionOrArrayFieldFromParameter"})
        // All Filter arrays are constructed internally, and/or by varargs
    CompositeFilter(CompositeOp op, Filter[] filters) {
        this.op = op;
        this.filters = filters;
    }

    @Override
    public Filter not() {
        return op == CompositeOp.NOT ? filters[0] : super.not();
    }

    @Override
    protected StringBuilder writeBody(StringBuilder builder) {
        builder.append(op.repr());
        for (Filter expr : filters) {
            ((AbstractFilter) expr).write(builder);
        }
        return builder;
    }

    @Override
    protected Object[] hashBase() {
        Object[] hc = new Object[filters.length + 1];
        hc[0] = op;
        System.arraycopy(filters, 0, hc, 1, filters.length);
        return hc;
    }

    @Override
    protected boolean eq(AbstractFilter expr) {
        CompositeFilter fce = (CompositeFilter) expr;
        return EqHc.eq(fce.op, op) && EqHc.eqA(fce.filters, filters);
    }

    @Override
    protected Filter newFilter(CompositeOp op, Filter[] filters) {
        if (op == this.op) {
            Filter[] sum = new Filter[this.filters.length + filters.length];
            System.arraycopy(this.filters, 0, sum, 0, this.filters.length);
            System.arraycopy(filters, 0, sum, this.filters.length, filters.length);
            return new CompositeFilter(op, sum);
        } else {
            return super.newFilter(op, filters);
        }
    }

    @Override
    public boolean matches(ServiceProperties<?> properties) {
        return op.match(new MatchesIterable(filters, properties));
    }

    @Override
    public boolean isTyped() {
        return op.match(new TypedFilterIterable(filters));
    }
}
