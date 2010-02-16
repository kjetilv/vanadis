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
package vanadis.integrationtests;

import junit.framework.Assert;
import org.junit.Test;
import vanadis.common.io.Location;
import vanadis.common.io.Probe;

public class RemotingTest extends SingleFelixTestCase {

    @Test(timeout = 30000L)
    public void startRemoting() {
        session().startRemoting(2);
        Location remotingLocation = session().getLaunch().getLocation().incrementPort(1);
        Assert.assertTrue
                ("No activity detected at " + remotingLocation,
                 Probe.detectedActivity(remotingLocation));
    }
}
