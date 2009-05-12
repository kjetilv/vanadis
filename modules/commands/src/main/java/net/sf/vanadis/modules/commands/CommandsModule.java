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
package net.sf.vanadis.modules.commands;

import net.sf.vanadis.ext.*;
import net.sf.vanadis.osgi.Context;

@Module(moduleType = "commands", launch = @AutoLaunch(name = "commands"))
public class CommandsModule extends AbstractContextAware {

    @Expose
    public Command[] getCommands() {
        Context context = context();
        return new Command[]{
                new GenericCommand("v-env", "describe environment", context, new EnvExecution()),
                new GenericCommand("v-logfile", "list log file(s)", context, new LogFileExecution()),
                new GenericCommand("v-list", "list managed objects [-v, {type,name}=foo]", context, 
                                   new ListExecution()),
        };
    }
}

