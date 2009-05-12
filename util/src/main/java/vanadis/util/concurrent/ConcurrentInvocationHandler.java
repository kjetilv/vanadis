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

package vanadis.util.concurrent;

import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;
import vanadis.core.time.TimeSpan;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

class ConcurrentInvocationHandler<T> implements InvocationHandler {

    private final TimeSpan timeout;

    private final Class<T> type;

    private final T instance;

    private final OperationQueuer operationQueuer;

    private final Thread thread;

    private final AtomicBoolean closed = new AtomicBoolean();

    private final boolean asynch;

    ConcurrentInvocationHandler(OperationQueuer operationQueuer, TimeSpan timeout,
                                Class<T> type, T instance,
                                Thread thread, boolean asynch) {
        this.operationQueuer = Not.nil(operationQueuer, "operation queuer");
        this.timeout = Not.nil(timeout, "timeout");
        this.type = Not.nil(type, "type");
        this.instance = Not.nil(instance, "instance");
        this.thread = thread;
        this.asynch = asynch;
    }

    void close() {
        closed.set(true);
    }

    void failIfClosed(Method method) {
        if (closed.get()) {
            throw new IllegalStateException(this + " was closed, cannot accept call to " + method.getName());
        }
    }

    protected final Class<T> getType() {
        return type;
    }

    protected final T getInstance() {
        return instance;
    }

    final TimeSpan getTimeout() {
        return timeout;
    }

    final <T> T waitFor(Future<T> future)
            throws Throwable {
        try {
            return getTimeout().waitFor(future);
        } catch (ExecutionException e) {
            throw e.getCause();
        } catch (TimeoutException e) {
            throw new AsynchException(this + " timed out on " + future, e);
        } catch (InterruptedException e) {
            throw new AsynchException(this + " interrupted", e);
        }
    }

    final boolean stayInThread() {
        return thread != null && Thread.currentThread() == thread;
    }

    final void runAndForget(Invocation invocation) {
        operationQueuer.submitAndForget(invocation);
    }

    final Future<?> run(Invocation invocation) {
        return operationQueuer.submit(invocation);
    }

    @Override
    public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        failIfClosed(method);
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
        Invocation invocation = new Invocation(instance, method, args);
        if (stayInThread()) {
            return invocation.execute();
        }
        if (asynch) {
            if (isVoid(method)) {
                runAndForget(invocation);
                return null;
            }
            throw new IllegalStateException
                    (method + " returns " + method.getReturnType() + ", cannot call on " + invocation +
                            " asynchronously!");
        }
        return waitFor(run(invocation));
    }

    private static boolean isVoid(Method method) {
        Class<?> type = method.getReturnType();
        return type == Void.class || type == void.class || type == null;
    }

    @Override
    public String toString() {
        return ToString.of(this, "thread", thread, "dispatch", operationQueuer);
    }
}
