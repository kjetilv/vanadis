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

package vanadis.ext;

import vanadis.core.lang.ToString;
import vanadis.osgi.Context;

/**
 * A generic, simple command that just needs a single {@link vanadis.ext.CommandExecution command execution}
 * implementation.
 */
public final class GenericCommand extends AbstractCommand {

    private final CommandExecution commandExecution;

    public GenericCommand(String name, String description, Context context, CommandExecution commandExecution) {
        super(name, description, context);
        this.commandExecution = commandExecution;
    }

    @Override
    protected void execute(String command, String[] args, StringBuilder sb, Context context) {
        commandExecution.exec(command, args, sb, context);
    }

    @Override
    public String toString() {
        return ToString.of(this, getName(), "execution", commandExecution);
    }
}
