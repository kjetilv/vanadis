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

package vanadis.util.concurrent;

import vanadis.core.lang.ToString;
import vanadis.core.time.Time;

public class ScavengeResult<V> {

    private final int approximateRemaining;

    private final V victim;

    private final Time lastEditTime;

    ScavengeResult(int approximateRemaining, V victim, Time lastEditTime) {
        this.approximateRemaining = approximateRemaining;
        this.victim = victim;
        this.lastEditTime = lastEditTime;
    }

    public int getApproximateRemaining() {
        return approximateRemaining;
    }

    public V getVictim() {
        return victim;
    }

    public Time getLastEditTime() {
        return lastEditTime;
    }

    @Override
    public String toString() {
        return ToString.of(this, "victim", victim, "remaining", approximateRemaining);
    }
}
