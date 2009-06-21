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

package vanadis.extrt;

import org.osgi.framework.BundleContext;
import vanadis.blueprints.BundleSpecification;
import vanadis.blueprints.ModuleSpecification;
import vanadis.core.collections.Generic;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;
import vanadis.core.time.TimeSpan;
import vanadis.ext.Command;
import vanadis.ext.CommandExecution;
import vanadis.ext.GenericCommand;
import vanadis.osgi.*;
import vanadis.util.concurrent.OperationQueuer;
import vanadis.util.concurrent.ThreadedDispatch;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The control room of the vanadis extender module.  Instantiates a {@link SystemEventsImpl
 * system events handler}.
 */
class FrameworkExtender {

    private final SystemEvents asynchSystemEvents;

    private final SystemEventsImpl synchSystemEvents;

    private final Mediator<ModuleSpecification> moduleSpecificationMediator;

    private final Mediator<BundleSpecification> bundleSpecificationMediator;

    private final BundleMediator bundleMediator;

    private final AtomicBoolean closed = new AtomicBoolean();

    private final Context context;

    private final Collection<Registration<Command>> commandRegistrations = Generic.set();

    FrameworkExtender(BundleContext bundleContext, TimeSpan timeout) {
        this.context = Contexts.create(Not.nil(bundleContext, "bundleContext"));

        OperationQueuer queuer = new ThreadedDispatch
                (THREAD_NAME + "[" + this.context.getLocation() + "@" + this.context.getHome() + "]", timeout);
        synchSystemEvents = new SystemEventsImpl(bundleContext, this.context, queuer);

        this.asynchSystemEvents = queuer.createAsynch(synchSystemEvents, SystemEvents.class);
        synchSystemEvents.setAsynchYou(this.asynchSystemEvents);

        this.bundleMediator = this.context.createBundleMediator(this.asynchSystemEvents);

        this.moduleSpecificationMediator = this.context.createMediator
                (ModuleSpecification.class, null,
                 new ModuleSpecMediatorListener(this.asynchSystemEvents));
        this.bundleSpecificationMediator = this.context.createMediator
                (BundleSpecification.class, null,
                 new BundleSpecMediatorListener(this.asynchSystemEvents));

        cmd("exit-ext", "clean exit vanadis", new QuitExecution(this));
        cmd("reload", "reload bundles", new ReloadExecution(asynchSystemEvents));
        cmd("launch", "launch bundles", new LaunchExecution(asynchSystemEvents));
        cmd("install", "install bundles from maven repository", new InstallFromRepoExecution());
        cmd("list", "list managed objects [-v, {type,name}=foo]", new ListExecution());
        cmd("env", "describe environment", new EnvExecution());
        cmd("logfile", "list log file(s)", new LogFileExecution());
        cmd("unresolved", "list unresolved bundles", new UnresolvedExecution(synchSystemEvents.getBundles()));
    }

    Context getContext() {
        return context;
    }

    void close(boolean finalStop) {
        boolean wasClosed = closed.getAndSet(true);
        if (wasClosed) {
            return;
        }
        if (finalStop) {
            unregisterCommands();
        }
        bundleMediator.close();
        moduleSpecificationMediator.close();
        bundleSpecificationMediator.close();
        synchSystemEvents.close();
    }

    private void cmd(String name, String desc, CommandExecution exec) {
        commandRegistrations.add
                (context.register(new GenericCommand("v-" + name, desc, this.context, exec), Command.class));
    }

    private void unregisterCommands() {
        for (Registration<Command> reg : commandRegistrations) {
            reg.unregister();
        }
    }

    private static final String THREAD_NAME = "SystemEvents";

    @Override
    public String toString() {
        return ToString.of(this, asynchSystemEvents);
    }
}
