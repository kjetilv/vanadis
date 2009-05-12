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

package net.sf.vanadis.core.system;

import java.io.PrintStream;

/**
 * Custom exception that suggests that the VM should exit!  Use it
 * instead of calling System.exit(), and allow an outside, top-level
 * handler to actually deal with the actual exit - or veto it. Also
 * helps the compiler get your control flow, avoiding rubbish like:
 *
 * <PRE>
 * System.exit(-1);
 * return; // Make compiler happy
 * </PRE>
 */
public final class SystemExitError extends Error {

    private static final long serialVersionUID = 1996928781698619057L;

    private final int exitCode;

    /**
     * Exit with a message, exit code 1.
     *
     * @param message Message
     */
    public SystemExitError(String message) {
        this(message, 1, null);
    }

    /**
     * Exit with an exit code and a message.  Exit code 0 denotes {@link #isNormalExit() normal exit}.
     *
     * @param message  Message
     * @param exitCode Exit code
     */
    public SystemExitError(String message, int exitCode) {
        this(message, exitCode, null);
    }

    /**
     * Exit with a message and a cause, exit code 1.
     *
     * @param message Message
     * @param cause   Cause
     */
    public SystemExitError(String message, Throwable cause) {
        this(message, 1, cause);
    }

    /**
     * Exit with an exit code, a message and a cause.  Exit code 0 denotes {@link #isNormalExit() normal exit}.
     *
     * @param message  Message
     * @param exitCode Exit code
     * @param cause    Cause
     */
    public SystemExitError(String message, int exitCode, Throwable cause) {
        super(message, cause);
        this.exitCode = exitCode;
    }

    /**
     * Exit code.
     *
     * @return Exit code
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * If {@link #getExitCode() exit code} is 0, it is a normal exit.
     *
     * @return True iff exit code is 0
     */
    public boolean isNormalExit() {
        return exitCode == 0;
    }

    /**
     * Perform the actual exit.  Calls {@link #exit(java.io.PrintStream)} on {@link System#err stderr}.
     */
    public void exit() {
        //noinspection RedundantTypeArguments
        exit(System.err);
    }

    /**
     * Perform the actual exit.  If stream != null, write
     * {@link #getMessage() the message} and (non-null) {@link #getCause() cause}
     * to it, before invoking {@link System#exit(int) exit} with the
     * {@link #getExitCode() exit code}.
     *
     * @param stream Print stream.
     */
    public void exit(PrintStream stream) {
        if (stream != null) {
            stream.println(getMessage());
            Throwable cause = getCause();
            if (cause != null) {
                cause.printStackTrace(stream);
            }
        }
        System.exit(exitCode);
    }
}
