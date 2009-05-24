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

import vanadis.ext.*;
import vanadis.services.scripting.ScriptingSessions;

import javax.script.ScriptEngineFactory;
import javax.servlet.Servlet;

@Module(moduleType = "scripting", launch = @AutoLaunch(name = "scripting"))
public class ScriptingModule extends AbstractContextAware {

    private final ManagerFactory managerFactory = new ManagerFactoryImpl();

    private ScriptingSessionsImpl scriptingSessions;

    @Configure(required = false, def = DEFAULT_VALUE)
    private String defaultLanguage;

    public ScriptingModule() {
        this(null);
    }

    public ScriptingModule(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    @Override
    public void dependenciesResolved() {
        scriptingSessions = new ScriptingSessionsImpl(defaultLanguage, managerFactory);
    }

    @Inject(required = false)
    public void addScriptEngineFactory(ScriptEngineFactory scriptEngineFactory) {
        managerFactory.addScriptEngineFactory(scriptEngineFactory);
    }

    @Retract
    public void removeScriptEngineFactory(ScriptEngineFactory scriptEngineFactory) {
        managerFactory.removeScriptEngineFactory(scriptEngineFactory);
    }

    @Expose
    public Command[] getCommands() {
        return new Command[]{new GenericCommand("v-eval", "evaluate script", context(),
                                                new ScriptExecution(scriptingSessions)),
                             new GenericCommand("v-new-session", "create new session", context(),
                                                new NewSessionExecution(scriptingSessions))};
    }

    @Expose(properties = @Property(name = SERVLET_ALIAS, value = SCRIPTING_ROOT))
    public Servlet getScriptingServlet() {
        return new ScriptingServlet(scriptingSessions, requiredContext());
    }

    @Expose
    public ScriptingSessions getScriptSessions() {
        return scriptingSessions;
    }

    private static final String SERVLET_ALIAS = "servlet.alias";

    private static final String DEFAULT_VALUE = "piji";

    private static final String SCRIPTING_ROOT = "scripting";
}
