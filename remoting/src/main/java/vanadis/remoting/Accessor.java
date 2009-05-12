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
package vanadis.remoting;

import vanadis.core.collections.Generic;
import vanadis.core.lang.Not;
import vanadis.services.remoting.TargetReference;
import vanadis.util.log.Log;
import vanadis.util.log.Logs;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class Accessor {

    private static final Log log = Logs.get(Accessor.class);

    public static final Map<String, TypeEnumerator> ENUMS = Generic.weakHashMap();

    private static <T> Session computeSession(T target) {
        return new Session(target.getClass().getName() + "@" + System.identityHashCode(target));
    }

    private final List<Object> locators = Generic.synchList();

    private final Map<Session, Object> targets = Generic.weakHashMap();

    private static final Accessor SINGLETON = new Accessor();

    public static Accessor getSingleton() {
        return SINGLETON;
    }

    @SuppressWarnings({"unchecked"})
    <T, L> TargetMatch<T> get(Session session, TargetReference<T, L> reference) {
        if (session != null) {
            Object target = targets.get(session);
            if (target != null) {
                return TargetMatch.create(session, (T) target);
            }
        }
        for (Object locator : locators) {
            Class<L> lookupClass = reference.getLookupClass();
            if (lookupClass.isInstance(locator)) {
                L lLocator = lookupClass.cast(locator);
                T target = reference.get(lLocator);
                if (target != null) {
                    Session newSession = computeSession(target);
                    targets.put(newSession, target);
                    return TargetMatch.create(newSession, target);
                }
            }
        }
        log.warn(reference + " could not be matched to a target, locators: " + locators);
        return null;
    }

    public void registerAccessPoint(Object locator) {
        locators.add(locator);
    }

    private Accessor() {
    }

    @SuppressWarnings({"unchecked"})
    static TypeEnumerator get(Class<?> type) {
        return ENUMS.get(type.getName());
    }

    static int indexOf(Class<?> clazz, Method method) {
        return enumerate(clazz).indexOf(method);
    }

    static Method methodNo(Class<?> clazz, int index) {
        return enumerate(clazz).method(index);
    }

    static <T> TypeEnumerator enumerate(Class<T> type) {
        if (!Not.nil((Class<?>) type, "type").isInterface()) {
            throw new IllegalArgumentException("Only interface types allowed, could not process " + type);
        }
        if (ENUMS.containsKey(type.getName())) {
            return get(type);
        } else {
            TypeEnumerator enumerator = new TypeEnumerator(type);
            ENUMS.put(type.getName(), enumerator);
            return enumerator;
        }
    }
}
