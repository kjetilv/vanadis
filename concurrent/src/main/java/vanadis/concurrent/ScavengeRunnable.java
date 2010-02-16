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

package vanadis.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vanadis.core.lang.ToString;
import vanadis.common.time.Time;
import vanadis.common.time.TimeSpan;

class ScavengeRunnable<V> implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ScavengeRunnable.class);

    private final Sessions<?, V> sessions;

    private final TimeSpan scavengeAge;

    private final TimeSpan scavengeInterval;

    private final ScavengeFailureListener<V> scavengeFailureListener;

    private int scavengesTotal;

    ScavengeRunnable(Sessions<?, V> sessions,
                     TimeSpan scavengeAge,
                     TimeSpan scavengeInterval,
                     ScavengeFailureListener<V> scavengeFailureListener) {
        this.sessions = sessions;
        this.scavengeAge = scavengeAge;
        this.scavengeInterval = scavengeInterval;
        this.scavengeFailureListener = scavengeFailureListener;
    }

    @Override
    public void run() {
        log.info(this + " starts scavenge...");
        runScavenge();
    }

    private void runScavenge() {
        Time checkpoint = Time.mark();
        int startCount = scavengesTotal;
        while (true) {
            ScavengeResult<V> scavengeResult;
            try {
                scavengeResult = scavenge();
                if (scavengeResult != null) {
                    scavengesTotal++;
                }
            } catch (Exception e) {
                scavengeFailureListener.failed(e);
                return;
            }
            if (complete(scavengeResult)) {
                logCompletedScavenge
                    (new ScavengeProgress<V>(scavengeResult, startCount, scavengesTotal));
                return;
            } else if (aMinutePassedSince(checkpoint)) {
                logStillScavenging
                    (new ScavengeProgress<V>(scavengeResult, startCount, scavengesTotal));
                checkpoint = Time.mark();
            }
        }
    }

    private boolean complete(ScavengeResult<V> result) {
        if (result == null) {
            return true;
        }
        V victim = result.getVictim();
        TimeSpan idleTime = TimeSpan.since(result.getLastEditTime());
        return victim == null || idleTime.isShorterThan(scavengeAge);
    }

    private static boolean aMinutePassedSince(Time checkpoint) {
        return TimeSpan.since(checkpoint).isLongerThan(TimeSpan.MINUTE);
    }

    private void logCompletedScavenge(ScavengeProgress<V> result) {
        String next = next();
        log.info(this + " completed " + result + ", " + next);
    }

    private void logStillScavenging(ScavengeProgress<V> result) {
        log.info(this + " still scavenging, current  " + result + ", " + next());
    }

    private String next() {
        return "next scavenge: " + Time.mark().after(scavengeInterval);
    }

    private ScavengeResult<V> scavenge() {
        try {
            return sessions.scavenge();
        } catch (Throwable e) {
            throw new ScavengeException(this + " encountered exception when scavenging", e);
        }
    }

    @Override
    public String toString() {
        return ToString.of(this, "scavenges", scavengesTotal,
                           "age", scavengeAge.toHumanFriendlyString(),
                           "interval", scavengeInterval);
    }
}
