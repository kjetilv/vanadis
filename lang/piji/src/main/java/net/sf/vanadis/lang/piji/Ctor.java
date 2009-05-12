/*
 * Copyright 2008 Kjetil Valstadsve
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

package net.sf.vanadis.lang.piji;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Ctor extends ReflectorHelper {

    public Ctor(Reflector ref) {
        super(ref);
    }

    public final Object construct(Context ctx, Expression[] args)
        throws Throwable {
        return construct(ctx, args, 0);
    }

    public final Object construct(Context ctx, Expression[] args, int offset)
        throws Throwable {
        Class<?> type = Reflector.resolveType(getRef().resolveTarget(ctx, args[offset]));
        return construct(ctx, type, args, offset + 1);
    }

    final Object construct(Context ctx, Class<?> type, Expression[] args, int offset)
        throws Throwable {
        RealAndFormalArrays arrays =
            new RealAndFormalArrays(getRef(), offset, args, ctx);
        Class<?>[] signature = arrays.getSignature();
        Object[] realArguments = arrays.getRealArguments();
        Constructor<?> constructor =
            getRef().getConstructorFinder().getConstructor(type, signature);
        return invokeConstructor(constructor, realArguments);
    }

    private static Object invokeConstructor(Constructor<?> constructor,
                                            Object[] realArguments)
        throws Throwable {
        boolean inaccessible = !constructor.isAccessible();
        if (inaccessible) {
            constructor.setAccessible(true);
        }
        try {
            return constructor.newInstance(realArguments);
        } catch (IllegalAccessException e) {
            throw new InternalException
                ("Could not access " + constructor + "\n" + e, e);
        } catch (IllegalArgumentException e) {
            throw new InternalException
                ("Got illegal argument for " + constructor + "\n" + e,
                 e);
        } catch (InstantiationException e) {
            throw new InternalException
                ("Could not invoke " + constructor + "\n" + e, e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } finally {
            if (inaccessible) {
                constructor.setAccessible(false);
            }
        }
    }

}
