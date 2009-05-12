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
package net.sf.vanadis.modules.examples.javacalc.sub;

import net.sf.vanadis.modules.examples.javacalc.calcservices.AbstractArithmetics;
import net.sf.vanadis.modules.examples.javacalc.calcservices.Subtractor;

import java.util.Arrays;

public class MySubtractor extends AbstractArithmetics
        implements Subtractor {

    @Override
    public int sub(int... args) {
        System.out.println(this + " -> " + Arrays.toString(args));
        int remainder = args[0];
        for (int arg : subArgs(args)) {
            remainder -= arg;
        }
        return remainder;
    }
}
