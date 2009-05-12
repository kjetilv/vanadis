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

package vanadis.lang.piji;

import vanadis.core.collections.Generic;

import java.util.Map;

public final class Symbol {

    private static final Map<String, Symbol> symbols = Generic.map();

    private final String name;

    private final int hashCode;

    public final String getName() {
        return this.name;
    }

    public static Symbol get(String name) {
        if (name == null) {
            throw new NullPointerException("Null symbol name given");
        }
        synchronized (symbols) {
            if (symbols.containsKey(name)) {
                return symbols.get(name);
            } else {
                Symbol newSymbol = new Symbol(name);
                symbols.put(name, newSymbol);
                return newSymbol;
            }
        }
    }

    private Symbol(String name) {
        this.name = name;
        this.hashCode = 17 + 37 * name.hashCode();
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object object) {
        return this == object;
    }

}
