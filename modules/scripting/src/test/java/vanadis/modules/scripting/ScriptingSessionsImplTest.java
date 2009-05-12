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

import vanadis.osgi.impl.BonyReference;
import vanadis.services.scripting.AbstractScriptEngine;
import vanadis.services.scripting.AbstractScriptEngineFactory;
import org.junit.Test;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

public class ScriptingSessionsImplTest {

    @Test
    public void sessionIdentity() {
        ScriptingSessionsImpl scriptingSessions = new ScriptingSessionsImpl(null, new FooManagerFactory());
        scriptingSessions.newSession
                ("foo", "foo", new BonyReference(String.class, "a service that is a string"),
                 "reference");
    }

    private static class FooScriptEngine extends AbstractScriptEngine {

        private FooScriptEngine(ScriptEngineFactory factory) {
            super(factory);
        }

        @Override
        public Object eval(String script, Bindings bindings) {
            return null;
        }
    }

    private static class FooScriptEngineFactory extends AbstractScriptEngineFactory {

        private FooScriptEngineFactory() {
            super("foo", "1.0");
        }

        @Override
        public ScriptEngine getScriptEngine() {
            return new FooScriptEngine(this);
        }
    }

    private static class FooManagerFactory implements ManagerFactory {

        @Override
        public ScriptEngineManager newManager(String language) {
            ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
            scriptEngineManager.registerEngineName
                    ("foo", new FooScriptEngineFactory());
            return scriptEngineManager;
        }

        @Override
        public void addScriptEngineFactory(ScriptEngineFactory scriptEngineFactory) {
        }

        @Override
        public void removeScriptEngineFactory(ScriptEngineFactory scriptEngineFactory) {
        }
    }
}
