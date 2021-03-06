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
import vanadis.lang.piji.*;
import vanadis.lang.piji.hold.Place;

import java.util.Arrays;

@EntryPoint("Reflection")
public final class SetFunction extends AbstractFunction {

    @EntryPoint("Reflection")
    public SetFunction(Context ctx) {
        super("[<symbol> <value>]+", true, 2, ctx);
    }

    @Override
    public Object apply(Context context, Expression[] args)
            throws Throwable {
        checkArgumentCount(args);
        if (args.length % 2 != 1) {
            throw new EvaluationException
                    (this + " needs even number of arguments, got " +
                            Arrays.toString(args));
        }
        Object value = null;
        for (int i = 1; i < args.length; i = i + 2) {
            value = args[i + 1].evaluate(context);
            Symbol symbol = checkSymbol(args[i], false);
            if (symbol == null) {
                Object place = args[i].evaluate(context);
                if (place instanceof Place) {
                    ((Place) place).setValue(value);
                } else {
                    throw new EvaluationException
                            (this + " got non-symbol " + args[i] +
                                    " and non-place " + place +
                                    " to set value " + value);
                }
            } else {
                context.set(symbol, value);
            }
        }
        return value;
    }

}
