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
package net.sf.vanadis.modules.lang.piji;

import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.ext.Expose;
import net.sf.vanadis.ext.Module;
import net.sf.vanadis.lang.piji.jsr223.PijiScriptEngineFactory;

import javax.script.ScriptEngineFactory;

@Module(moduleType = "piji")
public class PijiModule {

    private PijiScriptEngineFactory scriptEngineFactory;

    @Expose
    public ScriptEngineFactory getScriptEngineFactory() {
        if (scriptEngineFactory == null) {
            scriptEngineFactory = new PijiScriptEngineFactory();
        }
        return scriptEngineFactory;
    }

    @Override
    public String toString() {
        return ToString.of(this, scriptEngineFactory);
    }
}
