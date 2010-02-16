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

package vanadis.common.io;

import vanadis.core.collections.Generic;
import vanadis.core.lang.ToString;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Help with closeables, allowing you to ignore exceptions most of the time,
 * while handling any errors that really <em>could</em> occur.
 */
public final class Closeables {

    /**
     * A close error.
     */
    public static final class CloseIssue {

        private final Closeable closeable;

        private final IOException closeException;

        private CloseIssue(Closeable closeable,
                           IOException closeException) {
            this.closeable = closeable;
            this.closeException = closeException;
        }

        /**
         * The closeable that failed.
         *
         * @return Closeable
         */
        public Closeable getCloseable() {
            return closeable;
        }

        /**
         * The IOException that indicated the failure.
         *
         * @return IOException
         */
        public IOException getIOException() {
            return closeException;
        }

        @Override
        public String toString() {
            return ToString.of(this, closeable, closeException);
        }
    }

    /**
     * Close all closeables, and fail if any of them failed to close.
     *
     * @param closeables Closeables
     * @throws IORuntimeException Wrapping the first IOException that occurred
     */
    public static void closeOrFail(Closeable... closeables) {
        List<CloseIssue> list = close(closeables);
        if (list != null && !list.isEmpty()) {
            CloseIssue error1 = list.get(0);
            if (list.size() == 1) {
                throw new IORuntimeException
                    ("Failed to close " + error1.getCloseable(), error1.getIOException());
            } else {
                throw new IORuntimeException
                    ("Failed to close " + list.size() + " closeables: " + list, error1.closeException);
            }
        }
    }

    public static List<CloseIssue> close(Iterable<? extends Closeable> closeables) {
        List<CloseIssue> issues = null;
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    if (issues == null) {
                        issues = Generic.list();
                    }
                    issues.add(new CloseIssue(closeable, e));
                }
                if (closeable instanceof PrintStream) {
                    PrintStream printStream = (PrintStream)closeable;
                    boolean error = printStream.checkError();
                    if (error) {
                        if (issues == null) {
                            issues = Generic.list();
                        }
                        issues.add(new CloseIssue(printStream, null));
                    }
                }
            }
        }
        return issues;
    }

    public static List<CloseIssue> close(Closeable closeable) {
        return close(Collections.singleton(closeable));
    }

    public static List<CloseIssue> close(Closeable... closeables) {
        return close(Arrays.asList(closeables));
    }

    private Closeables() {}
}
