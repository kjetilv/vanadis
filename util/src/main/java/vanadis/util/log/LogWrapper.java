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

package vanadis.util.log;

import vanadis.core.lang.ToString;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.PrintStream;

final class LogWrapper implements Log {

    private final Logger log;

    private static final PrintStream ERR = System.err;

    LogWrapper(Class<?> clazz) {
        this(clazz.getName());
    }

    LogWrapper(String logger) {
        log = LogManager.getLogger(logger);
    }

    @Override
    public void info(String msg) {
        sLog(log, Level.INFO, msg);
    }

    @Override
    public void info(String msg, Throwable throwable) {
        sLog(log, Level.INFO, msg, throwable);
    }

    @Override
    public void debug(String msg) {
        sLog(log, Level.DEBUG, msg);
    }

    @Override
    public void debug(String msg, Throwable throwable) {
        sLog(log, Level.DEBUG, msg, throwable);
    }

    @Override
    public void warn(String msg) {
        sLog(log, Level.WARN, msg);
    }

    @Override
    public void warn(String msg, Throwable throwable) {
        sLog(log, Level.WARN, msg, throwable);
    }

    @Override
    public void error(String msg) {
        sLog(log, Level.ERROR, msg);
    }

    @Override
    public void error(String msg, Throwable throwable) {
        sLog(log, Level.ERROR, msg, throwable);
    }

    @Override
    public void fatal(String msg) {
        sLog(log, Level.FATAL, msg);
    }

    @Override
    public void fatal(String msg, Throwable throwable) {
        sLog(log, Level.FATAL, msg, throwable);
    }

    @Override
    public void log(String level, String msg) {
        sLog(log, Level.toLevel(level, Level.INFO), msg, null);
    }

    @Override
    public void log(String level, String msg, Throwable throwable) {
        sLog(log, Level.toLevel(level, Level.INFO), msg, throwable);
    }

    @Override
    public boolean isDebug() {
        Logger logger = log;
        Level lvl = Level.DEBUG;
        return does(logger, lvl);
    }

    @Override
    public boolean isInfo() {
        return does(log, Level.INFO);
    }

    @Override
    public boolean isWarn() {
        return does(log, Level.INFO);
    }

    @Override
    public boolean isError() {
        return does(log, Level.ERROR);
    }

    @Override
    public boolean isFatal() {
        return does(log, Level.FATAL);
    }

    @Override
    public String toString() {
        return ToString.of(this, log.getName());
    }

    private static void sLog(Logger logger, Level lvl, String msg, Throwable throwable) {
        try {
            logger.log(lvl, msg, throwable);
        } catch (Throwable e) {
            err(e, lvl, msg, throwable);
        }
    }

    private static void sLog(Logger logger, Level lvl, String msg) {
        try {
            logger.log(lvl, msg);
        } catch (Throwable e) {
            err(e, lvl, msg);
        }
    }

    private static boolean does(Logger logger, Level lvl) {
        try {
            return logger.isEnabledFor(lvl);
        } catch (Throwable e) {
            err(e, Level.FATAL, "Unable to check for level " + lvl, e);
            return true;
        }
    }

    private static void err(Throwable e, Level lvl, String msg) {
        err(e, lvl, msg, null);
    }

    private static void err(Throwable e, Level lvl, String msg, Throwable throwable) {
        ERR.println(lvl + ": " + msg + " (" + e + ")");
        if (throwable != null) {
            if (throwable instanceof NoClassDefFoundError) {
                ERR.println("Unloading issue: " + throwable);
            } else {
                throwable.printStackTrace(ERR);
            }
        }
    }
}
