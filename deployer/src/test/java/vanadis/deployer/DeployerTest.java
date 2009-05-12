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
package vanadis.deployer;

import vanadis.core.time.TimeSpan;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URI;
import java.util.Collections;

@RunWith(JMock.class)
public class DeployerTest {

    private final Mockery mockery = new Mockery();

    @Test
    public void discoverFile() throws Exception {
        final URI uri = new File("newfile.jar").toURI();
        final Deploy deploy = mockery.mock(Deploy.class);
        final UriExplorer uriExplorer = mockery.mock(UriExplorer.class);
        final DiscoveredUris discoveredUris = mockery.mock(DiscoveredUris.class);
        ActiveDeployer activeDeployer = new ActiveDeployer(deploy, TimeSpan.HALF_MINUTE, uriExplorer, null);
        mockery.checking(new Expectations() {
            {
                one(uriExplorer).getType();
                will(returnValue("bundle"));
                one(uriExplorer).discover();
                will(returnValue(discoveredUris));
                one(discoveredUris).newsAndUpdates();
                will(returnValue(Collections.singletonList(uri)));
                one(discoveredUris).updatesAndRemoves();
                will(returnValue(Collections.emptyList()));
                one(deploy).deployBundle(uri);
            }
        });
        activeDeployer.runCycle();
    }
}
