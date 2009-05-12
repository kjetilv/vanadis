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

import vanadis.ext.CommandExecution;
import vanadis.osgi.Context;
import vanadis.osgi.Reference;
import vanadis.services.scripting.ScriptingSessions;

public class NewSessionExecution implements CommandExecution {

    private final ScriptingSessions sessions;

    public NewSessionExecution(ScriptingSessions sessions) {
        this.sessions = sessions;
    }

    @Override
    public void exec(String command, String[] args, StringBuilder sb, Context context) {
        String arg = args[2];
        sessions.newSession(args[0],
                            args.length > 1 ? args[1] : null,
                            args.length > 2 ? ref(arg, context) : null,
                            args.length > 3 ? args[3] : null);
    }

    private Reference<?> ref(String arg, Context context) {
        return Services.getReference(context, arg, null);
    }
}
