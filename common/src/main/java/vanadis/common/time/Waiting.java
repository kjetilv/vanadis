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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;

public interface Waiting {

    boolean waitFor(Condition condition);

    boolean waitFor(Condition condition, boolean failOnInterrupt);

    boolean waitOn(Object lock);

    boolean waitOn(Object lock, boolean failOnInterrupt);

    <T> T waitFor(Future<T> future)
        throws ExecutionException, TimeoutException, InterruptedException;

    <T> T waitForUnchecked(Future<T> future)
        throws ExecutionRuntimeException, TimeoutRuntimeException, InterruptedRuntimeException;

    boolean sleep();
}
