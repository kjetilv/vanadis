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
package vanadis.remoting;

public final class TargetMatch<T> {

    private final Session session;

    private final T target;

    public static <T> TargetMatch<T> create(Session session, T target) {
        return new TargetMatch<T>(session, target);
    }

    private TargetMatch(Session session, T target) {
        this.session = session;
        this.target = target;
    }

    public T getTarget() {
        return target;
    }

    public Session getSession() {
        return session;
    }

}
