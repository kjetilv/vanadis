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
package net.sf.vanadis.modules.rmiprovider;

import junit.framework.Assert;
import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.io.Closeables;
import net.sf.vanadis.core.io.Location;
import net.sf.vanadis.remoting.MapTargetReference;
import net.sf.vanadis.remoting.RemotingImpl;
import net.sf.vanadis.remoting.TestTarget;
import net.sf.vanadis.remoting.TestTargetImpl;
import net.sf.vanadis.services.remoting.Remoting;
import net.sf.vanadis.services.remoting.TargetHandle;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class ClientTest extends Assert {

    private Remoting remoting;

    private static final Location LOCATION = new Location("127.0.0.1", 2000);

    @Test
    public void dummy() {

    }

    @Ignore
    @Test
    public void createClient() {
        TargetHandle<TestTarget> targetHandle = new TargetHandle<TestTarget>
                (LOCATION, new MapTargetReference<TestTarget>("foo", TestTarget.class));
        TestTarget target = remoting.connect(targetHandle);
        assertNotNull(target);
        assertEquals("world", target.hello("world", true));
    }

    @Test
    @Ignore
    public void setupServer()
            throws RemoteException {
        assertTrue(remoting.isEndPoint());
        assertTrue(remoting.isActive());
        assertNotNull(LocateRegistry.getRegistry(2000));
    }

    @Before
    public void setUp() {
        remoting = null;
        remoting = new RemotingImpl
                (new RmiRemotingInfrastructure(LOCATION),
                 new RmiRemoteClientFactory()).addLocator
                (Generic.map("foo", new TestTargetImpl()));
    }

    @After
    public void tearDown()
            throws Exception {
        Closeables.closeOrFail(remoting);
        remoting = null;
    }
}
