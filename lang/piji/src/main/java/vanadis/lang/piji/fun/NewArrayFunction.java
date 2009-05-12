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
import vanadis.lang.piji.hold.PrimitiveIntegerHolder;

import java.lang.reflect.Array;

@EntryPoint("Reflection")
public final class NewArrayFunction extends AbstractFunction {

    @EntryPoint("Reflection")
    public NewArrayFunction(Context ctx) {
        super("<public class denominator> length*", true, 2, ctx);
    }

    @Override
    public Object apply(Context context, Expression[] args)
            throws Throwable {
        checkArgumentCount(args);
        int[] sizes = new int[args.length - 2];
        for (int i = 0; i < sizes.length; i++) {
            Object object = args[2].evaluate(context);
            if (object instanceof PrimitiveIntegerHolder) {
                int length = ((PrimitiveIntegerHolder) object).getInt();
                if (length < 0) {
                    throw new EvaluationException
                            (this + " got array size " + length);
                }
                sizes[i] = length;
            }
        }
        Object type = Reflector.getClass(args[1], context);
        if (type instanceof Class) {
            return Array.newInstance((Class<?>) type, sizes);
        } else {
            throw new EvaluationException
                    (this + " got non-Class argument " + type);
        }
    }

}

