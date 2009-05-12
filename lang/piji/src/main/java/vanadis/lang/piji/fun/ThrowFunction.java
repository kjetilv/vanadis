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

package vanadis.lang.piji.fun;

import vanadis.core.lang.EntryPoint;
import vanadis.lang.piji.AbstractFunction;
import vanadis.lang.piji.Context;
import vanadis.lang.piji.EvaluationException;
import vanadis.lang.piji.Expression;

@EntryPoint("Reflection")
public final class ThrowFunction extends AbstractFunction {

    @EntryPoint("Reflection")
    public ThrowFunction(Context ctx) {
        super("<throwable>", 1, ctx);
    }

    @Override
    public Object apply(Context context, Expression[] args)
            throws Throwable {
        checkArgumentCount(args);
        Object object = args[1].evaluate(context);
        throw (object instanceof Throwable)
                ? (Throwable) object
                : new EvaluationException(this + " got non-throwable " + object);
    }

}

