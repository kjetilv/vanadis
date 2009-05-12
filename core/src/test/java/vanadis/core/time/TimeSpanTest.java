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

import vanadis.core.test.VAsserts;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TimeSpanTest extends TimeTestCase {

    @Test
    public void eternity() {
        assertTrue(TimeSpan.FOREVER.isLongerThan(TimeSpan.SECOND));
        assertTrue(TimeSpan.FOREVER.isLongerThan(TimeSpan.seconds(Integer.MAX_VALUE)));
        assertFalse(TimeSpan.FOREVER.isShorterThan(TimeSpan.SECOND));
        assertFalse(TimeSpan.FOREVER.isShorterThan(TimeSpan.seconds(Integer.MAX_VALUE)));

        assertEquals(0, TimeSpan.FOREVER.compareTo(TimeSpan.FOREVER));
        assertEquals(1, TimeSpan.FOREVER.compareTo(TimeSpan.seconds(Integer.MAX_VALUE)));
        assertEquals(-1, TimeSpan.seconds(Integer.MAX_VALUE).compareTo(TimeSpan.FOREVER));
    }

    @Test
    public void timeSpan() {
        TimeSpan val = TimeSpan.create(2, TimeUnit.SECONDS);
        assertEquals(2L, val.time());
        VAsserts.assertEqHcMatch(TimeUnit.SECONDS, val.unit());
        assertEquals(2, val.secondTime());
        assertEquals(2 * K, val.msTime());
        assertEquals(2 * M, val.microTime());
        assertEquals(2 * G, val.nanoTime());
    }

    @Test
    public void equalsAndHashCode() {
        TimeSpan timeSpan = TimeSpan.seconds(2);
        VAsserts.assertEqHcMatch(timeSpan, timeSpan);
        VAsserts.assertEqHcMatch(timeSpan, TimeSpan.micros(2 * M));
        VAsserts.assertEqHcMatch(TimeSpan.ms(2), TimeSpan.micros(2 * K));
        VAsserts.assertEqHcMatch(TimeSpan.micros(2), TimeSpan.nanos(2 * K));
        VAsserts.assertEqHcMatch(TimeSpan.seconds(2), TimeSpan.nanos(2 * G));

        VAsserts.assertEqHcMismatch(timeSpan, "foo");
    }

    @Test
    public void remaining() {
        assertEquals(TimeSpan.HALF_MINUTE, TimeSpan.MINUTE.remainingAfter(TimeSpan.HALF_MINUTE));
        assertEquals(TimeSpan.INSTANT, TimeSpan.HALF_MINUTE.remainingAfter(TimeSpan.MINUTE));
        assertEquals(TimeSpan.seconds(5), TimeSpan.seconds(6).remainingAfter(TimeSpan.SECOND));
        assertEquals(TimeSpan.seconds(5), TimeSpan.seconds(6).remainingAfter(TimeSpan.micros(M)));
    }

    @Test
    public void as() {
        TimeSpan one = TimeSpan.seconds(2);
        TimeSpan two = TimeSpan.ms(2000);
        assertEquals(TimeUnit.MILLISECONDS, one.as(TimeUnit.MILLISECONDS).unit());
        assertEquals(2 * K, one.as(TimeUnit.MILLISECONDS).time());
        assertEquals(two, one.as(TimeUnit.MILLISECONDS));

        assertSame(TimeSpan.FOREVER.asMicros(), TimeSpan.FOREVER);
        assertSame(TimeSpan.FOREVER.asMs(), TimeSpan.FOREVER);
        assertSame(TimeSpan.FOREVER.asNanos(), TimeSpan.FOREVER);
        assertSame(TimeSpan.FOREVER.asSeconds(), TimeSpan.FOREVER);
    }

    @Test
    public void longerThan() {
        assertTrue(TimeSpan.HALF_MINUTE.isLongerThan(TimeSpan.HUNDRED_MS));
        assertFalse(TimeSpan.ms(1).isLongerThan(TimeSpan.HUNDRED_MS));
        assertFalse(TimeSpan.ms(1).isLongerThan(TimeSpan.FOREVER));
    }

    @Test
    public void createHours() {
        assertEquals(3600L, TimeSpan.hours(1).asSeconds().time());
        assertEquals(3600L, TimeSpan.hours(1L).asSeconds().time());
    }

    @Test
    public void createMinutes() {
        assertEquals(3600L, TimeSpan.minutes(60).asSeconds().time());
        assertEquals(3600L, TimeSpan.minutes(60L).asSeconds().time());
    }

    @Test
    public void createMillis() {
        assertEquals(2L, TimeSpan.ms(2000).asSeconds().time());
        assertEquals(2L, TimeSpan.ms(2000L).asSeconds().time());
    }

    @Test
    public void createSeconds() {
        assertEquals(2000L, TimeSpan.seconds(2).as(TimeUnit.MILLISECONDS).time());
        assertEquals(2000L, TimeSpan.seconds(2L).as(TimeUnit.MILLISECONDS).time());
    }

    @Test
    public void instant() {
        assertTrue(TimeSpan.INSTANT.isInstant());
        assertSame(TimeSpan.INSTANT, TimeSpan.ms(0));
    }

    @Test
    public void shorterThan() {
        assertTrue(TimeSpan.HUNDRED_MS.isShorterThan(TimeSpan.MINUTE));
        assertTrue(TimeSpan.HUNDRED_MS.isShorterThan(TimeSpan.HALF_MINUTE));
        assertFalse(TimeSpan.HUNDRED_MS.isShorterThan(TimeSpan.ms(99)));
        assertFalse(TimeSpan.HUNDRED_MS.isShorterThan(TimeSpan.HUNDRED_MS));
    }

    @Test
    public void fromStringAndBackAgain() {
        assertParsed("30ms", TimeSpan.ms(30));
        assertParsed("30seconds", TimeSpan.HALF_MINUTE);
        assertParsed("4560000ms", TimeSpan.seconds(4560));
        assertParsed("4560000millisecs", TimeSpan.seconds(4560));
        assertParsed("4560000millis", TimeSpan.seconds(4560));
        assertParsed("4560000ms", TimeSpan.seconds(4560));
        assertParsed("5micros", TimeSpan.micros(5));
        assertParsed("30s", TimeSpan.HALF_MINUTE);
        assertParsed("5ns", TimeSpan.nanos(5));
        assertParsed("20nanoseconds", TimeSpan.nanos(20));
        assertParsed("20nanosecs", TimeSpan.nanos(20));

        assertParsed("30  ms", TimeSpan.ms(30));
        assertParsed("30 seconds", TimeSpan.HALF_MINUTE);
        assertParsed("4560000\n\n ms", TimeSpan.seconds(4560));
        assertParsed("4560000 millisecs", TimeSpan.seconds(4560));
        assertParsed("4560000  millis", TimeSpan.seconds(4560));
        assertParsed("4560000  ms", TimeSpan.seconds(4560));
        assertParsed("5\t\tmicros", TimeSpan.micros(5));
        assertParsed("30 s", TimeSpan.HALF_MINUTE);
        assertParsed("5  \t \n ns", TimeSpan.nanos(5));
        assertParsed("20  nanoseconds", TimeSpan.nanos(20));
        assertParsed("20  nanosecs", TimeSpan.nanos(20));

        assertParsed("60", TimeSpan.MINUTE);
        assertParsed("1min", TimeSpan.MINUTE);
        assertParsed("2mins", TimeSpan.minutes(2));
        assertParsed("30", TimeSpan.HALF_MINUTE);

        assertParsed("0", TimeSpan.INSTANT);
        assertParsed("0ms", TimeSpan.INSTANT);
        assertParsed("0s", TimeSpan.INSTANT);
        assertParsed("0hours", TimeSpan.INSTANT);

        assertNotParsed("foobar");
        assertNotParsed("&&&");
        assertNotParsed("foobarms");
        assertNotParsed("foobars");
        assertNotParsed("10potatoes");
        assertNotParsed("1000000000000000000000000000000000000000000000000secs");
    }

    private static void assertNotParsed(String invalid) {
        try {
            fail("Should not parse : " + TimeSpan.parse(invalid));
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void toStrings() {
        assertEquals("1 second", TimeSpan.SECOND.toString());
        assertEquals("2 seconds", TimeSpan.seconds(2).toString());
    }

    private static void assertParsed(String str, TimeSpan expected) {
        TimeSpan timeSpan = TimeSpan.parse(str);
        TimeSpan timeSpanSpaced = TimeSpan.parse(" " + str + "  ");
        assertEquals(timeSpan, timeSpanSpaced);
        assertNotNull(timeSpan);
        assertEquals(timeSpan, expected);
        assertEquals(timeSpan, TimeSpan.parse(timeSpan.toTimeSpanString(false)));
//        assertEquals(timeSpan, TimeSpan.parse(timeSpan.toHumanFriendlyString()));
        assertEquals(timeSpan, TimeSpan.parse(timeSpan.toTimeSpanString()));
    }

    @Test
    public void testHumanFriendlyString(){
        assertEquals("100 " + TimeUnit.MILLISECONDS.name().toLowerCase(),
                     TimeSpan.micros(100155).toHumanFriendlyString());
        assertEquals("120 " + TimeUnit.MILLISECONDS.name().toLowerCase(),
                     TimeSpan.nanos(120155345).toHumanFriendlyString());
        assertEquals("122 " + TimeUnit.MILLISECONDS.name().toLowerCase(),
                     TimeSpan.nanos(122155345).toHumanFriendlyString());
    }

    @Test
    public void asSecondsRounded() {
        TimeSpan timeSpan = TimeSpan.ms(2500);
        assertEquals(TimeUnit.SECONDS, timeSpan.asSeconds().unit());
        assertEquals(2, timeSpan.asSeconds().time());
    }

    @Test
    public void asSeconds() {
        TimeSpan timeSpan = TimeSpan.ms(2000);
        assertEquals(TimeUnit.SECONDS, timeSpan.asSeconds().unit());
        assertEquals(2, timeSpan.asSeconds().time());
    }

    @Test
    public void asMilliseconds() {
        TimeSpan timeSpan = TimeSpan.seconds(2);
        assertEquals(TimeUnit.MILLISECONDS, timeSpan.asMs().unit());
        assertEquals(2000, timeSpan.asMs().time());
    }

    @Test
    public void asNanos() {
        TimeSpan timeSpan = TimeSpan.seconds(2);
        assertEquals(TimeUnit.NANOSECONDS, timeSpan.asNanos().unit());
        assertEquals(2000000000, timeSpan.asNanos().time());
    }


    @Test
    public void asMicros() {
        TimeSpan timeSpan = TimeSpan.seconds(2);
        assertEquals(TimeUnit.MICROSECONDS, timeSpan.asMicros().unit());
        assertEquals(2000000, timeSpan.asMicros().time());
    }

    @Test
    public void illegalTime() {
        assertEquals(TimeSpan.INSTANT, TimeSpan.create(-5, TimeUnit.SECONDS));
        assertEquals(TimeSpan.INSTANT, TimeSpan.create(0, TimeUnit.SECONDS));
    }

    @Test
    public void rounded() {
        assertEquals(52, TimeSpan.nanos(52000).rounded().time());
        assertEquals(TimeUnit.MICROSECONDS, TimeSpan.nanos(52000).rounded().unit());
        assertEquals(52, TimeSpan.nanos(52).rounded().time());
        assertEquals(TimeUnit.NANOSECONDS, TimeSpan.nanos(52).rounded().unit());
        TimeUnit last = TimeUnit.values()[TimeUnit.values().length - 1];
        assertEquals(23000, TimeSpan.create(23000, last).rounded().time());
        assertEquals(last, TimeSpan.create(23000, last).unit());
    }

    @Test
    public void approximate() {
        assertEquals(TimeSpan.SECOND, TimeSpan.micros(1023000).approximate());
        assertEquals(TimeSpan.SECOND, TimeSpan.ms(1023).approximate());
        assertEquals(TimeSpan.ms(23), TimeSpan.ms(23).approximate());
        assertEquals(TimeSpan.ms(23), TimeSpan.micros(23000).approximate());
        assertEquals(TimeSpan.seconds(42), TimeSpan.nanos(42456456345L).approximate());
        assertEquals(TimeSpan.seconds(42), TimeSpan.micros(42456445L).approximate());
        assertEquals(TimeSpan.seconds(42), TimeSpan.ms(42464).approximate());
    }

    @Test
    public void approximateUnit() {
        assertEquals(TimeSpan.SECOND,
                     TimeSpan.micros(1023000).approximate(TimeUnit.SECONDS));
        assertEquals(TimeSpan.HUNDRED_MS,
                     TimeSpan.nanos(100004000).approximate(TimeUnit.MILLISECONDS));
    }

    @Test
    public void sleep() {
        Time before = Time.mark();
        TimeSpan ts = TimeSpan.ms(2);
        ts.sleep();
        Time after = Time.mark();
        assertTrue(after.isAfter(before));
        TimeSpan span = after.timeSpanTo(before);
        assertTrue(ts.equals(span) || ts.isShorterThan(span));
    }

    @Test
    public void testTimeSpanTo() {
        Time time = Time.mark();
        TimeSpan sleepTime = TimeSpan.HUNDRED_MS;
        sleepTime.sleep();
        TimeSpan timeSpan = TimeSpan.since(time);
        assertTrue(timeSpan.equals(sleepTime) || timeSpan.isLongerThan(sleepTime));
    }

    @Test
    public void condition()
            throws InterruptedException {
        assertWaitForCondition(TimeSpan.SECOND);
    }

    @Test
    public void waitForTheFuture()
            throws TimeoutException, ExecutionException, InterruptedException {
        assertWaitForFuture(TimeSpan.SECOND);
    }

    @Test
    public void waiting()
            throws InterruptedException {
        assertWaitOnLock(TimeSpan.SECOND);
    }

    @Test
    public void deadline() {
        TimeSpan deadlineTime = TimeSpan.SECOND;
        Time zero = Time.msEpoch(0);
        Time milli = Time.msEpoch(1);
        Time halfTime = Time.msEpoch(500);
        Time almostTime = Time.msEpoch(999);
        Time time = Time.msEpoch(1000);
        Time overTime = Time.msEpoch(1001);
        Time wayOverdue = Time.msEpoch(2000000);

        Deadline deadline = deadlineTime.newDeadline(zero);
        assertFalse(deadline.isExceededAt(zero));
        assertTrue(deadline.hasTimeLeft(zero));
        assertEquals(deadlineTime, deadline.timeLeftAt(zero));

        assertFalse(deadline.isExceededAt(halfTime));
        assertTrue(deadline.hasTimeLeft(halfTime));
        assertEquals(TimeSpan.ms(halfTime.getEpoch()), deadline.timeLeftAt(halfTime));

        assertFalse(deadline.isExceededAt(almostTime));
        assertTrue(deadline.hasTimeLeft(almostTime));
        assertEquals(TimeSpan.ms(milli.getEpoch()), deadline.timeLeftAt(almostTime));

        assertFalse(deadline.isExceededAt(time));
        assertFalse(deadline.hasTimeLeft(time));
        assertEquals(TimeSpan.INSTANT, deadline.timeLeftAt(time));

        assertTrue(deadline.isExceededAt(overTime));
        assertFalse(deadline.hasTimeLeft(overTime));
        assertEquals(TimeSpan.INSTANT, deadline.timeLeftAt(overTime));

        assertTrue(deadline.isExceededAt(wayOverdue));
        assertFalse(deadline.hasTimeLeft(wayOverdue));
        assertEquals(TimeSpan.INSTANT, deadline.timeLeftAt(wayOverdue));
    }

    @Test
    public void difference() {
        assertEquals(TimeSpan.HALF_MINUTE, TimeSpan.MINUTE.difference(TimeSpan.HALF_MINUTE));
        assertEquals(TimeSpan.HALF_MINUTE, TimeSpan.HALF_MINUTE.difference(TimeSpan.MINUTE));
        assertEquals(TimeSpan.HALF_MINUTE, TimeSpan.HALF_MINUTE.difference(TimeSpan.INSTANT));
        assertEquals(TimeSpan.HALF_MINUTE, TimeSpan.INSTANT.difference(TimeSpan.HALF_MINUTE));
    }

    @Test
    public void shortest() {
        assertEquals(TimeSpan.HUNDRED_MS, TimeSpan.shortest(TimeSpan.HALF_MINUTE,
                                                            TimeSpan.HUNDRED_MS,
                                                            TimeSpan.SECOND));
    }

    @Test
    public void add() {
        assertEquals(TimeSpan.HALF_MINUTE, TimeSpan.INSTANT.added(TimeSpan.HALF_MINUTE));
        assertEquals(TimeSpan.micros(1000005),
                     TimeSpan.micros(5).added(TimeSpan.SECOND));
    }

    @Test
    public void dividedBy() {
        TimeSpan timeSpan = TimeSpan.SECOND.dividedBy(10);
        assertEquals(TimeSpan.HUNDRED_MS, timeSpan);
        assertEquals(TimeUnit.MILLISECONDS, timeSpan.unit());

        TimeSpan veryShort = TimeSpan.seconds(5).dividedBy(1000000000);
        assertEquals(TimeSpan.nanos(5), veryShort);
        assertEquals(TimeUnit.NANOSECONDS, veryShort.unit());
    }

    @Test
    public void foreverDeadline() {
        Deadline deadline = TimeSpan.FOREVER.newDeadline();
        assertTrue(deadline.hasTimeLeft());
        assertFalse(deadline.hasExpired());
        assertEquals(TimeSpan.FOREVER, deadline.timeLeft());
    }
}

