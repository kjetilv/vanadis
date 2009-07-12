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
package vanadis.concurrent;

import junit.framework.TestCase;
import vanadis.core.io.Closeables;
import vanadis.core.time.TimeSpan;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentTest extends TestCase implements Closeable {

    private final AtomicInteger atomicInteger = new AtomicInteger();

    private OperationQueuer operationQueuer;

    public ConcurrentTest(String name) {
        super(name);
    }

    @Override
    protected void setUp()
            throws Exception {
        super.setUp();
        atomicInteger.set(0);
        operationQueuer = new ThreadedDispatch(getName());
    }

    @Override
    protected void tearDown()
            throws Exception {
        Closeables.close(this);
        super.tearDown();
    }

    public interface RunMe {

        int add(int x, int y);

        void add(int x, int y, AtomicInteger atomicInteger);
    }

    public void testConcurrentAsynch() {
        RunMe runMe = operationQueuer.createAsynch(new RunMeImpl(), RunMe.class, true);
        runMe.add(1, 1, atomicInteger);
        synchronized (atomicInteger) {
            int waits = 100;
            while (atomicInteger.get() != 2 && waits-- > 0) {
                TimeSpan.ms(10).waitOn(atomicInteger);
            }
        }
        assertEquals(2, atomicInteger.get());
    }

    private static class RunMeImpl implements RunMe {

        @Override
        public int add(int x, int y) {
            return x + y;
        }

        @SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter"})
        @Override
        public void add(int x, int y, AtomicInteger atomicInteger) {
            atomicInteger.set(x + y);
            synchronized (atomicInteger) {
                atomicInteger.notifyAll();
            }
        }
    }

    @Override
    public void close() {
        Closeables.close(operationQueuer);
    }

}
