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

package vanadis.concurrent;

import vanadis.core.lang.Not;
import vanadis.core.lang.Proxies;
import vanadis.core.lang.ToString;
import vanadis.common.time.TimeSpan;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadedDispatch implements OperationQueuer {

    private Thread pickUpThread() {
        return timeout.waitForUnchecked(service.submit(new ThreadGetter()));
    }

    private final ScheduledExecutorService service;

    private final Thread thread;

    private final TimeSpan timeout;

    private final AtomicInteger depth = new AtomicInteger(0);

    public ThreadedDispatch(String name) {
        this(name, null);
    }

    public ThreadedDispatch(String name, TimeSpan timeout) {
        this.timeout = timeout == null ? TimeSpan.MINUTE : timeout;
        this.service = Executors.newSingleThreadScheduledExecutor(new SingleThreadFactory(name));
        this.thread = pickUpThread();
    }

    @Override
    public Thread getThread() {
        return thread;
    }

    @Override
    public TimeSpan getTimeout() {
        return timeout;
    }

    @Override
    public boolean inDispatchThread() {
        return Thread.currentThread() == thread;
    }

    @Override
    public void close() {
        if (inDispatchThread()) {
            throw new IllegalStateException(this + " cannot close itself!");
        }
        ExecutorUtils.terminate(this, service, timeout);
    }

    @Override
    public <T> T createAsynch(T instance, Class<T> type, Class<?>... moreClasses) {
        return createAsynch(instance, type, true, moreClasses);
    }

    @Override
    public <T> T createAsynch(T instance, Class<T> type, boolean reentrant, Class<?>... moreClasses) {
        ConcurrentInvocationHandler<T> handler = new ConcurrentInvocationHandler<T>
                (this, timeout,
                 Not.nil(type, "main type"),
                 Not.nil(instance, "instance"),
                 reentrant ? thread : null, true);
        return Proxies.genericProxy(type.getClassLoader(), type, handler, moreClasses);
    }

    @Override
    public <T> T createSynch(T instance, Class<T> type, Class<?>... moreClasses) {
        return Proxies.genericProxy
                (type.getClassLoader(), type,
                 new ConcurrentInvocationHandler<T>(this, timeout,
                                                    Not.nil(type, "main type"),
                                                    Not.nil(instance, "instance"),
                                                    thread, false),
                 moreClasses);
    }

    @Override
    public boolean synchUp() {
        return synchUp(timeout);
    }

    @Override
    public Future<?> submit(Invocation invocation) {
        try {
            return service.submit((Callable<?>) invocation);
        } finally {
            depth.incrementAndGet();
        }
    }

    @Override
    public void submitAndForget(Invocation invocation) {
        try {
            service.submit((Runnable) invocation);
        } finally {
            depth.incrementAndGet();
        }
    }

    @Override
    public void awaitShutdown(TimeSpan timeout) {
        timeout.awaitTermination(service, true);
    }

    boolean synchUp(TimeSpan timeout) {
        Future<Object> future = service.submit(new Synch());
        timeout.waitForUnchecked(future);
        return depth.get() == 0;
    }

    @Override
    public String toString() {
        return ToString.of(this, "timeout", timeout, "thread", thread, "depth", depth);
    }
}
