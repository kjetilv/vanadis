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

import vanadis.core.time.TimeSpan;

import java.io.Closeable;
import java.util.concurrent.Future;

public interface OperationQueuer extends Closeable {

    Thread getThread();

    TimeSpan getTimeout();

    boolean inDispatchThread();

    <T> T createAsynch(T instance, Class<T> type, Class<?>... moreClasses);

    <T> T createAsynch(T instance, Class<T> type, boolean reentrant, Class<?>... moreClasses);

    <T> T createSynch(T instance, Class<T> type, Class<?>... moreClasses);

    Future<?> submit(Invocation invocation);

    void submitAndForget(Invocation invocation);

    boolean synchUp();

    void awaitShutdown(TimeSpan timeout);
}
