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

package vanadis.modules.log;

import vanadis.core.collections.Generic;
import vanadis.core.lang.ToString;
import vanadis.ext.AutoLaunch;
import vanadis.ext.Expose;
import vanadis.ext.Module;
import vanadis.util.log.Log;
import vanadis.util.log.Logs;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

import java.util.Map;

@Module(moduleType = "logservice", launch = @AutoLaunch(name = "logservice"))
@Expose(ranking = Integer.MIN_VALUE, exposedType = LogService.class)
public class SimpleLogService implements LogService {

    private static final Log log = Logs.get(SimpleLogService.class);

    private static final Map<Integer, String> LEVELS =
            Generic.map(LogService.LOG_DEBUG, "DEBUG",
                        LogService.LOG_INFO, "INFO",
                        LogService.LOG_WARNING, "WARN",
                        LogService.LOG_ERROR, "ERROR");

    @Override
    public void log(int level, String msg) {
        log.log(LEVELS.get(level), msg, null);
    }

    @Override
    public void log(int level, String msg, Throwable throwable) {
        log.log(LEVELS.get(level), msg, throwable);
    }

    @Override
    public void log(ServiceReference serviceReference, int level, String msg) {
        log.log(LEVELS.get(level), serviceReference + ": " + msg, null);
    }

    @Override
    public void log(ServiceReference serviceReference, int level, String msg, Throwable throwable) {
        log.log(LEVELS.get(level), serviceReference + ": " + msg, throwable);
    }

    @Override
    public String toString() {
        return ToString.of(this);
    }
}
