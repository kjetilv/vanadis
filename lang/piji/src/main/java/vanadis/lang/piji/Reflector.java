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
import net.sf.vanadis.lang.piji.hold.Holder;
import net.sf.vanadis.lang.piji.hold.PrimitiveHolder;
import net.sf.vanadis.lang.piji.loading.ConstructorFinder;
import net.sf.vanadis.lang.piji.loading.FieldFinder;
import net.sf.vanadis.lang.piji.loading.MethodFinder;

public class Reflector {

    private static final Reflector ref = new Reflector(false);

    private static final Reflector pref = new Reflector(true);

    public static Reflector get() {
        return get(false);
    }

    /**
     * Return a reflector, with optional capacity for private members.
     *
     * @param privates Set to true if the reflector should access
     *                 private members
     * @return A suitable reflector
     */
    public static Reflector get(boolean privates) {
        return privates ? pref : ref;
    }

    private final MethodFinder methodFinder;

    private final ConstructorFinder constructorFinder;

    private final FieldFinder fieldFinder;

    private final Invoker invoker;

    private final Accessor accessor;

    private final Ctor ctor;

    private Reflector(boolean privates) {
        this.methodFinder = new MethodFinder(privates);
        this.constructorFinder = new ConstructorFinder(privates);
        this.fieldFinder = new FieldFinder(privates);
        this.invoker = new Invoker(this);
        this.accessor = new Accessor(this);
        this.ctor = new Ctor(this);
    }

    public MethodFinder getMethodFinder() {
        return this.methodFinder;
    }

    public FieldFinder getFieldFinder() {
        return this.fieldFinder;
    }

    public ConstructorFinder getConstructorFinder() {
        return this.constructorFinder;
    }

    public Invoker getInvoker() {
        return this.invoker;
    }

    public Accessor getAccessor() {
        return this.accessor;
    }

    public Ctor getCtor() {
        return this.ctor;
    }

    private static Object getNameIfThere(Expression arg, Context ctx)
            throws Throwable {
        if (arg instanceof LeafNode) {
            if (((LeafNode) arg).isContentSymbol()) {
                return ((LeafNode) arg).getSymbolContent().getName();
            } else if (((LeafNode) arg).isContentString()) {
                return ((LeafNode) arg).getStringContent();
            }
        }
        Object value = arg.evaluate(ctx);
        if (value instanceof Symbol || value instanceof String) {
            return String.valueOf(value);
        }
        return value;
    }

    public static String getName(Expression arg, Context ctx)
            throws Throwable {
        Object name = getNameIfThere(arg, ctx);
        if (name instanceof String) {
            return (String) name;
        } else {
            throw new InternalException
                    ("Could not read name from " + arg);
        }
    }

    public static Class<?> getClass(Expression node, Context ctx)
            throws Throwable {
        Object value = getNameIfThere(node, ctx);
        if (value instanceof String) {
            String typeName = (String) value;
            Class<?> type = getClass(typeName, ctx);
            if (type == null) {
                Object object = ctx.lookup(Symbol.get(typeName));
                if (object == null) {
                    throw new InternalException
                            ("Found no type denoted by " + typeName +
                                    " for " + ctx);
                } else {
                    return resolveType(object);
                }
            }
            return type;
        }
        return resolveType(value);
    }

    static Class<?> getClass(String typeName, Context ctx)
            throws Throwable {
        return ctx.getClassResolver().findClass(typeName, false);
    }

    static Class<?> getInterface(Expression arg, Context ctx)
            throws Throwable {
        Class<?> type = getClass(arg, ctx);
        if (type.isInterface()) {
            return type;
        } else {
            throw new InternalException
                    ("Could not evaluate " + arg +
                            " to interface type, got: " + type);
        }
    }

    public static Class<?>[] getInterfaces(int offset, Expression[] args, Context ctx)
            throws Throwable {
        Class<?>[] interfaces = new Class[args.length - offset];
        for (int i = offset; i < args.length; i++) {
            interfaces[i - offset] = getInterface(args[i], ctx);
        }
        return interfaces;
    }

    public static Class<?>[] getClasses(Expression[] args, Context ctx)
            throws Throwable {
        Class<?>[] classes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            classes[i] = getClass(args[i], ctx);
        }
        return classes;
    }

    public final Object resolveLeafNode(Context ctx, LeafNode node)
            throws Throwable {
        Symbol symb = ExpressionCheck.checkSymbol(this, node, false);
        Object object;
        object = symb == null ? node.evaluate(ctx)
                : ctx.isBound(symb) ? ctx.lookup(symb)
                        : getClass(symb.getName(), ctx);
        if (object == null) {
            throw new BadArgumentException((symb == null
                    ? "Null target from " + node
                    : "Unknown symbol " + symb));
        }
        if (object instanceof PrimitiveHolder) {
            throw new BadArgumentException
                    ("Invalid primitive value " + object);
        }
        return object;
    }

    public final Object resolveTarget(Context ctx, Expression node)
            throws Throwable {
        return node instanceof LeafNode ? resolveLeafNode(ctx, (LeafNode) node)
                : node.evaluate(ctx);
    }

    public static Object resolveObject(Object object) {
        return DataHolderFactory.drop(object);
    }

    public static Class<?> resolveType(Object object) {
        return resolveType(object, false);
    }

    public static Class<?> resolveType(Object object, boolean acceptClass) {
        return object == null
                ? null
                : (object instanceof Class
                        ? (acceptClass
                        ? Class.class
                        : (Class<?>) object)
                        : (object instanceof Holder
                                ? ((Holder) object).getType()
                                : object.getClass()));
    }

}
