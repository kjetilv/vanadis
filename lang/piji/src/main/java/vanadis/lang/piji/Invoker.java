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

package net.sf.vanadis.lang.piji;

import net.sf.vanadis.lang.piji.hold.DataHolderFactory;
import net.sf.vanadis.lang.piji.loading.FieldFinder;
import net.sf.vanadis.lang.piji.loading.MethodFinder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Invoker extends ReflectorHelper {

    private static String argumentString(Object[] args) {
        int argCount = args.length;
        StringBuffer argString = new StringBuffer("[");
        for (int i = 0; i < argCount; i++) {
            argString.append(String.valueOf(args[i]));
            if (i < argCount - 1) {
                argString.append(", ");
            }
        }
        argString.append("]");
        return argString.toString();
    }

    public Invoker(Reflector ref) {
        super(ref);
    }

    public Object invoke(Context context, Expression[] args)
            throws Throwable {
        return invoke(context, args, 0);
    }

    public Object invoke(Context context, Expression[] args,
                         int offset)
            throws Throwable {
        Object object = getRef().resolveTarget(context, args[offset]);
        return invoke(context, object, args, offset + 1);
    }

    public Object invoke(Context context,
                         Object object, Expression[] args,
                         int offset)
            throws Throwable {
        String name = Reflector.getName(args[offset], context);
        MethodFinder finder = getRef().getMethodFinder();
        RealAndFormalArrays arrays =
                new RealAndFormalArrays(getRef(), offset + 1, args, context);
        Method method = finder.getMethod(object, name, arrays.getSignature(), true);

        if (method == null && !name.startsWith("get")) {
            String getName =
                    "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
            method = finder.getMethod(object, getName, arrays.getSignature(), true);
        }

        if (method != null) {
            return invokeMethod(object, method, arrays.getRealArguments());
        }

        if (args.length - offset == 1) {
            FieldFinder ff = getRef().getFieldFinder();
            Field field = ff.getField(object, name, true);
            if (field != null) {
                return getRef().getAccessor().access(object, field);
            } else {
                throw new InternalException
                        ("No method, property or field " + "\"" + name +
                                "\"" + " in " + object + "(" + object.getClass() + ")");
            }
        } else {
            throw new InternalException
                    ("No method " + (name.startsWith("get")
                            ? ""
                            : "or property ") + "\"" + name + "\"" +
                            (args.length == 0
                                    ? ""
                                    : " for " + Arrays.toString(args)) +
                            " in " + object + "(" + object.getClass() +
                            ")");
        }
    }

    private static Object invokeMethod(Object object,
                                       Method method,
                                       Object[] realArguments)
            throws Throwable {
        if (object == null) {
            throw new InternalException
                    ("Got null target for " + method +
                            " " + argumentString(realArguments));
        }
        boolean inaccessible = !method.isAccessible();
        if (inaccessible) {
            method.setAccessible(true);
        }
        try {
            Object value = method.invoke(object, realArguments);
            Class<?> declared = method.getReturnType();
            return resolveReturnValue(object, value, declared);
        } catch (IllegalAccessException e) {
            throw new InternalException
                    ("Could not access " + method + ", declared by " +
                            method.getDeclaringClass() +
                            " in " + Reflector.resolveType(object) + " of " +
                            Reflector.resolveType(object) + " :\n" + e, e);
        } catch (IllegalArgumentException e) {
            throw new InternalException
                    ("Got invalid object for method:\n  " + method +
                            "\ndeclared by:\n  " + method.getDeclaringClass() +
                            "\nin object:\n  " + Reflector.resolveObject(object) +
                            " (" + object + ")" +
                            "\nof class:\n  " + Reflector.resolveType(object, true) +
                            "\narguments:\n  " +
                            argumentString(realArguments) + ":\n  " + e, e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } finally {
            if (inaccessible) {
                method.setAccessible(false);
            }
        }
    }

    private static Object resolveReturnValue(Object target, Object result,
                                             Class<?> resultType) {
        return resultType == Void.TYPE ? target
                : resultType.isPrimitive() ? DataHolderFactory.primitiveHolder(result, resultType)
                        : result;
    }

}

