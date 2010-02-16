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

import junit.framework.Assert;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class TimeTestCase extends Assert {

    protected static final int K = 1000;

    protected static final int M = K * K;

    protected static final int G = M * K;

    protected static class Atomtick implements TickTock {

        private final AtomicLong atomicLong;

        protected Atomtick(AtomicLong atomicLong) {
            this.atomicLong = atomicLong;
        }

        @Override
        public long n() {
            return atomicLong.get();
        }

        @Override
        public long m() {
            return atomicLong.get() / M;
        }
    }

    private static class XMs implements TickTock {

        private final int ms;

        private XMs(int ms) {
            this.ms = ms;
        }

        @Override
        public long n() {
            return ms * M;
        }

        @Override
        public long m() {
            return ms;
        }
    }

    protected Time mark(int ms) {
        return Time.mark(xms(ms));
    }

    protected TickTock xms(int ms) {
        return new XMs(ms);
    }

    protected Future<Object> future(Object foo) {
        return new FooFuture(foo);
    }

    protected TickTock ams(AtomicLong al) {
        return new Atomtick(al);
    }

    protected void assertWaitForCondition(final Waiting waiting)
        throws InterruptedException {
        final Lock lock = new ReentrantLock(true);
        final Condition cond = lock.newCondition();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    lock.lock();
                    waiting.waitFor(cond);
                } finally {
                    lock.unlock();
                }
            }
        };
        Thread thread = startThread(runnable);
        try {
            lock.lock();
            cond.signal();
        } finally {
            lock.unlock();
        }
        thread.join();
        assertFalse(thread.isAlive());
    }

    protected Thread startThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.start();
        return thread;
    }

    protected void assertWaitForFuture(Waiting waiting)
        throws ExecutionException, TimeoutException, InterruptedException {
        Future<Object> future = future("foo");
        Object foo = waiting.waitForUnchecked(future);
        assertEquals("foo", foo);
    }

    @SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter"})
    protected void assertWaitOnLock(final Waiting waiting)
        throws InterruptedException {
        final Object lock = new Object();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    waiting.waitOn(lock);
                }
            }
        };
        Thread thread = startThread(runnable);
        synchronized (lock) {
            lock.notifyAll();
        }
        thread.join();
        assertFalse(thread.isAlive());
    }

    protected void assertSleep(Time startTime, Time endTime, Waiting waiting, TimeSpan minimumTimeSpan) {
        waiting.sleep();
        TimeSpan timePassed = endTime.timeSpanTo(startTime);
        assertTrue("Min " + minimumTimeSpan + ", got " + timePassed.approximate() + ", difference: " +
                   minimumTimeSpan.difference(timePassed),
                   timePassed.equals(minimumTimeSpan) || timePassed.isLongerThan(minimumTimeSpan));
    }
}
