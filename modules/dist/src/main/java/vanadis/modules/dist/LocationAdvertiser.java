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

import vanadis.core.io.Location;
import vanadis.core.lang.ToString;
import vanadis.core.time.TimeSpan;
import org.jgroups.JChannel;
import org.jgroups.Message;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ScheduledExecutorService;

class LocationAdvertiser extends ChannelPusher {

    private static final String ENC = "UTF-8";

    private static Message createMessage(Location endPoint) {
        byte[] payload;
        try {
            payload = endPoint.toLocationString().getBytes(ENC);
        } catch (UnsupportedEncodingException e) {
            throw new Error("Unsupported encoding: " + ENC, e);
        }
        return new Message(null, null, payload);
    }

    LocationAdvertiser(ScheduledExecutorService service,
                       JChannel channel,
                       TimeSpan advertiseInterval,
                       Location endPoint) {
        super(service, channel, advertiseInterval, createMessage(endPoint));
    }

    @Override
    public String toString() {
        return ToString.of(this);
    }

}
