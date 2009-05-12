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
package net.sf.vanadis.ext;

import net.sf.vanadis.core.collections.Pair;
import net.sf.vanadis.core.io.Closeables;
import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.core.system.VM;
import net.sf.vanadis.osgi.Context;
import net.sf.vanadis.util.log.Log;
import net.sf.vanadis.util.log.Logs;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * Support class for {@link Command command} classes.
 */
public abstract class AbstractCommand implements Command {

    private static final Log log = Logs.get(AbstractCommand.class);

    private final String name;

    private final String usage;

    private final boolean commandIsEvent;

    private final String shortDescription;

    private final Context context;

    private final EventAdmin eventAdmin;

    AbstractCommand(String name, String shortDescription,
                    Context context) {
        this(name, name, shortDescription, context);
    }

    private AbstractCommand(String name, String usage, String shortDescription,
                            Context context) {
        this(name, usage, shortDescription, context, false);
    }

    protected AbstractCommand(String name, String shortDescription,
                              Context context,
                              boolean commandIsEvent) {
        this(name, name, shortDescription, context, commandIsEvent);
    }

    private AbstractCommand(String name, String usage, String shortDescription,
                            Context context,
                            boolean commandIsEvent) {
        this.name = name;
        this.usage = usage;
        this.commandIsEvent = commandIsEvent;
        this.shortDescription =
                "Vanadis: " +
                        shortDescription.substring(0, 1).toUpperCase() +
                        shortDescription.substring(1);
        this.context = context;
        this.eventAdmin = context.getServiceProxy(EventAdmin.class);
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String getUsage() {
        return usage;
    }

    @Override
    public final String getShortDescription() {
        return shortDescription;
    }

    @Override
    public final void execute(String fullCommand, PrintStream out) {
        Pair<String, String[]> pair = Parse.args(fullCommand);
        String command = pair.getOne();
        String[] args = pair.getTwo();
        StringBuilder sb = new StringBuilder();
        if (commandIsEvent) {
            Event event = createEvent(command, args);
            eventAdmin.postEvent(event);
            sb.append("Sent event: ").append(event);
        } else {
            runCommand(command, args, sb);
        }
        out.println(sb.toString().trim());
    }

    /**
     * May be overridden.
     *
     * @param command Command
     * @param args    Args
     * @return Event
     */
    protected Event createEvent(String command, String[] args) {
        return CommandEvents.newEvent(command, args);
    }

    private void runCommand(String command, String[] args, StringBuilder string) {
        try {
            execute(command, args, string, context);
        } catch (Exception e) {
            handleError(command, args, string, e);
        }
    }

    private void handleError(String command, String[] args, StringBuilder string, Exception e) {
        log.warn(getName() + ": Command " + command + Arrays.toString(args) + " failed: " + e, e);
        string.append(getName()).append(" failed").append(VM.LN);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        try {
            e.printStackTrace(ps);
            string.append(new String(baos.toByteArray())).append(VM.LN);
        } finally {
            Closeables.close(ps);
        }
    }

    void execute(String command, String[] args, StringBuilder sb, Context context) {
        sb.append("WARNING: ").append(getClass()).append
                (" is not an event command, and has no implementation of the execute method");
    }

    @Override
    public String toString() {
        return ToString.of(this, name, "event", commandIsEvent, "context", context);
    }
}
