package vanadis.integrationtests;

import junit.framework.Assert;
import vanadis.core.collections.Generic;
import vanadis.core.time.TimeSpan;
import vanadis.util.concurrent.ConcurrentSessions;
import vanadis.util.concurrent.ScavengeResult;
import vanadis.util.concurrent.SessionFactory;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ConcurrentSessionsIntegrationTest {

    private final DummySession NULL = new DummySession(-1, null, null, null, null);

    @Test
    public void lowConcurrency() throws ExecutionException, InterruptedException {
        runConcurrency(2, 20, 200, TimeSpan.micros(500));
    }

    @Test
    public void mediumConcurrency() throws ExecutionException, InterruptedException {
        runConcurrency(10, 200, 10000, TimeSpan.micros(200));
    }

    @Test
    public void highConcurrency() throws ExecutionException, InterruptedException {
        runConcurrency(40, 200, 10000, TimeSpan.micros(200));
    }

    @Test
    public void veryHighConcurrency() throws ExecutionException, InterruptedException {
        runConcurrency(80, 1000, 10000, TimeSpan.micros(400));
    }

    @Test
    public void extremelyHighConcurrency() throws ExecutionException, InterruptedException {
        runConcurrency(160, 2000, 10000, TimeSpan.micros(2));
    }

    private void runConcurrency(int conc, int keyCount, int opCount, TimeSpan opTime)
        throws ExecutionException, InterruptedException {
        if (opCount % 10 != 0) {
            throw new IllegalArgumentException("opCount must be divisible by 10");
        }
        AtomicInteger scavenges = new AtomicInteger(0);
        AtomicInteger opCountdown = new AtomicInteger(opCount);
        int maxCreate = keyCount + opCount / 10;
        AtomicInteger closeCountdown = new AtomicInteger();
        AtomicReference<List<String>> failed =
            new AtomicReference<List<String>>(Generic.<String>synchList());
        ConcurrentSessions<Integer, DummySession> sessions = new ConcurrentSessions<Integer, DummySession>
            (new DummySessionFactory
                (keyCount, maxCreate, failed, opCountdown, closeCountdown, opTime), conc);

        ExecutorService service = Executors.newFixedThreadPool(conc);
        for (int i = 0; i < opCount; i++) {
            Assert.assertTrue(messages(failed), failed.get().isEmpty());
            service.execute(new Task(sessions, i % keyCount, service, failed));
            if (i % 10 == 0) {
                service.execute(new Scavenge(sessions, null, scavenges, failed));
            }
        }
        close(opCountdown, service);
        Assert.assertEquals(0, sessions.getActiveSessionsCount());
        Assert.assertTrue("User count " + keyCount + " < " + sessions.getSessionCount(),
                          keyCount >= sessions.getSessionCount());
        Assert.assertFalse(sessions.close() == 0);

        service = Executors.newFixedThreadPool(conc);
        closeCountdown.set(sessions.getSessionCount());
        Set<Boolean> okClose = Generic.set();
        for (int i = 0; i < keyCount; i++) {
            service.execute(new Scavenge(sessions, okClose, scavenges, failed));
        }
        Assert.assertTrue(messages(failed), failed.get().isEmpty());

        close(closeCountdown, service);
        Assert.assertEquals(0, sessions.getSessionCount());
        Assert.assertTrue(okClose.size() < keyCount);

        Assert.assertTrue(messages(failed), failed.get().isEmpty());
        Assert.assertEquals(0, opCountdown.get());
        Assert.assertEquals(0, closeCountdown.get());

        Assert.assertTrue("No scavenges!", scavenges.get() > 1);
        System.out.println(scavenges);
    }

    private void close(AtomicInteger opCountdown, ExecutorService service) {
        Assert.assertNotNull
            ("Failed to shut down in time",
             TimeSpan.minutes(10).newDeadline().tryEvery
                 (TimeSpan.seconds(2), new Shutdown(service, opCountdown)));
    }

    private static String messages(AtomicReference<List<String>> failed) {
        List<String> stringList = failed.get();
        StringBuilder sb = new StringBuilder(stringList.size() + ":[\n");
        for (String string : stringList) {
            sb.append("  ").append(string).append("\n");
        }
        return sb.append("]").toString();
    }

    private class DummySessionFactory implements SessionFactory<Integer, DummySession> {

        private int count;

        private final int userCount;

        private final int maxCreates;

        private final AtomicReference<List<String>> failed;

        private final AtomicInteger opCountdown;

        private final AtomicInteger closeCountdown;

        private final TimeSpan opTime;

        private DummySessionFactory(int userCount, int maxCreates,
                                   AtomicReference<List<String>> failed,
                                   AtomicInteger opCountdown,
                                   AtomicInteger closeCountdown,
                                   TimeSpan opTime) {
            this.userCount = userCount;
            this.maxCreates = maxCreates;
            this.failed = failed;
            this.opCountdown = opCountdown;
            this.closeCountdown = closeCountdown;
            this.opTime = opTime;
        }

        @Override
        public DummySession createNullInstance() {
            return NULL;
        }

        @Override
        public DummySession create(Integer key) {
            if (key > userCount) {
                failed.get().add("Created user #" + key);
            }
            count++;
            if (count > maxCreates) {
                failed.get().add("Created >" + maxCreates + " sessions, key " + key + ": " + count);
            }
            DummySession dummySession = new DummySession(key, failed, opCountdown, closeCountdown, opTime);
            dummySession.open();
            return dummySession;
        }

        @Override
        public Integer key(DummySession value) {
            return value.getKey();
        }

        @Override
        public Integer destroy(DummySession victim) {
            Integer key = victim.getKey();
            victim.close();
            return key;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    private class DummySession implements Comparable<DummySession> {

        private final int key;

        private final AtomicReference<List<String>> failed;

        private final AtomicInteger opCountdown;

        private final AtomicInteger closeCountdown;

        private final TimeSpan opTime;

        private boolean open;

        private DummySession(int key,
                            AtomicReference<List<String>> failed,
                            AtomicInteger opCountdown,
                            AtomicInteger closeCountdown,
                            TimeSpan opTime) {
            this.key = key;
            this.failed = failed;
            this.opCountdown = opCountdown;
            this.closeCountdown = closeCountdown;
            this.opTime = opTime;
        }

        public DummySession touch() {
            if (!open) {
                failed.get().add(this + " received event while not open");
            }
            opCountdown.decrementAndGet();
            opTime.sleep(false);
            return this;
        }

        public void open() {
            if (open) {
                failed.get().add(this + " opened again");
            }
            open = true;
        }

        public DummySession close() {
            closeCountdown.decrementAndGet();
            open = false;
            return this;
        }

        @Override
        public String toString() {
            return "Session for " + key;
        }

        public Integer getKey() {
            return key;
        }

        @Override
        public int compareTo(DummySession o) {
            return key - o.key;
        }
    }

    private class Shutdown implements Callable<ExecutorService> {

        private final ExecutorService service;

        private final AtomicInteger countdown;

        private Shutdown(ExecutorService service,
                        AtomicInteger countdown) {
            this.service = service;
            this.countdown = countdown;
        }

        @Override
        public ExecutorService call() throws Exception {
            if (countdown == null || countdown.get() <= 0) {
                service.shutdown();
                TimeSpan.MINUTE.awaitTermination(service, false);
                return service;
            }
            return null;
        }
    }

    private class Task implements Runnable {

        private final ConcurrentSessions<Integer, DummySession> sessions;

        private final int id;

        private final ExecutorService service;

        private final AtomicReference<List<String>> failed;

        private Task(ConcurrentSessions<Integer, DummySession> sessions,
                    int id,
                    ExecutorService service,
                    AtomicReference<List<String>> failed) {
            this.sessions = sessions;
            this.id = id;
            this.service = service;
            this.failed = failed;
        }

        @Override
        public void run() {
            DummySession session;
            try {
                session = sessions.acquire(id, TimeSpan.MINUTE);
            } catch (RuntimeException e) {
                failed.get().add(e.toString());
                throw e;
            }
            try {
                if (session == null) {
                    service.execute(this);
                } else {
                    session.touch();
                }
            } finally {
                try {
                    sessions.release();
                } catch (Exception e) {
                    failed.get().add(session + " failed to release " + session + ": " + e);
                }
            }
        }
    }

    private class Scavenge implements Runnable {

        private final ConcurrentSessions<Integer, DummySession> sessions;

        private final Set<Boolean> okClose;

        private final AtomicInteger scavenges;

        private final AtomicReference<List<String>> failed;

        private Scavenge(ConcurrentSessions<Integer, DummySession> sessions,
                        Set<Boolean> okClose,
                        AtomicInteger scavenges,
                        AtomicReference<List<String>> failed) {
            this.sessions = sessions;
            this.okClose = okClose;
            this.scavenges = scavenges;
            this.failed = failed;
        }

        @Override
        public void run() {
            try {
                ScavengeResult<DummySession> result = sessions.scavenge();
                if (result != null) {
                    scavenges.incrementAndGet();
                    if (result.getApproximateRemaining() == 0) {
                        if (okClose != null) {
                            okClose.add(true);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                failed.get().add(e.toString());
            }
        }
    }

}
