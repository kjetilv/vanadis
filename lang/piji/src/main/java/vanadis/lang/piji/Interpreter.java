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

package vanadis.lang.piji;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vanadis.lang.piji.fun.Functions;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An interpreter is mainly holder of a top-level context which it
 * fills with the standard set of internal functions, reflection
 * functions and the odd utility function.
 *
 * @author Kjetil Valstadsve
 */
public final class Interpreter {

    private static final Logger log = LoggerFactory.getLogger(Interpreter.class);

    private static final Symbol INTERPRETER = Symbol.get("interpreter");

    private static final Symbol INTER_STDIN = Symbol.get("inter-stdin");

    private static final Symbol INTER_STDOUT = Symbol.get("inter-stdout");

    private static final Symbol INTER_STDERR = Symbol.get("inter-stderr");

    private static final Parser parser = new Parser();

    private static final Context commonContext = new Context(null, Interpreter.class.getClassLoader(), false);

    private static final Interpreter bootstrap;

    public static Expression parse(String string) {
        return Interpreter.parser.parse(string);
    }

    public static Expression parse(InputStream stream) {
        return Interpreter.parser.parse(stream);
    }

    public static Expression parse(Reader reader) {
        return Interpreter.parser.parse(reader);
    }

    public static List<Expression> parseAll(String string) {
        return Interpreter.parser.parseAll(string);
    }

    public static List<Expression> parseAll(InputStream stream) {
        return Interpreter.parser.parseAll(stream);
    }

    private static List<Expression> parseAll(Reader reader) {
        return Interpreter.parser.parseAll(reader);
    }

    private static void commonBind(Symbol symbol, Object object) {
        commonContext.bind(symbol, object);
    }

    private static void commonUnbind(Symbol symbol) {
        commonContext.unbind(symbol);
    }

    private final Context context;

    private static void preload(String symb, String name) {
        Symbol symbol = Symbol.get(symb);
        if (commonContext.isBound(symbol)) {
            throw new ConfigurationException("Already bound " + symbol + " to " +
                    commonContext.lookup(symbol));
        }
        commonBind(symbol, new Loader(symbol, name));
    }

    private static Function load(String name) {
        try {
            Class<?> functionClass = commonContext.getClassLoader().loadClass(name);
            Constructor<?>[] conses = functionClass.getConstructors();
            if (conses.length == 0) {
                throw new IllegalStateException("Found class " + functionClass + " with no constructors!");
            }
            if (conses.length > 1) {
                throw new IllegalArgumentException("Cannot choose between constructors " +
                        Arrays.toString(conses) + ", trying to instantiate " +
                        functionClass);
            }
            Constructor<?> cons = conses[0];
            Class<?>[] parTypes = cons.getParameterTypes();
            if (parTypes.length != 1) {
                throw new InternalRuntimeException("Got function " + functionClass +
                        " without single-argument constructor");
            }
            Object[] args = new Object[]{commonContext};
            return (Function) cons.newInstance(args);
        }
        catch (ClassNotFoundException e) {
            throw new InternalRuntimeException("Could not find " + name + ", got " + e, e);
        }
        catch (InstantiationException e) {
            throw new InternalRuntimeException("Could not instantiate " + name + ", got " + e, e);
        }
        catch (IllegalAccessException e) {
            throw new InternalRuntimeException("Could not access constructor of " + name + ", got " + e, e);
        }
        catch (InvocationTargetException e) {
            throw new InternalRuntimeException("Failed to invokek constructor of " + name + ", got " + e, e);
        }
    }

    private static final class Loader extends AbstractFunction {

        private final Symbol symbol;

        private final String classname;

        private Loader(Symbol symbol, String classname) {
            super(symbol + "-Loader", true, 0, commonContext);
            this.symbol = symbol;
            this.classname = classname;
            commonBind(this.symbol, this);
        }

        private Function loadInternal() {
            Function fun = load(this.classname);
            commonUnbind(this.symbol);
            commonBind(this.symbol, fun);
            return fun;
        }

        @Override
        public Object apply(Context context, Expression[] nodes)
                throws Throwable {
            return loadInternal().apply(context, nodes);
        }

        @Override
        public boolean isVararg() {
            return loadInternal().isVararg();
        }

        @Override
        public int getArgumentCount() {
            return loadInternal().getArgumentCount();
        }

        @Override
        public String getDocumentationString() {
            return loadInternal().getDocumentationString();
        }

        @Override
        public Context getContext() {
            return loadInternal().getContext();
        }
    }

    private static void loadFunctions() {
        for (int i = 0; i < Functions.MAP.length; i++) {
            String[] spec = Functions.MAP[i];
            String classname = "vanadis.lang.piji.fun." + spec[0];
            preload(spec[1], classname);
        }
    }

    private static void loadLibraries() {
        try {
            String res = "library.pji";
            URL url = commonContext.getClassLoader().getResource(res);
            if (url == null) {
                log.warn("Could not find url " + res);
                return;
            }
            InputStream library = url.openStream();
            if (library == null) {
                log.warn("Could not open resources " + url);
                return;
            }
            bootstrap.evalInternal
                    (parseAll(new InputStreamReader(library)),
                     commonContext);
        } catch (FileNotFoundException e) {
            throw new ConfigurationException("Could not find library: " + e, e);
        } catch (ParseException e) {
            throw new ConfigurationException("Had problem parsing library: " + e, e);
        } catch (IOException e) {
            throw new ConfigurationException("Had problem loading library: " + e, e);
        } catch (Throwable e) {
            throw new ConfigurationException("Had problem executing library code: " + e, e);
        }
    }

    static {
        try {
            bootstrap = new Interpreter(Interpreter.class.getClassLoader());
            loadFunctions();
            loadLibraries();
        }
        catch (ConfigurationException e) {
            throw new PijiRuntimeException("Could not configure piji, got " + e, e);
        }
    }

    public Interpreter(ClassLoader classLoader) {
        Context topContext = new Context(commonContext);
        topContext.bind(Interpreter.INTER_STDIN, System.in);
        topContext.bind(Interpreter.INTER_STDOUT, System.out);
        topContext.bind(Interpreter.INTER_STDERR, System.err);
        topContext.bind(Interpreter.INTERPRETER, this);
        this.context = new Context(topContext, classLoader, false);
    }

    public Context getContext() {
        return this.context;
    }

    public Result evalResult(String string) {
        return evalResult(string, this.context);
    }

    Result evalResult(String string, Context context) {
        return evalResult(new StringReader(string), context);
    }

    public Result evalResult(InputStream reader) {
        return evalResult(reader, this.context);
    }

    Result evalResult(InputStream reader, Context context) {
        return evalResult(new InputStreamReader(reader), context);
    }

    public Result evalResult(Reader reader) {
        return evalResult(reader, this.context);
    }

    Result evalResult(Reader reader, Context context) {
        return evalInternal(parseAll(reader), context);
    }

    public Result evalResult(Expression expr) {
        return this.evalInternal(expr, this.context);
    }

    Result evalResult(Expression expr, Context context) {
        return this.evalInternal(expr, context);
    }

    private Result evalInternal(Expression expr, Context ctx) {
        return this.evalInternal(Collections.singletonList(expr), ctx);
    }

    private Result evalInternal(List<Expression> exprs, Context ctx) {
        if (ctx == null) {
            throw new NullPointerException(this + " got null context for evaluating " + exprs);
        }
        if (exprs == null || exprs.isEmpty()) {
            return null;
        }
        Result result = null;
        Expression expr = null;
        for (Expression e : exprs) {
            expr = e;
            if (expr != null) {
                Object obj;
                long startTime = 0;
                long evalTime;
                try {
                    startTime = System.currentTimeMillis();
                    obj = expr.evaluate(ctx);
                    evalTime = System.currentTimeMillis() - startTime;
                    if (obj == Context.NULL) {
                        obj = null;
                    }
                    result = Result.ok(expr, obj, evalTime);
                }
                catch (EvaluationException exc) {
                    throw exc;
                }
                catch (Throwable t) {
                    evalTime = System.currentTimeMillis() - startTime;
                    return Result.fail(expr, t, evalTime);
                }
            }
        }
        if (result == null) {
            throw new IllegalStateException(this + " got illegal null result from " + expr);
        }
        return result;
    }

    public Object eval(String string) {
        return this.eval(string, this.context);
    }

    Object eval(String string, Context context) {
        return unwrap(this.evalResult(string, context));
    }

    public Object eval(InputStream stream) {
        return eval(stream, this.context);
    }

    Object eval(InputStream stream, Context context) {
        return unwrap(this.evalResult(stream, context));
    }

    public Object eval(Reader reader) {
        return eval(reader, this.context);
    }

    Object eval(Reader reader, Context context) {
        return unwrap(this.evalResult(reader, context));
    }

    public Object eval(Expression expr) {
        return eval(expr, this.context);
    }

    Object eval(Expression expr, Context context) {
        return unwrap(this.evalResult(expr, context));
    }

    private static Object unwrap(Result res) {
        if (res.isOK()) {
            return res.getTypedValue();
        } else {
            throw new RuntimeException(res.getThrowable());
        }
    }

    @Override
    public String toString() {
        return "Interpreter[" + this.context + "]";
    }
}
