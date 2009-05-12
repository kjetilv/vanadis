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

package net.sf.vanadis.annopro;

@Foo("interface-impl")
class AnotherAnnotatedTypeImpl implements AnotherAnnotatedType {

    @Baz(zot = "impl-field", zip = 2)
    private final Long loong = 1L;

    @Override
    @Inherited(f2 = 2)
    @Bar(zot = "interface-impl", zip = 2)
    public Number method(String object) {
        return object.hashCode();
    }

    @Bar(zot = "impl-exclusive")
    public void implemented() {
    }

    public Long getLoong() {
        return loong;
    }
}