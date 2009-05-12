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

package vanadis.core.time;

import vanadis.core.lang.ToString;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;

public final class Deadline implements Serializable, Waiting {

    private static final long serialVersionUID = 7424124122423006289L;

    private final Time start;

    private final TimeSpan deadlineTime;

    public Deadline(TimeSpan deadlineTime) {
        this(Time.mark(), deadlineTime);
    }

    public Deadline(Time start, TimeSpan deadlineTime) {
        this.start = start;
        this.deadlineTime = deadlineTime;
    }

    public boolean hasTimeLeft() {
        return hasTimeLeft(null);
    }

    /**
     * For testing purposes.
     *
     * @param suggestedTime Time suggsted by test
     * @return True iff time left
     */
    public boolean hasTimeLeft(Time suggestedTime) {
        return timePassed(suggestedTime).isShorterThan(deadlineTime);
    }

    public boolean hasExpired() {
        return isExceededAt(null);
    }

    /**
     * For testing purposes.
     *
     * @param suggestedTime Time suggsted by test
     * @return True iff exceeded
     */
    public boolean isExceededAt(Time suggestedTime) {
        return timePassed(suggestedTime).isLongerThan(deadlineTime);
    }

    public TimeSpan getSleepTime(TimeSpan sleepTime) {
        return getSleepTime(sleepTime, null);
    }

    public TimeSpan getSleepTime(TimeSpan maxSleepTime, TimeSpan timePassed) {
        TimeSpan remainingSleepTime = maxSleepTime.remainingAfter(timePassed);
        TimeSpan timeToExpiry = timeLeft();
        return TimeSpan.shortest(remainingSleepTime, timeToExpiry);
    }

    public TimeSpan timeLeft() {
        return timeLeftAt(null);
    }

    @Override
    public boolean waitFor(Condition condition) {
        return timeLeft().waitFor(condition);
    }

    @Override
    public boolean waitFor(Condition condition, boolean failOnInterrupt) {
        return timeLeft().waitFor(condition, failOnInterrupt);
    }

    @Override
    public boolean waitOn(Object lock) {
        return timeLeft().waitOn(lock);
    }

    @Override
    public boolean waitOn(Object lock, boolean failOnInterrupt) {
        return timeLeft().waitOn(lock, failOnInterrupt);
    }

    @Override
    public <T> T waitForUnchecked(Future<T> future) {
        return timeLeft().waitForUnchecked(future);
    }

    @Override
    public <T> T waitFor(Future<T> future)
            throws ExecutionException, TimeoutException, InterruptedException {
        return timeLeft().waitFor(future);
    }

    @Override
    public boolean sleep() {
        return timeLeft().sleep();
    }

    public TimeSpan timeLeftAt(Time time) {
        TimeSpan timePassed = timePassed(time);
        return deadlineTime.remainingAfter(timePassed);
    }

    public <T> T tryEvery(TimeSpan interval, Callable<T> callable) {
        return retry(interval, null, null, null, null, callable, false);
    }

    public <T> T tryEvery(TimeSpan interval, Object lock, Callable<T> callable) {
        return retry(interval, null, null, lock, null, callable, false);
    }

    public <T> T tryEvery(TimeSpan interval, Condition condition, Callable<T> callable) {
        return retry(interval, null, null, null, condition, callable, false);
    }

    public <T> T tryForNullEvery(TimeSpan interval, Callable<T> callable) {
        return retry(interval, null, null, null, null, callable, true);
    }

    public <T> T tryForNullEvery(TimeSpan interval, Object lock, Callable<T> callable) {
        return retry(interval, null, null, lock, null, callable, true);
    }

    public <T> T tryForNullEvery(TimeSpan interval, Condition condition, Callable<T> callable) {
        return retry(interval, null, null, null, condition, callable, true);
    }

    public <T> T tryEvery(TimeSpan interval, double factor, TimeSpan maxInterval, Callable<T> callable) {
        return retry(interval, factor, maxInterval, null, null, callable, false);
    }

    public <T> T tryEvery(TimeSpan interval, double factor, TimeSpan maxInterval, Object lock, Callable<T> callable) {
        return retry(interval, factor, maxInterval, lock, null, callable, false);
    }

    public <T> T tryEvery(TimeSpan interval, double factor, TimeSpan maxInterval, Condition condition, Callable<T> callable) {
        return retry(interval, factor, maxInterval, null, condition, callable, false);
    }

    public <T> T tryForNullEvery(TimeSpan interval, double factor, TimeSpan maxInterval, Callable<T> callable) {
        return retry(interval, factor, maxInterval, null, null, callable, true);
    }

    public <T> T tryForNullEvery(TimeSpan interval, double factor, TimeSpan maxInterval, Object lock, Callable<T> callable) {
        return retry(interval, factor, maxInterval, lock, null, callable, true);
    }

    public <T> T tryForNullEvery(TimeSpan interval, double factor, TimeSpan maxInterval, Condition condition, Callable<T> callable) {
        return retry(interval, factor, maxInterval, null, condition, callable, true);
    }

    private <T> T retry(TimeSpan interval, Double factor, TimeSpan maxInterval,
                        Object lock, Condition condition, Callable<T> callable, boolean forNull) {
        T t = null;
        TimeSpan waitTime = interval;
        while (hasTimeLeft()) {
            Time startTime = Time.mark();
            t = call(callable);
            if ((t == null) == forNull) {
                return t;
            }
            TimeSpan timePassed = TimeSpan.since(startTime);
            TimeSpan sleepTime = getSleepTime(waitTime, timePassed);
            if (lock != null) {
                sleepTime.waitOn(lock);
            } else if (condition != null) {
                sleepTime.waitFor(condition);
            } else {
                sleepTime.sleep();
            }
            if (factor != null && waitTime != maxInterval) {
                waitTime = TimeSpan.create((long) (waitTime.time() * factor), waitTime.unit());
                if (waitTime.isLongerThan(maxInterval)) {
                    waitTime = maxInterval;
                }
            }
        }
        return t;
    }

    private <T> T call(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new IllegalArgumentException
                    (this + " got exception when (re)trying " + callable, e);
        }
    }

    private TimeSpan timePassed(Time suggestedTime) {
        return start.timeSpanTo(now(suggestedTime));
    }

    private static Time now(Time suggestedTime) {
        return suggestedTime == null ? Time.mark() : suggestedTime;
    }

    @Override
    public String toString() {
        return ToString.of(this, "at", start.after(deadlineTime), "time left", timeLeft());
    }
}
