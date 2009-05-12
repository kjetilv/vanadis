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

package net.sf.vanadis.lang.piji.fun;

import net.sf.vanadis.lang.piji.AbstractFunction;
import net.sf.vanadis.lang.piji.Context;
import net.sf.vanadis.lang.piji.EvaluationException;
import net.sf.vanadis.lang.piji.Expression;
import net.sf.vanadis.lang.piji.hold.PrimitiveBooleanHolder;

abstract class AbstractLoopFunction extends AbstractFunction {

    protected AbstractLoopFunction(String docString,
                                int argCount,
                                Context ctx) {
        super(docString, true, argCount, ctx);
    }

    final boolean test(Context context,
                       Expression expr)
            throws Throwable {
        Object test = expr.evaluate(context);
        if (test instanceof PrimitiveBooleanHolder) {
            return ((PrimitiveBooleanHolder) test).getBoolean();
        } else {
            throw new EvaluationException
                    (this + " got non-boolean test " + test);
        }
    }

    static Object bodyValue(Context context,
                            Expression[] args,
                            int offset)
            throws Throwable {
        Object value = null;
        for (int i = offset; i < args.length; i++) {
            value = args[i].evaluate(context);
        }
        return value;
    }
}
