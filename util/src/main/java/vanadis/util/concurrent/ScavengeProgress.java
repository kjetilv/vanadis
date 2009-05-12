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

package net.sf.vanadis.util.concurrent;

import net.sf.vanadis.core.lang.ToString;

final class ScavengeProgress<V> {

    private final ScavengeResult<V> result;

    private final int startCount;

    private final int endCount;

    ScavengeProgress(ScavengeResult<V> result, int startCount, int endCount) {
        this.result = result;
        this.startCount = startCount;
        this.endCount = endCount;
    }

    @Override
    public String toString() {
        int rounds = endCount - startCount;
        return ToString.of(this, "scavengeCount", rounds, "result", result);
    }
}
