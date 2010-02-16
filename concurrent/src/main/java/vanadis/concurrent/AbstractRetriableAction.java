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
import vanadis.core.lang.ToString;
import vanadis.common.time.TimeSpan;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;

public abstract class AbstractRetriableAction<T> implements Callable<Boolean> {

    private static final Logger log = LoggerFactory.getLogger(AbstractRetriableAction.class);

    private final ScheduledExecutorService service;

    private final TimeSpan retry;

    private final T target;

    protected AbstractRetriableAction(TimeSpan retry, ScheduledExecutorService service, T target) {
        this.retry = retry;
        this.service = service;
        this.target = target;
    }

    @Override
    public final Boolean call() throws Exception {
        try {
            makeCall();
        } catch (Exception e) {
            logException(e);
            service.schedule(this, retry.time(), retry.unit());
            return false;
        }
        return true;
    }

    private void logException(Exception e) {
        StringBuilder msg = new StringBuilder(this.toString()).append(" failed, re-scheduling...:");
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            msg.append(" -> ").append(cause);
        }
        log.info(msg.toString());
    }

    protected abstract void makeCall();

    protected final T getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return ToString.of(this, "target", getTarget(), "retry", retry);
    }
}
