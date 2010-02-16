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
package vanadis.modules.rmiprovider;

import junit.framework.TestCase;
import vanadis.common.io.Location;

public class RmiInfrastructureTest extends TestCase {

    public RmiInfrastructureTest(String name) {
        super(name);
    }

    public void testEndPoint() {
        RmiRemotingInfrastructure remoting1 = new RmiRemotingInfrastructure(new Location(2000));
        assertTrue(remoting1.isEndPoint());
        remoting1.destroyServer(false);
        RmiRemotingInfrastructure remoting2 = new RmiRemotingInfrastructure();
        assertFalse(remoting2.isEndPoint());
        remoting2.destroyServer(false);
    }
}
