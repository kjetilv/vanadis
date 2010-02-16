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
package vanadis.services.scripting;

import vanadis.common.io.IO;

import javax.script.*;
import java.io.Reader;

public abstract class AbstractScriptEngine implements ScriptEngine {

    private final ScriptEngineFactory factory;

    protected AbstractScriptEngine(ScriptEngineFactory factory) {
        this.factory = factory;
    }

    @Override
    public Object eval(String script, ScriptContext scriptContext)
            throws ScriptException {
        return eval(script, scriptContext == null
                ? null
                : scriptContext.getBindings(ScriptContext.ENGINE_SCOPE));
    }

    @Override
    public Object eval(Reader reader, ScriptContext scriptContext)
            throws ScriptException {
        return eval(IO.toString(reader));
    }

    @Override
    public Object eval(String script)
            throws ScriptException {
        return eval(script, (ScriptContext) null);
    }

    @Override
    public Object eval(Reader reader)
            throws ScriptException {
        return eval(IO.toString(reader), (ScriptContext) null);
    }

    @Override
    public Object eval(Reader reader, Bindings bindings)
            throws ScriptException {
        return eval(IO.toString(reader), bindings);
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return factory;
    }

    @Override
    public void put(String name, Object object) {

    }

    @Override
    public Object get(String s) {
        return null;
    }

    @Override
    public Bindings getBindings(int i) {
        return null;
    }

    @Override
    public void setBindings(Bindings bindings, int scope) {
        if (scope == ScriptContext.ENGINE_SCOPE) {
        }
    }

    @Override
    public Bindings createBindings() {
        return null;
    }

    @Override
    public ScriptContext getContext() {
        return null;
    }

    @Override
    public void setContext(ScriptContext scriptContext) {
    }
}
