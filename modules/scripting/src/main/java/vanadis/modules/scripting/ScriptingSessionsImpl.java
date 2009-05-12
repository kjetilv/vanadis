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

import vanadis.core.collections.Generic;
import vanadis.core.lang.Not;
import vanadis.core.lang.Strings;
import vanadis.core.lang.ToString;
import vanadis.osgi.Reference;
import vanadis.services.scripting.ScriptingSession;
import vanadis.services.scripting.ScriptingSessions;

import javax.script.ScriptEngineManager;
import java.util.Iterator;
import java.util.Map;

class ScriptingSessionsImpl implements ScriptingSessions {

    private final String defaultLanguage;

    private final ManagerFactory managerFactory;

    private final Map<String, ScriptingSessionImpl> sessions = Generic.map();

    ScriptingSessionsImpl(String defaultLanguage, ManagerFactory managerFactory) {
        this.defaultLanguage = Strings.neitherNullNorEmpty(defaultLanguage);
        this.managerFactory = managerFactory;
    }

    @Override
    public Iterable<String> sessionNames() {
        return sessions.keySet();
    }

    @Override
    public ScriptingSession getSession(String name) {
        return sessions.get(name);
    }

    @Override
    public ScriptingSession newSession(String name, String language, Reference<?> service, String serviceBinding) {
        Not.nil(name, "name");
        Not.nil(language, "language");
        if (Strings.isBlank(language) && defaultLanguage == null) {
            throw new IllegalStateException(this + " has no default language");
        }
        ScriptingSessionImpl existingSession = sessions.get(name);
        if (existingSession != null) {
            if (existingSession.is(name, language)) {
                return existingSession;
            } else {
                throw new IllegalArgumentException("Session " + name + " already present: " + existingSession);
            }
        }
        ScriptEngineManager scriptEngineManager = managerFactory.newManager(language);
        ScriptingSessionImpl newSession =
                ScriptingSessionImpl.create(name, language, serviceBinding, scriptEngineManager, service, this);
        sessions.put(name, newSession);
        return newSession;
    }

    @Override
    public boolean clearSession(String name) {
        return sessions.remove(name) != null;
    }

    @Override
    public Iterator<String> iterator() {
        return sessions.keySet().iterator();
    }

    @Override
    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    @Override
    public String toString() {
        return ToString.of(this, "default langauge", defaultLanguage, "sessions", sessions.size());
    }
}