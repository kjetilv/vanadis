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
import net.sf.vanadis.lang.piji.BadArgumentException;
import net.sf.vanadis.lang.piji.Context;
import net.sf.vanadis.lang.piji.Expression;
import net.sf.vanadis.lang.piji.hold.DataHolderFactory;

@EntryPoint("Reflection")
public final class ArrayLengthFunction extends AbstractFunction {

    @EntryPoint("Reflection")
    public ArrayLengthFunction(Context ctx) {
        super("<array>", 1, ctx);
    }

    @Override
    public Object apply(Context context, Expression[] args)
            throws Throwable {
        checkArgumentCount(args);
        Object array = DataHolderFactory.drop(args[1].evaluate(context));
        if (array instanceof Object[]) {
            return DataHolderFactory.holder(((Object[]) array).length);
        }
        if (array instanceof int[]) {
            return DataHolderFactory.holder(((int[]) array).length);
        }
        if (array instanceof char[]) {
            return DataHolderFactory.holder(((char[]) array).length);
        }
        if (array instanceof long[]) {
            return DataHolderFactory.holder(((long[]) array).length);
        }
        if (array instanceof double[]) {
            return DataHolderFactory.holder(((double[]) array).length);
        }
        if (array instanceof float[]) {
            return DataHolderFactory.holder(((float[]) array).length);
        }
        if (array instanceof short[]) {
            return DataHolderFactory.holder(((short[]) array).length);
        }
        if (array instanceof byte[]) {
            return DataHolderFactory.holder(((byte[]) array).length);
        }
        if (array instanceof boolean[]) {
            return DataHolderFactory.holder(((boolean[]) array).length);
        }
        throw new BadArgumentException
                (this + " got non-array first argument " + array);
    }

}


