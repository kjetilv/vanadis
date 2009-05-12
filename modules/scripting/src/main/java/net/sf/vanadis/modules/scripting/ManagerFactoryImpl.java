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
package net.sf.vanadis.modules.scripting;

import net.sf.vanadis.core.collections.Generic;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.util.Map;

class ManagerFactoryImpl implements ManagerFactory {

    private final Map<String, ScriptEngineFactory> scriptEngineFactories = Generic.map();

    @Override
    public void removeScriptEngineFactory(ScriptEngineFactory scriptEngineFactory) {
        scriptEngineFactories.remove(scriptEngineFactory.getLanguageName());
    }

    @Override
    public void addScriptEngineFactory(ScriptEngineFactory scriptEngineFactory) {
        String name = scriptEngineFactory.getLanguageName();
        scriptEngineFactories.put(name, scriptEngineFactory);
    }

    @Override
    public ScriptEngineManager newManager(String language) {
        for (Map.Entry<String, ScriptEngineFactory> entry : scriptEngineFactories.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(language)) {
                ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
                scriptEngineManager.registerEngineName(language, entry.getValue());
                return scriptEngineManager;
            }
        }
        ScriptEngineManager defaultManager = new ScriptEngineManager();
        for (ScriptEngineFactory factory : defaultManager.getEngineFactories()) {
            if (factory.getLanguageName().equalsIgnoreCase(language)) {
                return defaultManager;
            }
        }
        throw new IllegalArgumentException("Unregistered language: " + language);
    }
}
