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

import vanadis.core.lang.ToString;
import vanadis.common.time.TimeSpan;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractRetrier<T> {

    private final ScheduledExecutorService service;

    private final TimeSpan retry;

    private final T target;

    private static final TimeSpan DEFAULT = TimeSpan.HALF_MINUTE;

    private final AtomicLong failuresInARow = new AtomicLong();

    private final AtomicLong failures = new AtomicLong();

    private final AtomicLong successFulCalls = new AtomicLong();

    protected AbstractRetrier(TimeSpan retry, ScheduledExecutorService service, T target) {
        this.target = target;
        this.retry = orDefault(retry);
        this.service = service;
    }

    protected static TimeSpan orDefault(TimeSpan timeSpan) {
        return timeSpan == null ? DEFAULT : timeSpan;
    }

    protected final ScheduledExecutorService getService() {
        return service;
    }

    protected final TimeSpan getRetry() {
        return retry;
    }

    protected final T getTarget() {
        return target;
    }

    protected final void submit(Callable<Boolean> baseAction) {
        if (service != null) {
            service.submit(new AuditingAction(baseAction));
        } else {
            try {
                new AuditingAction(baseAction).call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final class AuditingAction implements Callable<Boolean> {

        private final Callable<Boolean> baseAction;

        private AuditingAction(Callable<Boolean> baseAction) {
            this.baseAction = baseAction;
        }

        @Override
        public Boolean call() throws Exception {
            try {
                try {
                    return baseAction.call();
                } finally {
                    successFulCalls.incrementAndGet();
                    failuresInARow.set(0);
                }
            } catch (Exception e) {
                failures.incrementAndGet();
                throw e;
            }
        }
    }

    @Override
    public String toString() {
        return ToString.of
                (this, "target", target, "s", successFulCalls, "f", failures, "f/r", failuresInARow);
    }
}
