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

package vanadis.annopro;

@Foo("extend-interface-impl")
public class ExtendedAnotherAnnotatedTypeImpl extends AnotherAnnotatedTypeImpl {

    @Baz(zot = "extends-field", zip = 3)
    private final Integer integer = 1;

    @Override
    @Inherited(f3 = 3)
    @Bar(zot = "extend-interface-impl", zip = 3)
    public Integer method(String object) {
        return object.hashCode();
    }

    @Bar(zot = "extends-exclusive")
    public void extended() {
    }

    public Integer getInteger() {
        return integer;
    }
}
