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
import net.sf.vanadis.lang.piji.Expression;
import net.sf.vanadis.lang.piji.Reflector;
import net.sf.vanadis.lang.piji.hold.DataHolderFactory;
import net.sf.vanadis.lang.piji.hold.Holder;

@EntryPoint("Reflection")
public final class InstanceofFunction extends AbstractFunction {

    @EntryPoint("Reflection")
    public InstanceofFunction(Context ctx) {
        super("<object> <public class denominator>*", true, 2, ctx);
    }

    @Override
    public Object apply(Context context, Expression[] args)
            throws Throwable {
        checkArgumentCount(args);
        Object result = args[1].evaluate(context);
        if (result == null) {
            return DataHolderFactory.holder(false);
        }
        Class<?> resultClass = result instanceof Holder
                ? ((Holder) result).getType()
                : result.getClass();
        for (int i = 2; i < args.length; i++) {
            Class<?> type = Reflector.getClass(args[i], context);
            if (!type.isAssignableFrom(resultClass)) {
                return DataHolderFactory.holder(false);
            }
        }
        return DataHolderFactory.holder(true);
    }

}


