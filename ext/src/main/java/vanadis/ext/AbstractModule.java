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

import vanadis.core.lang.EntryPoint;
import vanadis.core.lang.ToString;
import vanadis.osgi.Context;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A convenience support class for the {@link vanadis.ext.ManagedLifecycle managed life cycle}.
 */
public abstract class AbstractModule implements ManagedLifecycle {

    private final AtomicReference<ModuleSystemCallback> systemCallback = new AtomicReference<ModuleSystemCallback>();

    private final AtomicReference<Context> context = new AtomicReference<Context>();

    /**
     * Implement {@link vanadis.ext.ContextAware} to have this method invoked.
     * The context is available as {@link #context()} and {@link #requiredContext()}.
     *
     * @param context Context
     */
    @EntryPoint("Inherited if subclass implements ContextAware")
    public final void setContext(Context context) {
        set(this.context, context);
    }

    /**
     * Implement {@link ModuleSystemAware} to have this method invoked.  Afterwards,
     * invoke {@link #fail(Throwable, Object[])} to push a callback.
     *
     * @param systemCallback System callback
     */
    @EntryPoint("Inherited if subclass imeplements ModuleSystemAware")
    public final void setManagedObjectCallback(ModuleSystemCallback systemCallback) {
        set(this.systemCallback, systemCallback);
    }

    @Override
    public void initialized() {
    }

    @Override
    public void dependenciesResolved() {
    }

    @Override
    public void servicesExposed() {
    }

    @Override
    public void activate() {
    }

    @Override
    public void dependenciesLost() {
    }

    @Override
    public void closed() {
    }

    @Override
    public void configured() {
    }

    /**
     * Get current context.
     *
     * @return Current context
     */
    protected Context context() {
        return context.get();
    }

    /**
     * Get current context.  Fail if not set - ie. if requested prematurely.
     *
     * @return Current context, if set
     * @throws IllegalStateException If not set
     */
    protected Context requiredContext() {
        Context required = context();
        if (required == null) {
            throw new IllegalStateException(this + " has not yet received a context");
        }
        return required;
    }

    /**
     * <P>Emergency brake: Reach up and pull this wire to signal a failure and request a stop.</P>
     *
     * @param cause Cause for failure
     * @param msg   Message to be logged
     */
    @EntryPoint
    protected void fail(Throwable cause, Object... msg) {
        if (this instanceof ModuleSystemAware) {
            if (systemCallback.get() == null) {
                throw new IllegalStateException
                        (this + " has no callback, cannot report error: " +
                                Arrays.toString(msg), cause);
            }
            systemCallback.get().fail(cause, msg);
        } else {
            throw new IllegalStateException(this + " does not implement " +
                    ModuleSystemAware.class + ", cannot report error: " +
                    Arrays.toString(msg), cause);
        }
    }

    private <T> void set(AtomicReference<T> ref, T t) {
        boolean wasNull = ref.compareAndSet(null, t);
        if (!wasNull) {
            throw new IllegalStateException(this + " already had " + ref + ", rejecting " + t);
        }
    }

    @Override
    public String toString() {
        return ToString.of(this, "hash", System.identityHashCode(this),
                           "context", context,
                           "callback", systemCallback != null);
    }
}