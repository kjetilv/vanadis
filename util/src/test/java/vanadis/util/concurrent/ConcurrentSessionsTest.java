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
package vanadis.util.concurrent;

import junit.framework.Assert;
import vanadis.core.time.TimeSpan;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentSessionsTest {

    @Test
    public void lockSemantics() {
        final Lock lock = new ReentrantLock(true);
        lock.lock();
        Assert.assertTrue(lock.tryLock());
        Assert.assertTrue(lock.tryLock());
        final AtomicBoolean bool = new AtomicBoolean();
        new Thread(new Runnable() {
            @Override
            public void run() {
                bool.set(lock.tryLock());
            }
        }).start();
        Assert.assertFalse(bool.get());
        lock.unlock();
    }

    @Test
    public void scavenging() {
        ConcurrentSessions<Integer, VeryDummySession> sessions = new ConcurrentSessions<Integer, VeryDummySession>
            (new VeryDummySessionFactory(),
             1);
        VeryDummySession session = sessions.acquire(1, TimeSpan.HUNDRED_MS);
        Assert.assertEquals(1, session.getKey());

        Assert.assertEquals(1, sessions.getSessionCount());
        Assert.assertEquals(1, sessions.getActiveSessionsCount());

        sessions.release();

        Assert.assertEquals(1, sessions.getSessionCount());
        Assert.assertEquals(0, sessions.getActiveSessionsCount());

        sessions.acquire(2, TimeSpan.HUNDRED_MS);
        sessions.release();
        sessions.acquire(3, TimeSpan.HUNDRED_MS);

        Assert.assertEquals(3, sessions.getSessionCount());
        Assert.assertEquals(1, sessions.getActiveSessionsCount());

        sessions.release();

        Assert.assertEquals(3, sessions.getSessionCount());
        Assert.assertEquals(0, sessions.getActiveSessionsCount());

        ScavengeResult<VeryDummySession> result = sessions.scavenge();

        Assert.assertEquals(2, sessions.getSessionCount());
        Assert.assertEquals(0, sessions.getActiveSessionsCount());

        Assert.assertEquals(1, result.getVictim().getKey());
        Assert.assertEquals(2, result.getApproximateRemaining());
    }

}
