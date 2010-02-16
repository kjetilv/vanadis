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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vanadis.common.text.Printer;
import vanadis.ext.CommandExecution;
import vanadis.osgi.Context;
import vanadis.services.scripting.ScriptingSession;

import java.util.Arrays;

class ScriptExecution implements CommandExecution {

    private final ScriptingSessionsImpl scriptingSessions;

    ScriptExecution(ScriptingSessionsImpl scriptingSessions) {
        this.scriptingSessions = scriptingSessions;
    }

    @Override
    public void exec(String command, String[] args, Printer p, Context context) {
        Object object = performEvaluation(arg(args, 0, "session name"), concat(args, 1), args);
        p.p(object);
    }

    private Object performEvaluation(String name, String script, String[] args) {
        try {
            ScriptingSession session = scriptingSessions.getSession(name);
            if (session != null) {
                return session.eval(script);
            }
        } catch (Exception e) {
            log.error(this + " failed to eval script, event args: " + Arrays.toString(args));
            return e;
        }
        return "No such session: " + name;
    }

    private static final Logger log = LoggerFactory.getLogger(ScriptExecution.class);

    private static String concat(String[] args, int index) {
        StringBuilder sb = new StringBuilder();
        for (int i = index; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        return sb.toString();
    }

    private static String arg(String[] args, int i, String name) {
        if (i < args.length) {
            return args[i];
        } else {
            throw new IllegalArgumentException("Argument " + i + " missing: " + name);
        }
    }

}
