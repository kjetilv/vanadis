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
package vanadis.modules.examples.javacalc.add;

import vanadis.modules.examples.javacalc.calcservices.AbstractArithmetics;
import vanadis.modules.examples.javacalc.calcservices.Adder;

import java.util.Arrays;

public class MyAdder extends AbstractArithmetics implements Adder {

    @Override
    public int add(int... args) {
        System.out.println(this + " -> " + Arrays.toString(args));
        int sum = 0;
        for (int arg : args) {
            sum += arg;
        }
        return sum;
    }
}
