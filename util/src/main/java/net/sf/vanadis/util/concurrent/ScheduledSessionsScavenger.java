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

package net.sf.vanadis.util.concurrent;

import net.sf.vanadis.core.lang.Not;
import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.core.time.TimeSpan;
import net.sf.vanadis.util.log.Log;
import net.sf.vanadis.util.log.Logs;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ScheduledSessionsScavenger<V> implements SessionsScavenger<V> {

    private static final Log log = Logs.get(ScheduledSessionsScavenger.class);

    private final Sessions<?, V> sessions;

    private final ScheduledExecutorService service =
            Executors.newSingleThreadScheduledExecutor();

    public ScheduledSessionsScavenger(Sessions<?, V> sessions,
                                      TimeSpan scavengeInterval,
                                      TimeSpan scavengeAge,
                                      ScavengeFailureListener<V> scavengeFailureListener) {
        Not.nil(scavengeInterval, "scavenge interval");
        this.sessions = Not.nil(sessions, "sessions");
        log.info(this + " scavenges at intervals of " + scavengeInterval.toHumanFriendlyString() +
                ", and idlers above " + scavengeAge.toHumanFriendlyString());
        service.scheduleWithFixedDelay
                (new ScavengeRunnable<V>(sessions, scavengeAge, scavengeInterval, scavengeFailureListener),
                 scavengeInterval.time(),
                 scavengeInterval.time(), scavengeInterval.unit());
    }

    @Override
    public ScavengeResult<V> scavenge() {
        return sessions.scavenge();
    }

    @Override
    public void stop(TimeSpan timeout) {
        service.shutdown();
        boolean shutdown = false;
        try {
            shutdown = timeout.awaitTermination(service, true);
        } finally {
            if (!shutdown) {
                service.shutdownNow();
            }
        }
    }

    @Override
    public String toString() {
        return ToString.of(this, "sessions", sessions, "service", service);
    }
}
