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

import vanadis.util.lang.ToString;
import vanadis.util.time.TimeSpan;
import org.jgroups.JChannel;
import org.jgroups.Message;

import java.io.Serializable;
import java.util.concurrent.ScheduledExecutorService;

abstract class ChannelPusher {

    private final ScheduledExecutorService service;

    private final JChannel channel;

    ChannelPusher(ScheduledExecutorService service, JChannel channel) {
        this(service, channel, null, null);
    }

    protected ChannelPusher(ScheduledExecutorService service,
                            JChannel channel,
                            TimeSpan rate, Message message) {
        this.service = service;
        this.channel = channel;
        if (rate != null && message != null) {
            this.service.scheduleAtFixedRate
                    (new Push(channel, message), rate.time(), rate.time(), rate.unit());
        }
    }

    public void stop() {
        service.shutdown();
    }

    protected void push(Object message) {
        push(new Message(null, null, (Serializable) message));
    }

    protected void push(Message message) {
        push(message, null);
    }

    protected void push(Object message, TimeSpan delay) {
        push(new Message(null, null, (Serializable) message), delay);
    }

    protected void push(Message message, TimeSpan delay) {
        Push push = new Push(channel, message);
        if (delay == null || delay.isInstant()) {
            this.service.submit(push);
        } else {
            this.service.schedule(push, delay.time(), delay.unit());
        }
    }

    @Override
    public String toString() {
        return ToString.of(this, channel);
    }
}
