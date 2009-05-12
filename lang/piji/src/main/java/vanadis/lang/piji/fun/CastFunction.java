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
import vanadis.lang.piji.hold.DataHolderFactory;
import vanadis.lang.piji.hold.PrimitiveNumberHolder;

@EntryPoint("Reflection")
public final class CastFunction extends AbstractFunction {

    @EntryPoint("Reflection")
    public CastFunction(Context ctx) {
        super("<public class denominator> <object>", 2, ctx);
    }

    @Override
    public Object apply(Context context, Expression[] args)
            throws Throwable {
        checkArgumentCount(args);
        Class<?> type = Reflector.getClass(args[1], context);
        if (type == null) {
            throw new EvaluationException
                    (this + " could not find type from " + args[2]);
        }
        Object object = args[2].evaluate(context);
        if (type.isPrimitive() && object instanceof PrimitiveNumberHolder) {
            PrimitiveNumberHolder holder = (PrimitiveNumberHolder) object;
            if (type == Integer.TYPE) {
                return DataHolderFactory.holder(holder.getInt());
            }
            if (type == Long.TYPE) {
                return DataHolderFactory.holder(holder.getLong());
            }
            if (type == Float.TYPE) {
                return DataHolderFactory.holder(holder.getFloat());
            }
            if (type == Double.TYPE) {
                return DataHolderFactory.holder(holder.getDouble());
            }
            if (type == Byte.TYPE) {
                return DataHolderFactory.holder(holder.getByte());
            }
            if (type == Short.TYPE) {
                return DataHolderFactory.holder(holder.getShort());
            }
            throw new InternalRuntimeException
                    (this + " got unsupported primitive type: " + type);
        }
        return DataHolderFactory.holder(DataHolderFactory.drop(object), type);
    }

}

