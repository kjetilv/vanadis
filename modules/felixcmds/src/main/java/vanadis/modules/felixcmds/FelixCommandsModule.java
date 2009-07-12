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
package vanadis.modules.felixcmds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vanadis.core.collections.Generic;
import vanadis.ext.*;
import vanadis.osgi.Registration;

import java.io.PrintStream;
import java.util.Map;

@Module(moduleType = "felix-commands", autolaunch = true)
public class FelixCommandsModule extends AbstractContextAware {

    private static final Logger log = LoggerFactory.getLogger(FelixCommandsModule.class);

    private final Map<Command, Registration<?>> registrations = Generic.map();

    @Inject(required = false)
    public void addCommand(Command command) {
        Registration<?> registration = context().register
                (new FelixCommand(command), org.ungoverned.osgi.service.shell.Command.class);
        registrations.put(command, registration);
    }

    @Retract
    public void removeCommand(Command command) {
        Registration<?> registration = registrations.remove(command);
        if (registration != null) {
            try {
                registration.unregister();
            } catch (Exception e) {
                log.warn(this + " failed to unregister " + registration + " wrapping " + command, e);
            }
        }
    }

    private static class FelixCommand implements org.ungoverned.osgi.service.shell.Command {

        private final Command command;

        FelixCommand(Command command) {
            this.command = command;
        }

        @Override
        public String getName() {
            return command.getName();
        }

        @Override
        public String getUsage() {
            return command.getUsage();
        }

        @Override
        public String getShortDescription() {
            return command.getShortDescription();
        }

        @Override
        public void execute(String txt, PrintStream out, PrintStream err) {
            command.execute(txt, out);
        }
    }
}