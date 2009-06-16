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

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import vanadis.core.collections.Pair;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;
import vanadis.core.text.Printer;
import vanadis.osgi.Context;
import vanadis.util.log.Log;
import vanadis.util.log.Logs;

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

    protected AbstractCommand(String name, String shortDescription,
                              Context context) {
        this(name, name, shortDescription, context, false);
    }

    protected AbstractCommand(String name, String usage, String desc,
                              Context context,
                              boolean commandIsEvent) {
        this.name = Not.nil(name, "name");
        this.usage = usage;
        this.commandIsEvent = commandIsEvent;
        this.shortDescription = desc == null ? name
                : desc.substring(0, 1).toUpperCase() + desc.substring(1);
        if (commandIsEvent) {
            this.context = Not.nil(context, "context");
            this.eventAdmin = context == null? null : context.getServiceProxy(EventAdmin.class);
        } else {
            this.context = context;
            this.eventAdmin = null;
        }
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
        Printer printer = new Printer(out)
                .terminateWithNewLine()
                .printStackTrace()
                .singleBlankLine()
                .autoFlush();
        try {
            if (commandIsEvent) {
                Event event = createEvent(command, args);
                eventAdmin.postEvent(event);
                printer.p("Sent event: ").p(event);
            } else {
                runCommand(command, args, printer);
            }
        } finally {
            printer.close();
        }
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

    private void runCommand(String command, String[] args, Printer printer) {
        try {
            execute(command, args, printer, context);
        } catch (Exception e) {
            handleError(command, args, printer, e);
        }
    }

    private void handleError(String command, String[] args, Printer printer, Exception e) {
        log.warn(getName() + ": Command " + command + Arrays.toString(args) + " failed: " + e, e);
        printer.p(getName()).p(" failed").cr();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        printer.printStackTrace(e);
        printer.p(new String(baos.toByteArray())).cr();
    }

    void execute(String command, String[] args, Printer printer, Context context) {
        printer.p("WARNING: ").p(getClass()).p
                (" is not an event command, and has no implementation of the execute method");
    }

    @Override
    public String toString() {
        return ToString.of(this, name, "event", commandIsEvent, "context", context);
    }
}
