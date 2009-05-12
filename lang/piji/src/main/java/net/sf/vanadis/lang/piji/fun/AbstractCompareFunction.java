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

package net.sf.vanadis.lang.piji.fun;

import net.sf.vanadis.core.lang.EntryPoint;
import net.sf.vanadis.lang.piji.AbstractFunction;
import net.sf.vanadis.lang.piji.Context;
import net.sf.vanadis.lang.piji.EvaluationException;
import net.sf.vanadis.lang.piji.Expression;
import net.sf.vanadis.lang.piji.hold.DataHolderFactory;

/**
 * Abstract superclass for compare operations.  Only needs the subclass to
 * know whether a compare of two values failed or not.  A "fail" means
 * the compare operation should return false.
 *
 * @author Kjetil Valstadsve
 */
abstract class AbstractCompareFunction extends AbstractFunction {

    @EntryPoint("Reflection")
    AbstractCompareFunction(String docString, Context ctx) {
        super(docString, true, 2, ctx);
    }

    boolean requiresComparable() {
        return true;
    }

    private Object nextArg(Context context, Expression arg)
            throws Throwable {
        Object evaluated = arg.evaluate(context);
        if (requiresComparable() && !(evaluated instanceof Comparable)) {
            throw new EvaluationException
                    (this + " got non-Comparable argument " + evaluated);
        }
        return evaluated;
    }

    protected abstract boolean compareFailed(Object last, Object value);

    @Override
    public Object apply(Context context, Expression[] args)
            throws Throwable {
        checkArgumentCount(args);
        Object last = nextArg(context, args[1]);
        int varSize = args.length;
        for (int i = 2; i < varSize; i++) {
            Object value = nextArg(context, args[i]);
            if (compareFailed(last, value)) {
                return DataHolderFactory.holder(false);
            } else {
                last = value;
            }
        }
        return DataHolderFactory.holder(true);
    }

    @SuppressWarnings({"unchecked", "RawUseOfParameterizedType"})
    // Stoopid generics...
    static int cmp(Object t1, Object t2) {
        if (t1 instanceof Comparable<?>) {
            if (t2 instanceof Comparable<?>) {
                Comparable c1 = (Comparable) t1;
                Comparable c2 = (Comparable) t2;
                return c1.compareTo(c2);
            }
            throw new IllegalArgumentException
                    (t1 + " not Comparable: " + t1.getClass());
        }
        throw new IllegalArgumentException
                (t1 + " not Comparable: " + t1.getClass());
    }
}
