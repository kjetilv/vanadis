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
import vanadis.ext.AbstractCommandExecution;
import vanadis.osgi.Context;

final class EnvExecution extends AbstractCommandExecution {

    @Override
    public void exec(String command, String[] args, StringBuilder sb, Context context) {
        ln(sb.append("Location : ").append(context.getLocation().toLocationString()));
        ln(sb.append("Home     : ").append(context.getHome()));
        ln(sb.append("PID      : ").append(VM.pid()));
        ln(sb.append("VM       : ").append(VM.VERSION));
        ln(sb.append("cwd      : ").append(VM.CWD));
        ln(sb.append("tmp      : ").append(VM.TMP));
    }
}
