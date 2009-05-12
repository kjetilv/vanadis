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
package net.sf.vanadis.lang.piji.jsr223;

import net.sf.vanadis.lang.piji.Context;
import net.sf.vanadis.lang.piji.Interpreter;
import net.sf.vanadis.lang.piji.Result;
import net.sf.vanadis.lang.piji.Symbol;
import net.sf.vanadis.services.scripting.AbstractScriptEngine;

import javax.script.Bindings;
import javax.script.ScriptException;

public class PijiScriptEngine extends AbstractScriptEngine {

    private final Interpreter interpreter;

    public PijiScriptEngine() {
        super(PijiScriptEngineFactory.SINGLETON);
        interpreter = new Interpreter(Thread.currentThread().getContextClassLoader());
    }

    @Override
    public Object eval(String script, Bindings bindings)
        throws ScriptException {
        if (bindings != null) {
            Context context = interpreter.getContext();
            for (String key : bindings.keySet()) {
                Object object = bindings.get(key);
                context.bind(Symbol.get(key), object);
            }
        }
        Result result = interpreter.evalResult(script);
        if (result.isOK()) {
            return result.isWrapped() ? result.getWrappedValue().getObject() : result.getValue();
        }
        throw new ScriptException(result.getThrowable() instanceof Exception
            ? (Exception) result.getThrowable()
            : new RuntimeException(result.getThrowable()));
    }
}
