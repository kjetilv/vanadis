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
package net.sf.vanadis.log4jsetup;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import java.sql.Date;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;

final class VanadisLayout extends Layout {

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

    @Override
    public String format(LoggingEvent event) {
        return toString(event);
    }

    @Override
    public boolean ignoresThrowable() {
        return false;
    }

    @Override
    public void activateOptions() {
    }

    static String toString(LoggingEvent event) {
        String base = baseMessage(event);
        return withThrowableInformation(base, event);
    }

    static String withThrowableInformation(String base, LoggingEvent event) {
        ThrowableInformation information = event.getThrowableInformation();
        return information != null ? withThrowableInformation(base, information) : base;
    }

    static String baseMessage(LoggingEvent event) {
        String name = event.getLoggerName();
        return LOG_FORMAT.format(new Object[]{
                NAMES[event.getLevel().getSyslogEquivalent()],
                FORMAT.format(new Date(event.timeStamp)),
                name,
                event.getMessage(),
                event.getThreadName()
        });
    }

    static String withThrowableInformation(String base, ThrowableInformation information) {
        StringBuilder sb = new StringBuilder(base).append("[Exception: ");
        for (String line : information.getThrowableStrRep()) {
            sb.append(LN).append(line);
        }
        sb.append("]@").append(information.getThrowable().hashCode()).append(LN);
        return sb.toString();
    }
}
