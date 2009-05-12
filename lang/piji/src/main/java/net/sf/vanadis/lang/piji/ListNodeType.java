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

package net.sf.vanadis.lang.piji;

import net.sf.vanadis.core.collections.Generic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class ListNodeType {

    private static final Map<Character, ListNodeType> PARS = Generic.map();

    private static final ListNodeType REGULAR =
            new ListNodeType("REGULAR", '(', ')');

    public static final ListNodeType PRIVATE =
            new ListNodeType("PRIVATE", '{', '}');

    private static final ListNodeType[] PRIVATE_VALUES =
            new ListNodeType[]{REGULAR, PRIVATE};

    public static final List<ListNodeType> VALUES = Collections.unmodifiableList
            (Arrays.asList(PRIVATE_VALUES));

    public static ListNodeType get(char par) {
        return PARS.get(par);
    }

    private static int nextOrdinal;

    private final String name;

    private final char left;

    private final char right;

    private final int ordinal = nextOrdinal++;

    private ListNodeType(String name, char left, char right) {
        this.name = name;

        this.left = left;
        this.right = right;

        PARS.put(left, this);
        PARS.put(right, this);
    }

    public char opposite(char par) {
        if (par == this.left) {
            return this.right;
        }
        if (par == this.right) {
            return this.left;
        }
        throw new IllegalArgumentException
                (this + " doesn't know the opposite of " + par);
    }

    public String getName() {
        return this.name;
    }

    public char getLeft() {
        return this.left;
    }

    public char getRight() {
        return this.right;
    }

    @Override
    public String toString() {
        return "ListNodeType[" + this.left + this.right + "]";
    }

    @Override
    public int hashCode() {
        return this.ordinal;
    }

}
