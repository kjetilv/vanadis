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
import net.sf.vanadis.lang.piji.hold.PrimitiveDataHolder;
import net.sf.vanadis.lang.piji.hold.PrimitiveIntegerHolder;
import net.sf.vanadis.lang.piji.hold.PrimitiveNumberHolder;

import java.lang.reflect.Array;

@EntryPoint("Reflection")
public final class ArraySetFunction extends AbstractFunction {

    @EntryPoint("Reflection")
    public ArraySetFunction(Context ctx) {
        super("<array> <index> <value>", 3, ctx);
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
        int index = ((PrimitiveIntegerHolder) object).getInt();
        Object value = args[3].evaluate(context);
        Object array = args[1].evaluate(context);
        if (!array.getClass().isArray()) {
            throw new EvaluationException
                    (this + " got non-array " + array +
                            ", index:" + index + ", value:" + value);
        }
        if (array instanceof int[]) {
            Array.setInt(array, index,
                         ((PrimitiveNumberHolder) value).getInt());
        } else if (array instanceof Object[]) {
            Array.set(array, index, value);
        } else if (array instanceof char[]) {
            Array.setChar(array, index,
                          ((PrimitiveDataHolder) value).getChar());
        } else if (array instanceof long[]) {
            Array.setLong(array, index,
                          ((PrimitiveNumberHolder) value).getLong());
        } else if (array instanceof float[]) {
            Array.setFloat(array, index,
                           ((PrimitiveNumberHolder) value).getFloat());
        } else if (array instanceof double[]) {
            Array.setDouble(array, index,
                            ((PrimitiveNumberHolder) value).getDouble());
        } else if (array instanceof byte[]) {
            Array.setByte(array, index,
                          ((PrimitiveNumberHolder) value).getByte());
        } else if (array instanceof short[]) {
            Array.setShort(array, index,
                           ((PrimitiveNumberHolder) value).getShort());
        } else if (array instanceof boolean[]) {
            Array.setBoolean(array, index,
                             ((PrimitiveDataHolder) value).getBoolean());
        } else {
            throw new EvaluationException
                    (this + " got unknown array type: " + array +
                            ", index:" + index + ", value:" + value);
        }
        return value;
    }
}


