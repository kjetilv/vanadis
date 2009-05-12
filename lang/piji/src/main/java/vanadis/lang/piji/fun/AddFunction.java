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
import vanadis.lang.piji.hold.DataHolderFactory;
import vanadis.lang.piji.hold.PrimitiveNumberHolder;

@EntryPoint("Reflection")
public final class AddFunction extends AbstractFunction {

    @EntryPoint("Reflection")
    public AddFunction(Context context) {
        super("Arithmetic and string addition", true, 1, context);
    }

    @Override
    public Object apply(Context context, Expression[] args)
            throws Throwable {
        checkArgumentCount(args);
        int varSize = args.length - 1;
        Object[] argValues = new Object[varSize];
        boolean numbers = true;
        for (int i = 0; i < varSize; i++) {
            Object val = args[i + 1].evaluate(context);
            numbers = numbers && val instanceof PrimitiveNumberHolder;
            argValues[i] = val;
        }
        if (numbers) {
            PrimitiveNumberHolder value = DataHolderFactory.holder(0);
            for (int i = 0; i < varSize; i++) {
                value = value.add((PrimitiveNumberHolder) argValues[i]);
            }
            return value;
        } else {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < varSize; i++) {
                buffer.append(argValues[i]);
            }
            return buffer.toString();
        }
    }

}
