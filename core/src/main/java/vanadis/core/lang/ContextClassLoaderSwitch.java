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

package net.sf.vanadis.core.lang;

/**
 * A one-time switch to {@link #ContextClassLoaderSwitch(ClassLoader) set the thread context class loader},
 * and {@link #revert() revert} it later.
 */
public final class ContextClassLoaderSwitch {

    private final ClassLoader previous;

    private final Thread thread = Thread.currentThread();

    private boolean reverted;

    /**
     * Sets the argument class loader as the thread context class
     * loader.  Should be {@link #revert() reverted} later, in
     * the same thread.
     *
     * @param classLoader Thread context class loader to switch.
     */
    public ContextClassLoaderSwitch(ClassLoader classLoader) {
        ClassLoader previous = thread.getContextClassLoader();
        if (previous == classLoader) {
            this.previous = null;
        } else {
            this.previous = previous;
            thread.setContextClassLoader(classLoader);
        }
    }

    /**
     * Revert back to the thread context class loader in use before this switch
     * was created.  Must be called in the same thread.
     */
    public void revert() {
        if (Thread.currentThread() != thread) {
            throw new IllegalStateException(this + " cannot revert in a thread other than " + thread);
        }
        if (!reverted) {
            if (previous != null) {
                thread.setContextClassLoader(previous);
            }
            reverted = true;
        }
    }

    @Override
    public String toString() {
        return ToString.of(this, thread, "previous", previous, "current", thread.getContextClassLoader());
    }
}
