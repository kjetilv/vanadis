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

import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.lang.piji.hold.Holder;
import net.sf.vanadis.lang.piji.loading.MethodFinder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ImplementHandler implements InvocationHandler {

    private static final List<Method> OBJECT_METHODS;

    static {
        try {
            Method[] methods = Object.class.getDeclaredMethods();
            OBJECT_METHODS = Arrays.asList(methods);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException
                    ("Got " + e + " while loading " + ImplementHandler.class, e);
        }
    }

    private static String nameArity(Method method) {
        return nameArity(method.getName(), method.getParameterTypes().length);
    }

    private static String nameArity(String name, int arity) {
        return name + "/" + arity;
    }

    private final Class<?>[] interfaces;

    private final Context context;

    private final Map<Method, Function> methodIndex = Generic.map();

    private final Map<String, Function> nameArityIndex = Generic.map();

    public ImplementHandler(Class<?>[] interfaces, Context context) {
        this.interfaces = interfaces;
        this.context = new Context(context);
    }

    public Context getContext() {
        return this.context;
    }

    public void implement(String methodName, Function function) throws Throwable {
        implement(methodName, null, function);
    }

    void implement(String methodName,
                   List<Class<?>> types,
                   Function function) throws Throwable {
        int argCount = function.getArgumentCount();
        MethodFinder finder = Reflector.get(false).getMethodFinder();
        if (types == null) {
            Method method = null;
            try {
                for (int i = 0; i < interfaces.length && method == null; i++) {
                    method = MethodFinder.getMethod(interfaces[i], methodName, argCount, true);
                }
                methodIndex.put(method, function);
            } catch (InternalException ignore) {
                nameArityIndex.put(nameArity(methodName, argCount), function);
            }
        } else {
            Method method = null;
            Class<?>[] classes = types.toArray(new Class[argCount]);
            for (int i = 0; i < interfaces.length && method == null; i++) {
                method = finder.getMethod(interfaces[i], methodName, classes, true);
            }
            methodIndex.put(method, function);
        }
    }

    @Override
    public Object invoke(Object object, Method method, Object[] args) throws Throwable {
        Function function = null;

        if (methodIndex.containsKey(method)) {
            function = methodIndex.get(method);
        } else {
            String nameArity = nameArity(method);
            if (nameArityIndex.containsKey(nameArity)) {
                function = nameArityIndex.get(nameArity);
            }
        }

        if (function == null) {
            if (OBJECT_METHODS.contains(method)) {
                return method.invoke(this, args);
            }
            return object;
        }

        LeafNode[] nodes;

        if (args == null) {
            nodes = new LeafNode[1];
        } else {
            nodes = new LeafNode[args.length + 1];
            for (int i = 0; i < args.length; i++) {
                nodes[i + 1] = new LeafNode(args[i]);
            }
        }

        Object value = function.apply(this.context, nodes);

        if (value instanceof Holder) {
            return ((Holder) value).getObject();
        }
        return value;
    }

    @Override
    public String toString() {
        return "ImplementHandler[" + super.toString() + ", " +
                Arrays.toString(interfaces) +
                (nameArityIndex.isEmpty()
                        ? ", alt name/arity"
                        : ", " + nameArityIndex.size() +
                                ", name/arit" + (methodIndex.size() > 1 ? "ies" : "y") +
                                ": " + nameArityIndex.keySet()) +
                (methodIndex.isEmpty()
                        ? ", alt resolved methods"
                        : ", " + methodIndex.size() +
                                " resolved method" + (methodIndex.size() > 1 ? "s" : "") +
                                ": " + methodIndex.keySet()) +
                " ctx:" + this.context + "]";
    }

}
