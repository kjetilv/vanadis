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
package net.sf.vanadis.services.scripting;

import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.core.system.VM;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class AbstractScriptEngineFactory implements ScriptEngineFactory {

    private final Map<String, String> values;

    private final String name;

    protected AbstractScriptEngineFactory(String name, String version) {
        this(name, version, name, name, version, name);
    }

    private AbstractScriptEngineFactory(String engineName,
                                        String engineVersion,
                                        String fileName,
                                        String languageName,
                                        String languageVersion,
                                        String name) {
        this.name = name;
        this.values = values(engineName, engineVersion, fileName, languageName, languageVersion, name);
    }

    private static Map<String, String> values(String engineName,
                                              String engineVersion,
                                              String fileName,
                                              String languageName,
                                              String languageVersion,
                                              String name) {
        return Generic.seal(Generic.map(ScriptEngine.ENGINE, engineName,
                                        ScriptEngine.ENGINE_VERSION, engineVersion,
                                        ScriptEngine.FILENAME, fileName,
                                        ScriptEngine.LANGUAGE, languageName,
                                        ScriptEngine.LANGUAGE_VERSION, languageVersion,
                                        ScriptEngine.NAME, name));
    }

    private String par(String par) {
        return String.valueOf(values.get(par));
    }

    @Override
    public final String getEngineName() {
        return par(ScriptEngine.ENGINE);
    }

    @Override
    public final String getEngineVersion() {
        return par(ScriptEngine.ENGINE_VERSION);
    }

    @Override
    public final List<String> getExtensions() {
        return Collections.singletonList(par(ScriptEngine.FILENAME));
    }

    @Override
    public final List<String> getMimeTypes() {
        return Collections.singletonList("text/" + name);
    }

    @Override
    public final List<String> getNames() {
        return Collections.singletonList(par(ScriptEngine.NAME));
    }

    @Override
    public final String getLanguageName() {
        return par(ScriptEngine.LANGUAGE);
    }

    @Override
    public final String getLanguageVersion() {
        return par(ScriptEngine.LANGUAGE_VERSION);
    }

    @Override
    public final Object getParameter(String par) {
        return par(par);
    }

    @Override
    public final String getMethodCallSyntax(String object, String method, String... args) {
        StringBuilder sb = new StringBuilder("(").append
                (object).append(" ").append(method);
        for (String arg : args) {
            sb.append(" ").append(arg);
        }
        return sb.append(")").toString();
    }

    @Override
    public final String getOutputStatement(String s) {
        return new StringBuilder().append("(print ").append(s).append(")").toString();
    }

    @Override
    public final String getProgram(String... statements) {
        StringBuilder sb = new StringBuilder("(begin ");
        for (String statement : statements) {
            sb.append(VM.LN).append("  ").append(statement);
        }
        return sb.append(")").toString();
    }

    @Override
    public String toString() {
        return ToString.of(this, name);
    }
}
