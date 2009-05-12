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
import vanadis.lang.piji.Expression;
import vanadis.lang.piji.Symbol;
import vanadis.lang.piji.hold.DataHolderFactory;
import vanadis.lang.piji.hold.PrimitiveNumberHolder;

@EntryPoint("Reflection")
public class IncfFunction extends AbstractFunction {

    @EntryPoint("Reflection")
    public IncfFunction(Context ctx) {
        super("<symbol> [value]", true, 1, ctx);
    }

    @Override
    public Object apply(Context context, Expression[] args)
            throws Throwable {
        checkArgumentCount(args);
        Symbol symbol = checkSymbol(args[1]);

        PrimitiveNumberHolder value =
                (PrimitiveNumberHolder) args[1].evaluate(context);

        PrimitiveNumberHolder inc = args.length > 2
                ? (PrimitiveNumberHolder) args[2].evaluate(context)
                : DataHolderFactory.holder(1);

        PrimitiveNumberHolder newValue = value.add(inc);
        context.set(symbol, newValue);

        return newValue;
    }

}
