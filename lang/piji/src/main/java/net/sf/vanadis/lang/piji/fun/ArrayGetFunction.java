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
import net.sf.vanadis.lang.piji.*;
import net.sf.vanadis.lang.piji.hold.DataHolderFactory;
import net.sf.vanadis.lang.piji.hold.PrimitiveIntegerHolder;
import net.sf.vanadis.lang.piji.hold.PrimitiveNumberHolder;

import java.lang.reflect.Array;

@EntryPoint("Reflection")
public final class ArrayGetFunction extends AbstractFunction {

    @EntryPoint("Reflection")
    public ArrayGetFunction(Context ctx) {
        super("<array> <index>", 2, ctx);
    }

    @Override
    public Object apply(Context context, Expression[] args)
            throws Throwable {
        checkArgumentCount(args);
        Object object = args[2].evaluate(context);
        if (!(object instanceof PrimitiveIntegerHolder)) {
            throw new BadArgumentException
                    (this + " got non-integer index: " + object);
        }

        int index = ((PrimitiveNumberHolder) object).getInt();
        Object array = args[1].evaluate(context);
        if (!array.getClass().isArray()) {
            throw new EvaluationException
                    (this + " got non-array: " + array);
        }

        if (array instanceof int[]) {
            return DataHolderFactory.holder(Array.getInt(array, index));
        } else if (array instanceof Object[]) {
            return Array.get(array, index);
        } else if (array instanceof char[]) {
            return DataHolderFactory.holder(Array.getChar(array, index));
        } else if (array instanceof long[]) {
            return DataHolderFactory.holder(Array.getLong(array, index));
        } else if (array instanceof float[]) {
            return DataHolderFactory.holder(Array.getFloat(array, index));
        } else if (array instanceof double[]) {
            return DataHolderFactory.holder(Array.getDouble(array, index));
        } else if (array instanceof byte[]) {
            return DataHolderFactory.holder(Array.getByte(array, index));
        } else if (array instanceof short[]) {
            return DataHolderFactory.holder(Array.getShort(array, index));
        } else if (array instanceof boolean[]) {
            return DataHolderFactory.holder(Array.getBoolean(array,
                                                             index));
        } else {
            throw new BadArgumentException
                    (this + " got unknown array type " + array +
                            ", index: " + index);
        }
    }

}


