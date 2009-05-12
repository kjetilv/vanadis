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
package net.sf.vanadis.modules.dist;

import net.sf.vanadis.util.log.Log;
import net.sf.vanadis.util.log.Logs;
import org.jgroups.Channel;
import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelNotConnectedException;
import org.jgroups.Message;

public final class Push implements Runnable {

    private static final Log log = Logs.get(Push.class);

    private final Channel channel;

    private final Message message;

    public Push(Channel channel, Message message) {
        this.channel = channel;
        this.message = message;
    }

    @Override
    public void run() {
        try {
            channel.send(message);
        } catch (ChannelNotConnectedException e) {
            log.warn(this + " failed to send " + message + " on " + channel, e);
        } catch (ChannelClosedException e) {
            log.warn(this + " failed to send " + message + " on " + channel, e);
        }
    }
}
