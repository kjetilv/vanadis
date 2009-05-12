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
package vanadis.modules.scripting;

import vanadis.core.lang.EqHc;
import vanadis.core.lang.ToString;
import vanadis.osgi.Reference;
import vanadis.services.scripting.ScriptingException;
import vanadis.services.scripting.ScriptingSession;

import javax.script.*;

class ScriptingSessionImpl implements ScriptingSession {

    static ScriptingSessionImpl create(String name, String language, String serviceBinding,
                                       ScriptEngineManager scriptEngineManager,
                                       Reference<?> reference,
                                       ScriptingSessionsImpl host) {
        return new ScriptingSessionImpl(name, language,
                                        serviceBinding, scriptEngineManager,
                                        reference, host);
    }

    private final String name;

    private final ScriptingSessionsImpl host;

    private final String language;

    private final Reference<?> reference;

    private final String serviceBinding;

    private final ScriptEngine scriptEngine;

    private final Bindings bindings;

    private ScriptingSessionImpl(String name, String language, String serviceBinding,
                                 ScriptEngineManager scriptEngineManager,
                                 Reference<?> reference,
                                 ScriptingSessionsImpl host) {
        this.reference = reference;
        this.name = name;
        this.language = language;
        this.bindings = new SimpleBindings();
        this.scriptEngine = scriptEngineManager.getEngineByName(language);
        this.serviceBinding = bindService(serviceBinding);
        this.host = host;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLanguage() {
        return language;
    }

    @Override
    public Object eval(String script) {
        try {
            return scriptEngine.eval(script, bindings);
        } catch (ScriptException e) {
            throw new ScriptingException("Failed to evaluate " + script, e);
        }
    }

    @Override
    public void close() {
        try {
            host.clearSession(name);
        } finally {
            if (reference != null) {
                reference.unget();
            }
        }
    }

    boolean is(String name, String language) {
        return this.name.equals(name) && this.language.equals(language);
    }

    private String bindService(String serviceBinding) {
        if (this.reference == null) {
            return null;
        }
        Object service = this.reference.getRawService();
        this.bindings.put(serviceBinding, service);
        this.scriptEngine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
        return serviceBinding;
    }

    @Override
    public boolean equals(Object obj) {
        ScriptingSessionImpl session = EqHc.retyped(this, obj);
        return session != null && EqHc.eq(name, session.name,
                                          language, session.language);
    }

    @Override
    public int hashCode() {
        return EqHc.hc(name, language);
    }

    @Override
    public String toString() {
        return ToString.of(this, "name", name, "lang", language,
                           "reference", reference,
                           "bound to", serviceBinding);
    }
}