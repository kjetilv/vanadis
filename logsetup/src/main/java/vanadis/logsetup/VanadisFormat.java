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
package vanadis.logsetup;

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import java.sql.Date;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;

final class VanadisFormat extends SimpleFormatter {

    static final SimpleDateFormat FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS");

    static final String LN = System.getProperty("line.separator");

    static final MessageFormat LOG_FORMAT = new MessageFormat("{0} {1} {2}: {3} [{4}]" + LN);

    static final String[] NAMES = new String[8];

    static {
        NAMES[0] = "FATAL";
        NAMES[3] = "ERROR";
        NAMES[4] = "WARN ";
        NAMES[6] = "INFO ";
        NAMES[7] = "DEBUG";
    }

    @SuppressWarnings({"SynchronizedMethod"})
    @Override
    public synchronized String format(LogRecord record) {
        return toString(record);
    }

    static String toString(LogRecord record) {
        String base = baseMessage(record);
        return withThrowableInformation(base, record);
    }

    static String withThrowableInformation(String base, LogRecord record) {
        Throwable thrown = record.getThrown();
        return thrown != null ? withThrowableInformation(base, thrown) : base;
    }

    static String baseMessage(LogRecord record) {
        String name = record.getLoggerName();
        return LOG_FORMAT.format(new Object[]
                { record.getLevel().getName(),
                  FORMAT.format(new Date(record.getMillis())),
                  name,
                  record.getMessage(),
                  record.getThreadID()
                });
    }

    static String withThrowableInformation(String base, Throwable throwable) {
        StringBuilder sb = new StringBuilder(base).append("[Exception: ");
        for (StackTraceElement line : throwable.getStackTrace()) {
            sb.append(LN).append(line);
        }
        sb.append("]@").append(throwable.hashCode()).append(LN);
        return sb.toString();
    }
}
