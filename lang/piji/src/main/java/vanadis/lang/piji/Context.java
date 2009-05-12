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

package vanadis.lang.piji;

import vanadis.core.collections.Generic;
import vanadis.core.lang.TraverseIterable;
import vanadis.lang.piji.hold.DataHolderFactory;
import vanadis.lang.piji.loading.ClassResolver;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Context {

    private static class ContextIterable extends TraverseIterable<Context> {

        @Override
        protected Context getNext(Context current) {
            return current.getParent();
        }

        private ContextIterable(Context start) {
            super(start);
        }
    }

    public static final Object NULL = new Object() {

        @Override
        public String toString() {
            return "NULL";
        }
    };

    private static ContextIterable towardsTheRoot(Context context) {
        return new ContextIterable(context);
    }

    public static final Symbol CLASS_PATH = Symbol.get("class-path");

    private static final Symbol CLASS_LOADER = Symbol.get("class-loader");

    private static final Symbol CLASS_RESOLVER = Symbol.get("class-resolver");

    private static ClassLoader chooseOriginLoader() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader != null) {
            return loader;
        }
        return ClassLoader.getSystemClassLoader();
    }

    private final Map<Symbol, Object> bindings = Generic.map();

    private final ClassResolver classResolver;

    private final ClassLoader classLoader;

    private final Context parent;

    private final boolean recursive;

    private ClassResolver resolveResolver() {
        return this.classLoader == null
                ? null
                : new ClassResolver(this.classLoader);
    }

    public Context() {
        this(null, false);
    }

    public Context(Context parent) {
        this(parent, true);
    }

    private Context(Context parent, boolean recursive) {
        this.parent = parent;
        this.recursive = this.parent != null && recursive;
        this.classLoader = parent == null
                ? chooseOriginLoader()
                : null;
        this.classResolver = resolveResolver();
        this.initContext();
    }

    public Context(Context parent, ClassLoader loader, boolean recursive) {
        if (parent == null && recursive) {
            throw new IllegalArgumentException("Null parent, but recursive is true!");
        }
        this.parent = parent;
        this.recursive = recursive;
        this.classLoader = loader;
        this.classResolver = resolveResolver();
        this.initContext();
    }

    ContextIterable towardsTheRoot() {
        return towardsTheRoot(this);
    }

    private void initContext() {
        if (this.classResolver != null) {
            this.bind(CLASS_LOADER, getClassLoader());
            this.bind(CLASS_RESOLVER, getClassResolver());
        }
    }

    public ClassLoader getClassLoader() {
        return this.getClassResolver().getClassLoader();
    }

    public ClassResolver getClassResolver() {
        for (Context ctx : towardsTheRoot()) {
            if (ctx.classResolver != null) {
                return ctx.classResolver;
            }
        }
        throw new IllegalStateException(this + " found no classResolver, should be one at top");
    }

    final Context getParent() {
        return this.parent;
    }

    public final Iterator<Object> symbols() {
        Collection<Object> collection = Generic.list();
        for (Context ctx : towardsTheRoot()) {
            collection.addAll(ctx.bindings.keySet());
        }
        return collection.iterator();
    }

    public final Iterator<Symbol> symbolsLocal() {
        return this.bindings.keySet().iterator();
    }

    public final Object set(Symbol symbol, Object value) {
        for (Context ctx = this; ctx != null; ctx = (ctx.recursive
                ? ctx.parent
                : null)) {
            if (ctx.bindings.containsKey(symbol)) {
                Object bound = ctx.bindings.get(symbol);
                ctx.bind(symbol, value);
                return bound;
            }
        }
        this.bind(symbol, value);
        return null;
    }

    public final Object lookup(Symbol symbol) {
        for (Context ctx = this; ctx != null; ctx = ctx.parent) {
            if (ctx.bindings.containsKey(symbol)) {
                return ctx.bindings.get(symbol);
            }
        }
        return null;
    }

    public boolean isBound(String name) {
        return isBound(Symbol.get(name));
    }

    public final Object set(String name, Object value) {
        return set(Symbol.get(name), value);
    }

    public Symbol reverseLookup(Object object) {
        for (Context ctx = this; ctx != null; ctx = ctx.parent) {
            Set<Map.Entry<Symbol, Object>> entries = ctx.bindings.entrySet();
            for (Map.Entry<Symbol, Object> entry : entries) {
                if (entry.getValue().equals(object)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public Object lookup(String name) {
        return lookup(Symbol.get(name));
    }

    public final Object unbind(String name) {
        return unbind(Symbol.get(name));
    }

    public final void bind(String name, Object value) {
        bind(Symbol.get(name), value);
    }

    public final void bind(Symbol symbol, Object value) {
        this.bindings.put(symbol, (value == null ? Context.NULL : value));
    }

    public final Object unbind(Symbol symbol) {
        for (Context ctx = this; ctx != null; ctx = (ctx.recursive ? ctx.parent : null)) {
            if (ctx.bindings.containsKey(symbol)) {
                return ctx.bindings.remove(symbol);
            }
        }
        return null;
    }

    public final boolean isBound(Symbol symbol) {
        for (Context ctx = this; ctx != null; ctx = ctx.parent) {
            if (ctx.bindings.containsKey(symbol)) {
                return true;
            }
        }
        return false;
    }

    public final void bind(String name, int val) {
        bind(Symbol.get(name), DataHolderFactory.holder(val));
    }

    public final void bind(String name, float val) {
        bind(Symbol.get(name), DataHolderFactory.holder(val));
    }

    public final void bind(String name, long val) {
        bind(Symbol.get(name), DataHolderFactory.holder(val));
    }

    public final void bind(String name, double val) {
        bind(Symbol.get(name), DataHolderFactory.holder(val));
    }

    public final void bind(String name, byte val) {
        bind(Symbol.get(name), DataHolderFactory.holder(val));
    }

    public final void bind(String name, char val) {
        bind(Symbol.get(name), DataHolderFactory.holder(val));
    }

    public final void bind(String name, short val) {
        bind(Symbol.get(name), DataHolderFactory.holder(val));
    }

    public final void bind(String name, boolean val) {
        bind(Symbol.get(name), DataHolderFactory.holder(val));
    }

    private String bindingsCount() {
        StringBuffer sb = new StringBuffer();
        for (Context ctx = this; ctx != null; ctx = ctx.parent) {
            if (ctx.classResolver != null) {
                sb.append(ctx.bindings.size());
                if (ctx.parent != null) {
                    sb.append(":");
                }
            }
        }
        return sb.toString();
    }

    @Override
    public final String toString() {
        return "Context[" + bindingsCount() + "]";
    }
}
