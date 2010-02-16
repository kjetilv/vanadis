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
package vanadis.modules.httpprovider;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import vanadis.core.collections.Generic;
import vanadis.common.io.Location;
import vanadis.core.lang.Proxies;
import vanadis.common.time.Time;
import vanadis.common.time.TimeSpan;
import vanadis.remoting.Accessor;
import vanadis.remoting.MapTargetReference;
import vanadis.services.remoting.TargetHandle;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class HttpTest extends Assert {

    private HttpRemotingInfrastructure infrastructure;

    private File path;

    @Ignore
    @SuppressWarnings({"RawUseOfParameterizedType"})
    @Test
    public void setup()
            throws IOException, ClassNotFoundException {
        List<Object> target = Generic.list();
        target.add("1");
        Accessor.getSingleton().registerAccessPoint(Generic.map("foo", target));

        Location location = infrastructure.getLocation();
        assertNotNull(location);
        int port = location.getPort();
        assertTrue("Port: " + port, port > 0);

        Handler<List> handler = new Handler<List>
                (new TargetHandle<List>(location, new MapTargetReference<List>("foo", List.class)),
                 getClass().getClassLoader());
        List list = Proxies.genericProxy(getClass().getClassLoader(), List.class, handler);
        assertEquals(1, list.size());
        target.add("2");
        assertEquals(2, list.size());
    }

    @Before
    public void before()
            throws IOException {
        path = File.createTempFile("foobar" + Time.mark().getEpoch(), "tmp");
        infrastructure = new HttpRemotingInfrastructure(null, TimeSpan.HALF_MINUTE, 1, 1, true);
        infrastructure.createServer(getClass().getClassLoader());
        TimeSpan.seconds(2).sleep();
    }

    @After
    public void after() {
        infrastructure.destroyServer(true);
        infrastructure = null;
        path.delete();
    }
}
