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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

@EntryPoint("Reflection")
public final class ImplementFunction extends AbstractFunction {

    @EntryPoint("Reflection")
    public ImplementFunction(Context ctx) {
        super("Implement method in proxy: <proxy> <method name> <lambda>",
              true, 3, ctx);
    }

    @Override
    public Object apply(Context context, Expression[] args)
            throws Throwable {
        checkArgumentCount(args);

        ImplementHandler implHandler;
        Object proxy = args[1].evaluate(context);
        try {
            InvocationHandler handler = Proxy.getInvocationHandler(proxy);
            if (handler instanceof ImplementHandler) {
                implHandler = (ImplementHandler) handler;
            } else {
                throw new EvaluationException
                        (this + " could not work with proxy " + proxy +
                                ", which has unknown invocation handler " + handler);
            }
        } catch (IllegalArgumentException e) {
            throw new EvaluationException
                    (this + " found no invocation handler, probably non-proxy: " + proxy, e);
        }

        String name = String.valueOf(isSymbol(args[2])
                ? checkSymbol(args[2])
                : args[2].evaluate(context));
        Function function = getFunctionObject(context, args[3]);
        implHandler.implement(name, function);
        return this;
    }

    private Function getFunctionObject(Context context, Expression arg)
            throws Throwable {
        Object functionObject = arg.evaluate(context);
        if (functionObject instanceof Function) {
            return (Function) functionObject;
        } else {
            throw new EvaluationException
                    (this + " got non-function: " + functionObject);
        }
    }

}

