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

package net.sf.vanadis.core.reflection;

import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.lang.AccessibleHelper;
import net.sf.vanadis.core.lang.Not;
import net.sf.vanadis.core.lang.VarArgs;
import net.sf.vanadis.core.properties.PropertySet;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Map;

public final class Invoker {

    private static final String SET_PREFIX = "set";

    public static Object invokeReplacedCoerced(Object invoker, Object target, Method method,
                                               PropertySet propertySet,
                                               Object... args) {
        Object[] replaced = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            replaced[i] = resolve(propertySet, args[i]);
        }
        return invoke(invoker, target, method, coerced(method, replaced));
    }

    private static String resolve(PropertySet propertySet, Object value) {
        String string = String.valueOf(value);
        return string == null ? null : propertySet.resolve(string);
    }

    public static Object invokeCoerced(Object invoker, Object target, Method method, Object... args) {
        return invoke(invoker, target, method, coerced(method, args));
    }

    private static Object coerced(Field field, Object value) {
        return coerced(field.getType(), value);
    }

    private static Object[] coerced(Method method, Object... args) {
        Class<?>[] parameterTypes = cardinalityChecked(method, args);
        Object[] coerced = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            coerced[i] = coerced(parameterTypes[i], args[i]);
        }
        return coerced;
    }

    private static Object coerced(Class<?> type, Object value) {
        return Enum.class.isAssignableFrom(type)
                ? Enums.get(type, value)
                : Retyper.coerce(type, value);
    }

    public static Method getSetter(Object target, String attribute, Class<?> type) {
        String methodName =
                SET_PREFIX + attribute.substring(0, 1).toUpperCase() +
                        attribute.substring(1);
        return getMethod(target, methodName, type);
    }

    public static Object invokeTypeChecked(Object invoker, Object target, Method method,
                                           Object... args) {
        checkInvocation(method, target, args);
        return invoke(invoker, target, method, args);
    }

    private static void checkInvocation(Method method, Object target, Object... args) {
        if (!method.getDeclaringClass().isInstance(target)) {
            throw new InvokeException
                    ("Invocation target " + target + " is an instanceof " +
                            target.getClass() + ", which does not match method " +
                            method);
        }
        Class<?>[] parameterTypes = cardinalityChecked(method, args);
        verifyParameterTypes(method, parameterTypes, args);
    }

    private static void verifyParameterTypes(Method method, Class<?>[] parameterTypes, Object... args) {
        for (int i = 0; i < args.length; i++) {
            if (!parameterTypes[i].isInstance(args[i])) {
                throw new InvokeException
                        (method + " argument #" + i + " is of " + parameterTypes[i] +
                                ", cannot pass " + args[i] + " of " + args[i].getClass());
            }
        }
    }

    private static Class<?>[] cardinalityChecked(Method method, Object... args) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != args.length) {
            throw new InvokeException
                    (method + " does not accept " + args.length + " parameters: " +
                            Arrays.toString(args));
        }
        return parameterTypes;
    }

    public static void assignCoerced(Object assigner, Object target, Field field, Object value) {
        assign(assigner, target, field, coerced(field, value));
    }

    public static void assignReplacedCoerced(Object assigner, Object target, Field field,
                                             PropertySet variables, Object value) {
        assign(assigner, target, field, coerced(field, resolve(variables, value)));
    }

    public static void assign(Object assigner, Object target, Field field, Object value) {
        if (isType(field, value)) {
            if (!field.isAccessible()) {
                AccessibleHelper.openSesame(field);
            }
            try {
                field.set(target, value);
            } catch (IllegalAccessException e) {
                throw new InvokeException(assignFailureMessage(assigner, target, field, value), e);
            }
        }
    }

    public static Object get(Object getter, Object target, Field field) {
        return get(getter, target, field, false);
    }

    public static Object get(Object getter, Object target, Field field, boolean force) {
        try {
            return makeAccessible(field, force).get(target);
        } catch (IllegalAccessException e) {
            throw new InvokeException(getter + " failed to get " + field + " from " + target, e);
        }
    }

    private static boolean isType(Field field, Object value) {
        Class<?> type = field.getType();
        return type.isInstance(value) || type.isPrimitive() && isPrimitiveType(type, value);
    }

    private static boolean isPrimitiveType(Class<?> type, Object value) {
        Class<?> wrapper = PRIMS.get(type);
        return wrapper != null && wrapper.isInstance(value);
    }

    private static final Map<Class<?>, Class<?>> PRIMS = Generic.map();

    static {
        PRIMS.put(int.class, Integer.class);
        PRIMS.put(long.class, Long.class);
        PRIMS.put(short.class, Short.class);
        PRIMS.put(byte.class, Byte.class);
        PRIMS.put(float.class, Float.class);
        PRIMS.put(double.class, Double.class);
        PRIMS.put(boolean.class, Boolean.class);
    }

    public static Object get(Object invoker, Object target, String property) {
        return get(invoker, target, property, false);
    }

    public static Object getByForce(Object invoker, Object target, Field field) {
        return get(invoker, target, field, true);
    }

    public static Object getByForce(Object invoker, Object target, String property) {
        return get(invoker, target, property, true);
    }

    public static Object get(Object invoker, Object target, String property, boolean force) {
        Class<?> clazz = Not.nil(target, "target").getClass();
        Method getter;
        try {
            getter = clazz.getMethod(GetNSet.getterName(property));
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(target + " has no property '" + property + "'", e);
        }
        return invoke(invoker, target, makeAccessible(getter, force));
    }

    private static <T extends AccessibleObject> T makeAccessible(T accessible, boolean force) {
        if (!accessible.isAccessible() && force) {
            AccessibleHelper.openSesame(accessible);
        }
        return accessible;
    }

    public static Object invoke(Object invoker, Object target, Method method, Object... args) {
        if (args.length != method.getParameterTypes().length) {
            throw new InvokeException
                    (invoker + " passed " + args.length + " arguments " + Arrays.toString(args) +
                            " to " + method);
        }
        try {
            return method.invoke(target, args);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("argument type mismatch")) {
                return detailedMismatchException(invoker, method, e, args);
            } else {
                throw new InvokeException
                        (invokeFailureMessage(invoker, target, method, args), e);
            }
        } catch (IllegalAccessException e) {
            throw new InvokeException
                    (invokeFailureMessage(invoker, target, method, args), e);
        } catch (InvocationTargetException e) {
            throw new InvokeException
                    (invokeFailureMessage(invoker, target, method, args), e);
        }
    }

    private static String invokeFailureMessage(Object invoker, Object target,
                                               Method method, Object... args) {
        String argsString = VarArgs.present(args)
                ? "with args " + Arrays.toString(args)
                : "";
        String invocation = method.getClass().getName() + "." + method.getName() + argsString;
        return invoker + " failed to invoke " + invocation + " on " + target;
    }

    private static String assignFailureMessage(Object assigner, Object target,
                                               Field field, Object arg) {
        return assigner + " failed to assign " + arg + " to " + field + " on " + target;
    }

    private static Object detailedMismatchException(Object invoker,
                                                    Method method,
                                                    IllegalArgumentException e,
                                                    Object... args) {
        throw new InvokeException
                (invoker + " passed " +
                        (args.length == 1 ? String.valueOf(args[0]) :
                                "args " + Arrays.toString(args)) + " to " + method +
                        (args.length == 1 ? ", expected instance of " +
                                method.getParameterTypes()[0] +
                                ", got " + args[0].getClass()
                                : ""), e);
    }

    public static Method getMethod(Object target, String name, Class<?>... formalParameters) {
        return getMethod(Not.nil(target, "target").getClass(),
                         Not.nil(name, "method name"),
                         formalParameters);
    }

    public static Method getMethod(Class<?> type, String name, Class<?>... formalParameters) {
        Not.nil(name, "method name");
        Method[] methods = type.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(name)) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    Class<?>[] realParameters = method.getParameterTypes();
                    if (match(realParameters, formalParameters)) {
                        return method;
                    }
                }
            }
        }
        return null;
    }

    private static boolean match(Class<?>[] realParameters, Class<?>... parameters) {
        if (realParameters.length == parameters.length) {
            if (parameters.length == 0) {
                return true;
            }
            for (int i = 0; i < parameters.length; i++) {
                if (!parameters[i].equals(realParameters[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private Invoker() { }
}
