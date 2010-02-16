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

package vanadis.common.time;

import vanadis.core.collections.Generic;
import vanadis.core.collections.Pair;
import vanadis.core.reflection.AbstractCoercer;
import vanadis.core.reflection.Retyper;
import vanadis.core.lang.EqHc;
import vanadis.core.lang.Not;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import static java.util.concurrent.TimeUnit.*;
import java.util.concurrent.locks.Condition;

/**
 * <P>An immutable time span.  Consists of a {@link java.util.concurrent.TimeUnit time unit}
 * and a number of units.  Time spans are equal if they represent the same amount of time, no matter what their
 * respective units are.</P>
 *
 * <P>The {@link #parse(String) parse factory method} embodies some rules for converting string representations
 * to time spans, round-trippable with {@link #toTimeSpanString()} .</P>
 *
 * <P>Intervals can be {@link #newDeadline() converted} to {@link vanadis.common.time.Deadline deadlines}.<P>
 */
public final class TimeSpan implements Comparable<TimeSpan>, Waiting, Serializable {

    private static class TimeSpanCoercer extends AbstractCoercer<TimeSpan> {

        @Override
        public TimeSpan coerce(String string) {
            return TimeSpan.parse(Not.nil(string, "timespan string"));
        }

        @Override
        public String toString(TimeSpan timeSpan) {
            return timeSpan.toTimeSpanString();
        }
    }

    static {
        Retyper.map(new TimeSpanCoercer());
    }

    private static final long serialVersionUID = -4460751471021045914L;

    public static final TimeSpan INSTANT = new TimeSpan(0L, NANOSECONDS);

    public static final TimeSpan FOREVER = new TimeSpan((Long) null, NANOSECONDS);

    public static final TimeSpan HUNDRED_MS = span(100L, MILLISECONDS);

    public static final TimeSpan SECOND = span(1L, SECONDS);

    public static final TimeSpan HALF_MINUTE = span(30L, SECONDS);

    public static final TimeSpan MINUTE = span(60L, SECONDS);

    public static final TimeSpan HOUR = span(3600L, SECONDS);

    public static final TimeSpan HALF_HOUR = span(1800L, SECONDS);

    private static final TimeUnit[] UNITS = values();

    static final String FOREVER_STRING = "forever";

    private static TimeSpan span(Long time, TimeUnit timeUnit) {
        return span(time, 1, timeUnit);
    }

    private static TimeSpan span(Long time, int mult, TimeUnit unit) {
        return time == null ? FOREVER
                : time <= 0 ? INSTANT
                        : new TimeSpan(mult * time, unit);
    }

    private TimeSpan oneFiner() {
        TimeUnit previous = null;
        for (TimeUnit unit : TimeUnit.values()) {
            if (unit == this.unit) {
                if (previous == null) {
                    return this;
                }
                long converted = previous.convert(time, this.unit);
                return span(converted, previous);
            }
            previous = unit;
        }
        throw new RuntimeException
                ("There should always be one finer than " + this.unit);
    }

    @SuppressWarnings({"RedundantTypeArguments"})
    private static Pair<TimeSpan, TimeSpan> sameUnitPair(TimeSpan one, TimeSpan two) {
        Not.nil(one, "first timeSpan");
        Not.nil(two, "second timeSpan");
        return one.unit == two.unit ? new Pair<TimeSpan, TimeSpan>(one, two)
                : one.finerThan(two) ? new Pair<TimeSpan, TimeSpan>(one, two.as(one.unit()))
                        : new Pair<TimeSpan, TimeSpan>(one.as(two.unit()), two);
    }

    /**
     * <P>Parse an interval from a string.  Supported formats:</P>
     *
     * <UL>
     * <LI><code>&lt;number&gt;</code> parses to seconds
     * <LI><code>&lt;number&gt;ms</code> to milliseconds, and so on for...
     * <LI><code>&lt;number&gt;microsecs</code></LI>
     * <LI><code>&lt;number&gt;microseconds</code></LI>
     * <LI><code>&lt;number&gt;nanosecs</LI>
     * <LI><code>&lt;number&gt;nanoseconds</code></LI>
     * <LI><code>&lt;number&gt;?</code>(microseconds)</LI>
     * <LI><code>&lt;number&gt;ns</code></LI>
     * <LI><code>&lt;number&gt;millis</code></LI>
     * <LI><code>&lt;number&gt;millisecs</code></LI>
     * <LI><code>&lt;number&gt;milliseconds</code></LI>
     * <LI><code>&lt;number&gt;seconds</code></LI>
     * <LI><code>&lt;number&gt;secs</code></LI>
     * <LI><code>&lt;number&gt;s</code></LI>
     * <UL>
     * </P>
     *
     * @param str String
     * @return TimeSpan
     * @throws IllegalArgumentException If parsing failed
     */
    public static TimeSpan parse(String str) {
        return TimeSpanParser.parse(str);
    }

    public static TimeSpan shortest(Iterable<TimeSpan> intervals) {
        List<TimeSpan> list = Generic.list(intervals);
        Collections.sort(list);
        return list.get(0);
    }

    public static TimeSpan shortest(TimeSpan... timeSpans) {
        return shortest(Arrays.asList(timeSpans));
    }

    public static TimeSpan hours(long time) {
        return span(time, 3600, SECONDS);
    }

    public static TimeSpan minutes(long time) {
        return span(time, 60, SECONDS);
    }

    public static TimeSpan seconds(long time) {
        return span(time, SECONDS);
    }

    public static TimeSpan millis(long time) {
        return ms(time);
    }

    public static TimeSpan ms(long time) {
        return span(time, MILLISECONDS);
    }

    public static TimeSpan micros(long time) {
        return span(time, MICROSECONDS);
    }

    public static TimeSpan nanos(long time) {
        return span(time, NANOSECONDS);
    }

    public static TimeSpan hours(int time) {
        return span((long) time, 3600, SECONDS);
    }

    public static TimeSpan minutes(int time) {
        return span((long) time, 60, SECONDS);
    }

    public static TimeSpan seconds(int time) {
        return span((long) time, SECONDS);
    }

    public static TimeSpan millis(int time) {
        return ms((long) time);
    }

    public static TimeSpan ms(int time) {
        return span((long) time, MILLISECONDS);
    }

    public static TimeSpan micros(int time) {
        return span((long) time, MICROSECONDS);
    }

    public static TimeSpan nanos(int time) {
        return span((long) time, NANOSECONDS);
    }

    public static TimeSpan since(Time time) {
        return time == null ? INSTANT
                : time.timeSpanTo(Time.mark());
    }

    public static TimeSpan create(long time, TimeUnit unit) {
        return span(time, unit);
    }

    private final long time;

    private final TimeUnit unit;

    private TimeSpan(Long time, TimeUnit unit) {
        if (time != null && time < 0) {
            throw new IllegalArgumentException("Negative timespan: " + time + " " + unit.name().toLowerCase());
        }
        this.time = time == null ? -1 : time;
        this.unit = Not.nil(unit, "unit");
    }

    private boolean finerThan(TimeSpan timeSpan) {
        return this.unit.compareTo(timeSpan.unit) < 0;
    }

    public long msTime() {
        return unitsOf(MILLISECONDS);
    }

    public long secondTime() {
        return unitsOf(SECONDS);
    }

    public long microTime() {
        return unitsOf(MICROSECONDS);
    }

    public long nanoTime() {
        return unitsOf(NANOSECONDS);
    }

    public long unitsOf(TimeUnit unit) {
        Not.nil(unit, "unit");
        return isForever() ? -1
                : isInstant() ? 0
                        : unit.convert(time, this.unit);
    }

    public TimeUnit unit() {
        return unit;
    }

    public long time() {
        return time;
    }

    public boolean isForever() {
        return this == FOREVER || this.time < 0;
    }

    public boolean isInstant() {
        return this == INSTANT || this.time() == 0;
    }

    @Override
    public int hashCode() {
        return (int) ((17 + 37 * nanoTime()) % Integer.MAX_VALUE);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object == null) {
            return false;
        }
        TimeSpan timeSpan = EqHc.retyped(this, object);
        if (timeSpan == null) {
            return false;
        }
        if (isForever() && timeSpan.isForever()) {
            return true;
        }
        if (isInstant() && timeSpan.isInstant()) {
            return true;
        }
        Pair<TimeSpan, TimeSpan> pair = sameUnitPair(this, timeSpan);
        return EqHc.eq(pair.getOne().time, pair.getTwo().time,
                       pair.getOne().unit, pair.getTwo().unit);
    }

    @Override
    public String toString() {
        return toHumanFriendlyString();
    }

    public String toHumanFriendlyString() {
        return rounded().toTimeSpanString(true);
    }

    public String toTimeSpanString() {
        return toTimeSpanString(false);
    }

    public String toTimeSpanString(boolean spaced) {
        if (isForever()) {
            return FOREVER_STRING;
        }
        String name = unit.name().toLowerCase();
        return time + (spaced ? " " : "") + (time == 1 ? name.substring(0, name.length() - 1) : name);
    }

    public TimeSpan difference(TimeSpan timeSpan) {
        if (timeSpan == null || isForever()) {
            return this;
        }
        if (isInstant()) {
            return timeSpan;
        }
        Pair<TimeSpan, TimeSpan> pair = sameUnitPair(this, timeSpan);
        TimeSpan me = pair.getOne();
        TimeSpan it = pair.getTwo();
        long difference = Math.abs(me.time() - it.time());
        return span(difference, me.unit());
    }

    public TimeSpan remainingAfter(TimeSpan timeSpan) {
        if (timeSpan == null || isForever() || isInstant()) {
            return this;
        }
        Pair<TimeSpan, TimeSpan> pair = sameUnitPair(this, timeSpan);
        TimeSpan me = pair.getOne();
        TimeSpan it = pair.getTwo();
        return it.time() > me.time() ? TimeSpan.INSTANT : span(me.time() - it.time(), me.unit());
    }

    @Override
    public int compareTo(TimeSpan timeSpan) {
        Not.nil(timeSpan, "compare argument");
        if (isForever()) {
            return timeSpan.isForever() ? 0 : 1;
        } else if (timeSpan.isForever()) {
            return -1;
        }
        Pair<TimeSpan, TimeSpan> pair = sameUnitPair(this, timeSpan);
        long t1 = pair.getOne().time;
        long t2 = pair.getTwo().time;
        return t1 < t2 ? -1 : (t1 > t2 ? 1 : 0);
    }

    public TimeSpan asSeconds() {
        return as(SECONDS);
    }

    public TimeSpan asMicros() {
        return as(MICROSECONDS);
    }

    public TimeSpan asNanos() {
        return as(NANOSECONDS);
    }

    public TimeSpan asMs() {
        return as(MILLISECONDS);
    }

    public TimeSpan as(TimeUnit unit) {
        Not.nil(unit, "unit");
        return isForever() ? this : span(unitsOf(unit), unit);
    }

    @Override
    public boolean waitFor(Condition condition, boolean failOnInterrupt) {
        return JustWaiting.forThat(this, Not.nil(condition, "condition"), failOnInterrupt);
    }

    @Override
    public boolean waitFor(Condition condition) {
        return waitFor(Not.nil(condition, "condition"), true);
    }

    @Override
    public <T> T waitFor(Future<T> future)
            throws ExecutionException, TimeoutException, InterruptedException {
        return JustWaiting.forThe(Not.nil(future, "future"), this);
    }

    @Override
    public <T> T waitForUnchecked(Future<T> future) {
        return JustWaiting.nervouslyForThe(Not.nil(future, "future"), this, "{0} was cancelled", future);
    }

    @Override
    public boolean waitOn(Object lock, boolean failOnInterrupt) {
        return JustWaiting.onThat(Not.nil(lock, "lock"), this, failOnInterrupt);
    }

    @Override
    public boolean waitOn(Object lock) {
        return waitOn(Not.nil(lock, "lock"), true);
    }

    @Override
    public boolean sleep() {
        return sleep(true);
    }

    public boolean sleep(boolean fail) {
        return JustWaiting.beforeIWake(this, fail);
    }

    /**
     * Null argument is equivalent to passing {@link TimeSpan#INSTANT}.
     *
     * @param timeSpan Time span
     * @return True if shorter
     */
    public boolean isShorterThan(TimeSpan timeSpan) {
        return timeSpan != null && compareTo(timeSpan) < 0;
    }

    /**
     * Null argument is equivalent to passing {@link TimeSpan#INSTANT}.
     *
     * @param timeSpan Time span
     * @return True if longer
     */
    public boolean isLongerThan(TimeSpan timeSpan) {
        return timeSpan == null || compareTo(timeSpan) > 0;
    }

    /**
     * Return an approximation of this interval, using an optional, preferred unit.
     * For instance, 42,345,543 microseconds will approximate to 42,345 milliseconds,
     * if {@link java.util.concurrent.TimeUnit#MILLISECONDS} is used.
     *
     * @param unit A unit
     * @return Approximation using that unit
     */
    public TimeSpan approximate(TimeUnit unit) {
        long nanos = nanoTime();
        long tenths = (long) Math.floor(Math.log10(nanos));
        for (int i = 0; ; i++) {
            int exp = (i + 1) * 3;
            if (tenths < exp || i == 3 || unit == UNITS[i]) {
                long div = (long) Math.pow(10, exp - 3);
                long time = nanos / div;
                return span(time, UNITS[i]);
            }
        }
    }

    /**
     * Returns a readable approximation of this time span.  For instance, instead of being
     * billions of nanoseconds, it will be a number of seconds.  Example: 42,345,543 microseconds
     * will approximate to 42 seconds.  In other words, it will gravitate towards a unit
     * that makes it more approximate, but suits the size of the time span.
     *
     * @return Approximate time span
     */
    public TimeSpan approximate() {
        return approximate(null);
    }

    public TimeSpan rounded() {
        int increasingUnit = Arrays.binarySearch(UNITS, this.unit);
        long decreasingTime = this.time;
        while (true) {
            if (increasingUnit < UNITS.length) {
                if (decreasingTime > 1000) {
                    decreasingTime /= 1000;
                    increasingUnit++;
                } else {
                    return span(decreasingTime, UNITS[increasingUnit]);
                }
            } else {
                return this;
            }
        }
    }

    public boolean awaitTermination(ExecutorService service, boolean failInterrupted) {
        return JustWaiting.forThe(Not.nil(service, "service"), this, failInterrupted);
    }

    public TimeSpan added(TimeSpan timeSpan) {
        if (timeSpan == null || isForever()) {
            return this;
        } else if (isInstant()) {
            return timeSpan;
        } else {
            Pair<TimeSpan, TimeSpan> pair = sameUnitPair(this, timeSpan);
            TimeSpan me = pair.getOne();
            TimeSpan other = pair.getTwo();
            return span(me.time() + other.time(), me.unit());
        }
    }

    public TimeSpan dividedBy(int divisor) {
        long divided = time / divisor;
        return divided > 0 || unit == NANOSECONDS
                ? span(divided, unit)
                : this.oneFiner().dividedBy(divisor);
    }

    public static TimeSpan until(Time time) {
        if (time == null) {
            return INSTANT;
        }
        Time now = Time.mark();
        if (time.isBefore(now)) {
            return INSTANT;
        }
        return now.timeSpanTo(time);
    }

    public Deadline newDeadline() {
        return newDeadline(Time.mark());
    }

    Deadline newDeadline(Time time) {
        return new Deadline(time, this);
    }
}
