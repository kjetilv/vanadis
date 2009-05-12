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
package vanadis.services.scripting;

import vanadis.osgi.Reference;

public interface ScriptingSessions extends Iterable<String> {

    /**
     * Default language used when passing a null language to
     * {@link #newSession(String, String, vanadis.osgi.Reference, String)}.
     *
     * @return Default language
     */
    String getDefaultLanguage();

    ScriptingSession getSession(String name);

    ScriptingSession newSession(String name, String language, Reference<?> service, String serviceBinding);

    Iterable<String> sessionNames();

    boolean clearSession(String name);
}
