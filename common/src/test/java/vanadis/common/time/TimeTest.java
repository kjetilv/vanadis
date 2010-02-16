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
package vanadis.common.time;

import vanadis.core.test.VAsserts;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TimeTest extends TimeTestCase {

    @Test
    public void tooString() {
        VAsserts.assertImplementsToString(mark(1));
    }

    @Test
    public void eqHash() {
        Time time1 = mark(5);
        VAsserts.assertEqHcMatch(time1, time1);
        VAsserts.assertEqHcMatch(time1, mark(5));
        VAsserts.assertEqHcMismatch(time1, "foo");
        VAsserts.assertEqHcMismatch(time1, mark(6));
    }

    @Test
    public void timeHasntStopped() {
        Time time = Time.mark(xms(100));
        assertTrue(new Time(xms(200)).isAfter(time));
    }

    @Test
    public void compare() {
        assertEquals(-1, mark(5).compareTo(mark(6)));
        assertEquals(0, mark(5).compareTo(mark(5)));
        assertEquals(1, mark(5).compareTo(mark(4)));
    }

    @Test
    public void epoch() {
        assertEquals(5, mark(5).getEpoch());
    }

    @Test
    public void mark() {
        Time time = Time.mark(xms(100));
        TimeSpan timeSince = time.timeSpanTo(Time.mark(xms(200)));
        assertFalse(timeSince + " should be at least " + TimeSpan.HUNDRED_MS,
                    TimeSpan.HUNDRED_MS.isLongerThan(timeSince));
        TimeSpan nanosSince = time.timeSpanTo(Time.mark());
        assertEquals(TimeUnit.NANOSECONDS, nanosSince.unit());
    }

    @Test
    public void timeSpanBetweenTimes() {
        Time atOneTime = Time.mark(xms(100));
        Time atSomeOtherTime = Time.mark(xms(200));
        TimeSpan timePassed = atSomeOtherTime.timeSpanTo(atOneTime);
        assertEquals(TimeSpan.HUNDRED_MS, timePassed);
        assertEquals(atSomeOtherTime.timeSpanTo(atOneTime), atOneTime.timeSpanTo(atSomeOtherTime));
    }

    @Test
    public void timeSpanSince() {
        AtomicLong al = new AtomicLong(5);
        TickTock tickTock = ams(al);
        Time time = Time.mark(tickTock);
        al.set(10);
        assertEquals(TimeSpan.nanos(5), time.timeSpanTo(Time.mark(tickTock)));
    }

    @Test
    public void after() {
        AtomicLong al = new AtomicLong(5);
        TickTock tickTock = ams(al);
        Time time = Time.mark(tickTock);
        Time after = time.after(TimeSpan.nanos(5));
        assertEquals(10, after.getMark());
    }

}
