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

class VeryDummySessionFactory implements SessionFactory<Integer, VeryDummySession> {

    @Override
    public VeryDummySession createNullInstance() {
        return new VeryDummySession();
    }

    @Override
    public VeryDummySession create(Integer key) {
        return new VeryDummySession(key);
    }

    @Override
    public Integer key(VeryDummySession value) {
        return value.getKey();
    }

    @Override
    public Integer destroy(VeryDummySession victim) {
        return key(victim);
    }
}
