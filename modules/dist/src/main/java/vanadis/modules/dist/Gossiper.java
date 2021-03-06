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
package vanadis.modules.dist;

import vanadis.osgi.Registration;
import vanadis.common.io.Location;
import vanadis.core.properties.PropertySets;
import org.jgroups.JChannel;

import java.util.concurrent.ScheduledExecutorService;

public class Gossiper extends ChannelPusher {

    private final Location endPoint;

    private static final String ENDPOINT = "endpoint";

    public Gossiper(ScheduledExecutorService service, JChannel channel, Location endPoint) {
        super(service, channel);
        this.endPoint = endPoint;
    }

    public void registrationReceived(Registration<?> registration) {
        push(registration.getServiceProperties().with
                (PropertySets.create(ENDPOINT, endPoint.toLocationString())));
    }

}
