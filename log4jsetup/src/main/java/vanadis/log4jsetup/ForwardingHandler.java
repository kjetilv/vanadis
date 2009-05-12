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
package vanadis.log4jsetup;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

class ForwardingHandler extends Handler {

    private static final Level[] LEVELS = new Level[]{
            null, // 0
            null, // 1
            null, // 2
            Level.DEBUG, // 3
            Level.DEBUG, // 4
            Level.DEBUG, // 5
            null, // 6
            null, // 7
            Level.INFO, // 8
            Level.WARN, // 9
            Level.ERROR,// 10
    };

    static Level log4JLevel(LogRecord logRecord) {
        return LEVELS[logRecord.getLevel().intValue() / 100];
    }

    @Override
    public void publish(LogRecord javaUtilLogRecord) {
        Logger logger = LogManager.getLogger(javaUtilLogRecord.getLoggerName());
        logger.log(log4JLevel(javaUtilLogRecord),
                   javaUtilLogRecord.getMessage(),
                   javaUtilLogRecord.getThrown());
    }

    @Override
    public void flush() { }

    @Override
    public void close() { }
}
