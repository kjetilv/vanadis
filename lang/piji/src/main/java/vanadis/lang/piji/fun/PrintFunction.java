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

@EntryPoint("Reflection")
public final class PrintFunction extends AbstractFunction {

    @EntryPoint("Reflection")
    public PrintFunction(Context context) {
        super("Print values", true, 1, context);
    }

    @Override
    public Object apply(Context context, Expression[] args)
            throws Throwable {
        checkArgumentCount(args);
        for (int i = 1; i < args.length; i++) {
            System.out.print(args[i].evaluate(context));
        }
        return null;
    }

}
