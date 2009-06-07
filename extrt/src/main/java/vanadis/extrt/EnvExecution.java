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

package vanadis.extrt;

import vanadis.core.system.VM;
import vanadis.ext.CommandExecution;
import vanadis.ext.Printer;
import vanadis.osgi.Context;

final class EnvExecution implements CommandExecution {

    @Override
    public void exec(String command, String[] args, Printer p, Context context) {
        p.p("Location : ").p(context.getLocation().toLocationString()).cr();
        p.p("Home     : ").p(context.getHome()).cr();
        p.p("PID      : ").p(VM.pid()).cr();
        p.p("VM       : ").p(VM.VERSION).cr();
        p.p("cwd      : ").p(VM.CWD).cr();
        p.p("tmp      : ").p(VM.TMP).cr();
    }
}
