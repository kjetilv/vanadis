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

package net.sf.vanadis.core.time;

import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;

final class JustWaiting {

    private static void interrupted(Object object, Time waitStart, TimeSpan timeSpan, boolean failInterrupted,
                                    InterruptedException e) {
        Thread.currentThread().interrupt();
        if (failInterrupted) {
            throw new InterruptedRuntimeException
                    ("Interrupted after " + TimeSpan.since(waitStart).toHumanFriendlyString() +
                            " while waiting for " + timeSpan.toHumanFriendlyString() +
                            " on " + object, e);
        }
    }

    private static void alreadyInterrupted(Object object, Time waitStart, TimeSpan timeSpan) {
        Thread thread = Thread.currentThread();
        throw new InterruptedRuntimeException("Thread " + thread.getName() + " (id " + thread.getId() + ")" +
                " already interrupted at " + waitStart +
                ", aborting wait for " + timeSpan.toHumanFriendlyString() +
                " on " + object);
    }

    static boolean onThat(Object object, TimeSpan timeSpan, boolean failInterrupted) {
        if (checkAlreadyInterrupted(object, timeSpan, failInterrupted)) {
            return false;
        }
        Time startTime = Time.mark();
        try {
            if (timeSpan.isForever()) {
                object.wait();
            } else {
                object.wait(timeSpan.msTime());
            }
        } catch (InterruptedException e) {
            interrupted(object, startTime, timeSpan, failInterrupted, e);
            return false;
        }
        return true;
    }

    static boolean forThat(TimeSpan timeSpan, Condition condition, boolean failInterrupted) {
        if (checkAlreadyInterrupted(condition, timeSpan, failInterrupted)) {
            return false;
        }
        Time startTime = Time.mark();
        try {
            if (timeSpan.isForever()) {
                condition.await();
            } else {
                condition.await(timeSpan.time(), timeSpan.unit());
            }
            return true;
        } catch (InterruptedException e) {
            interrupted(condition, startTime, timeSpan, failInterrupted, e);
            return false;
        }
    }

    static boolean beforeIWake(TimeSpan timeSpan, boolean failInterrupted) {
        if (checkAlreadyInterrupted(null, timeSpan, failInterrupted)) {
            return false;
        }
        checkForever("Cannot sleep forever", timeSpan);
        Time startTime = Time.mark();
        try {
            Thread.sleep(timeSpan.msTime());
            return true;
        } catch (InterruptedException e) {
            interrupted(null, startTime, timeSpan, failInterrupted, e);
            return false;
        }
    }

    private static boolean checkAlreadyInterrupted(Object target, TimeSpan waitTime, boolean failInterrupted) {
        boolean interrupted = Thread.currentThread().isInterrupted();
        if (interrupted) {
            if (failInterrupted) {
                alreadyInterrupted(target, Time.mark(), waitTime);
            } else {
                return true;
            }
        }
        return false;
    }

    static <T> T forThe(Future<T> future, TimeSpan timeSpan)
            throws TimeoutException, ExecutionException {
        if (checkAlreadyInterrupted(future, timeSpan, true)) {
            return null;
        }
        Time startTime = Time.mark();
        try {
            return doWait(future, timeSpan);
        } catch (InterruptedException e) {
            interrupted(future, startTime, timeSpan, true, e);
            return null;
        }
    }

    private static <T> T doWait(Future<T> future, TimeSpan timeSpan)
            throws InterruptedException, ExecutionException, TimeoutException {
        return timeSpan.isForever()
                ? future.get()
                : future.get(timeSpan.time(), timeSpan.unit());
    }

    static <T> T nervouslyForThe(Future<T> future, TimeSpan timeSpan, String failMsg, Object... args) {
        if (checkAlreadyInterrupted(future, timeSpan, true)) {
            return null;
        }
        Time startTime = Time.mark();
        try {
            return doWait(future, timeSpan);
        } catch (InterruptedException e) {
            interrupted(future, startTime, timeSpan, true, e);
            return null;
        } catch (ExecutionException e) {
            throw new ExecutionRuntimeException(MessageFormat.format(failMsg, args), e);
        } catch (TimeoutException e) {
            throw new TimeoutRuntimeException(MessageFormat.format(failMsg, args), timeSpan, e);
        }
    }

    static boolean forThe(ExecutorService service, TimeSpan timeSpan,
                          boolean failInterrupted) {
        checkForever("Cannot wait forever for " + service, timeSpan);
        if (checkAlreadyInterrupted(service, timeSpan, failInterrupted)) {
            return false;
        }
        Time startTime = Time.mark();
        try {
            return service.awaitTermination(timeSpan.time(), timeSpan.unit());
        } catch (InterruptedException e) {
            interrupted(service, startTime, timeSpan, failInterrupted, e);
            return false;
        }
    }

    private static void checkForever(String msg, TimeSpan timeSpan) {
        if (timeSpan.isForever()) {
            throw new IllegalArgumentException(msg);
        }
    }

    private JustWaiting() {
    }
}
