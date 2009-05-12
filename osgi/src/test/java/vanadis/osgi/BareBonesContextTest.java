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
package vanadis.osgi;

import junit.framework.Assert;
import vanadis.core.collections.Generic;
import vanadis.osgi.impl.BareBonesContext;
import vanadis.osgi.impl.BonyReference;
import vanadis.osgi.impl.BonyRegistration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

public class BareBonesContextTest {

    private BareBonesContext context;

    interface FooService {

        void foo();
    }

    class FooServiceImpl implements FooService {

        private final String id;

        FooServiceImpl(String id) {
            this.id = id;
        }

        @Override
        public void foo() {
        }

        public String getId() {
            return id;
        }
    }

    @Before
    public void setup() {
        context = new BareBonesContext();
    }

    @Test
    public void register() {
        Assert.assertEquals(0, context.registrationCount());
        Registration<FooService> registration =
                context.register(new FooServiceImpl("1"),
                                 ServiceProperties.create(FooService.class, Generic.map("foo", "bar")));
        Reference<?> reference = context.getReference(FooService.class);
        Assert.assertEquals(1, context.registrationCount());
        BonyReference<FooService> storedReference = ((BonyRegistration<FooService>) registration).getReference();
        Assert.assertEquals(reference, storedReference);
        Assert.assertEquals(reference.getService(), storedReference.getService());
    }

    @Test
    public void listen() {
        Assert.assertEquals(0, context.listenerCount());
        AtomicReference<FooService> foo = new AtomicReference<FooService>();
        context.addContextListener(FooService.class, new AtomicListener(foo), null);
        Assert.assertNull(foo.get());
        context.register(new FooServiceImpl("foo"),
                         ServiceProperties.create(FooService.class));
        Assert.assertNotNull(foo.get());
        AtomicReference<FooService> foo2 = new AtomicReference<FooService>();
        context.addContextListener(FooService.class, new AtomicListener(foo2), null);
        Assert.assertNotNull(foo2.get());
    }

    @After
    public void teardown() {
        context = null;
    }

    private static class AtomicListener extends AbstractContextListener<FooService> {

        private final AtomicReference<FooService> foo;

        private AtomicListener(AtomicReference<FooService> foo) {
            this.foo = foo;
        }

        @Override
        public void serviceRegistered(Reference<FooService> fooServiceReference) {
            foo.set(fooServiceReference.getService());
        }

        @Override
        public void serviceUnregistering(Reference<FooService> fooServiceReference) {
            foo.set(null);
        }
    }
}
