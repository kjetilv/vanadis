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

package vanadis.extrt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vanadis.core.collections.Generic;
import vanadis.core.lang.ToString;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

final class DependencyTracker<T extends ManagedFeature<?,?>> implements Iterable<T> {

    private static final Logger log = LoggerFactory.getLogger(DependencyTracker.class);

    private final Set<String> complete = Generic.set();

    private final Set<String> requiredIncomplete = Generic.set();

    private final Set<String> incomplete = Generic.set();

    private final Map<String, T> trackees = Generic.map();

    public T track(T trackee) {
        if (trackee.isComplete()) {
            throw new IllegalStateException(this + " is already complete");
        }
        String name = trackee.getFeatureName();
        if (trackees.containsKey(name)) {
            throw new IllegalArgumentException
                    (this + " failed to setup injectorDependencyTracker, duplicate injection: " + name);
        }

        trackees.put(name, trackee);
        if (trackee.isRequired()) {
            requiredIncomplete.add(name);
        }
        incomplete.add(name);
        return trackee;
    }


    public boolean isTracking(String featureName) {
        return trackees.containsKey(featureName);
    }

    public void progress(String name) {
        checkArgument(name);
        if (incomplete.contains(name) && trackee(name).isComplete()) {
            incomplete.remove(name);
            requiredIncomplete.remove(name);
            complete.add(name);
            if (log.isDebugEnabled()) {
                log.debug(this + " completed " + name);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug(this + " progressing: " + name);
            }
        }
    }

    public void setback(String name) {
        checkArgument(name);
        T trackee = trackee(name);
        if (complete.contains(name) && !trackee.isComplete()) {
            complete.remove(name);
            incomplete.add(name);
            if (trackee.isRequired()) {
                requiredIncomplete.add(name);
            }
        }
    }

    @Override
    public Iterator<T> iterator() {
        return trackees.values().iterator();
    }

    public Iterable<T> requiredIncomplete() {
        return trackees(requiredIncomplete);
    }

    public Iterable<T> incomplete() {
        return trackees(incomplete);
    }

    public Iterable<T> complete() {
        return trackees(complete);
    }

    public Collection<String> completeNames() {
        return complete;
    }

    public boolean isRequiredComplete() {
        return requiredIncomplete.isEmpty();
    }
    
    public void reset() {
        complete.clear();
        requiredIncomplete.clear();
        incomplete.clear();
        trackees.clear();
    }

    private T trackee(String name) {
        return trackees.get(name);
    }

    private Iterable<T> trackees(Collection<String> names) {
        Collection<T> inc = Generic.list(names.size());
        for (String name : names) {
            inc.add(trackee(name));
        }
        return inc;
    }

    private void checkArgument(String name) {
        if (!trackees.containsKey(name)) {
            throw new IllegalStateException("Unknown property: " + name);
        }
    }

    @Override
    public String toString() {
        return ToString.of(this, "tracking", trackees.keySet(),
                           "complete", complete.size(),
                           "requiredIncomplete", requiredIncomplete.size(),
                           "incomplete", incomplete.size());
    }
}
