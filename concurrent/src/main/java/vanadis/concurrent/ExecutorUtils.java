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

package vanadis.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vanadis.core.time.InterruptedRuntimeException;
import vanadis.core.time.TimeSpan;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ExecutorUtils {

    private static final List<Runnable> NO_TASKS = Collections.emptyList();

    private static final Logger log = LoggerFactory.getLogger(ExecutorUtils.class);

    public static List<Runnable> terminate(ExecutorService service, TimeSpan timeout) {
        return terminate(null, service, timeout);
    }

    public static List<Runnable> terminate(Object host, ExecutorService service, TimeSpan timeout) {
        service.shutdown();
        if (service.isTerminated()) {
            return NO_TASKS;
        }
        try {
            timeout.awaitTermination(service, true);
        } catch (InterruptedRuntimeException ignore) {
            log.warn("Already interrupted: " + service);
        }
        if (service.isTerminated()) {
            return NO_TASKS;
        }
        List<Runnable> tasks = service.shutdownNow();
        if (host != null && !tasks.isEmpty()) {
            log.warn(host + " closed " + service + " with " + tasks.size() + " unfinished tasks");
        }
        return tasks;
    }
}
