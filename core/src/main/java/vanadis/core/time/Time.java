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

package vanadis.core.time;

import vanadis.core.lang.EqHc;

import java.io.File;
import java.util.Date;

/**
 * A time, as in a point in time.  It can answer how long ago it is since that time, and it can provide the
 * time span between it and another point in time.  Useful for all kinds of "waiting and retrying" type of logic.
 */
public final class Time implements Comparable<Time> {

    private static final TickTock SYSTEM_CLOCK = new SystemTickTock();

    /**
     * Get the system time.
     *
     * @return Current time
     */
    public static Time mark() {
        return new Time(SYSTEM_CLOCK);
    }

    public static Time modified(File file) {
        return msEpoch(file.lastModified());
    }

    public static Time msEpoch(long msSince70) {
        return new Time(msSince70 * 1000 * 1000, msSince70);
    }

    /**
     * Get another time.  Only usable for unit testing.
     *
     * @param tickTock Tick tick
     * @return Time
     */
    public static Time mark(TickTock tickTock) {
        return new Time(tickTock.n(), tickTock.m());
    }

    private final long mark;

    private final long epoch;

    /**
     * Get the system time.
     */
    public Time() {
        this(SYSTEM_CLOCK);
    }

    public Time(TickTock tickTock) {
        this(tickTock.n(), tickTock.m());
    }

    private Time(long mark, long epoch) {
        this.mark = mark;
        this.epoch = epoch;
    }

    public TimeSpan timeSpanTo(Time time) {
        return time == null || mark == time.mark ? TimeSpan.INSTANT : TimeSpan.nanos(Math.abs(mark - time.mark));
    }

    public Time after(TimeSpan timeSpan) {
        return new Time(mark + timeSpan.nanoTime(), epoch + timeSpan.msTime());
    }

    @Override
    public int compareTo(Time o) {
        return mark < o.mark ? -1 : mark > o.mark ? 1 : 0;
    }

    public long getMark() {
        return mark;
    }

    public long getEpoch() {
        return epoch;
    }

    public Date getDate() {
        return new Date(epoch);
    }

    public boolean isBefore(Time time) {
        return mark < time.mark;
    }

    public boolean isAfter(Time time) {
        return mark > time.mark;
    }

    public TimeSpan since() {
        return timeSpanTo(Time.mark());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getDate() + "]";
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object == null) {
            return false;
        }
        Time time = EqHc.retyped(this, object);
        return time != null && time.mark == mark;
    }

    @Override
    public int hashCode() {
        return EqHc.hc(mark);
    }
}
