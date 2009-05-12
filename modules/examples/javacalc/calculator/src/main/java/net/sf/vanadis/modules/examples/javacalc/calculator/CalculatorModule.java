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
package net.sf.vanadis.modules.examples.javacalc.calculator;

import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.ext.AbstractModule;
import net.sf.vanadis.ext.Module;
import net.sf.vanadis.modules.examples.javacalc.calcservices.*;

import java.util.Set;

@Module(moduleType = "javacalc-calculator")
public class CalculatorModule extends AbstractModule
        implements OperatorWiring {

    private final Set<Adder> adders = Generic.synchSet();

    private final Set<Subtractor> subtractors = Generic.synchSet();

    private final Set<Multiplier> multipliers = Generic.synchSet();

    private final Set<Divisor> divisors = Generic.synchSet();

    private MyPocketCalculator calc;

    @Override
    public PocketCalculator getPocketCalculator() {
        if (calc == null) {
            calc = new MyPocketCalculator(this);
        }
        return calc;
    }

    @Override
    public Set<Adder> getAdders() {
        return adders;
    }

    @Override
    public Set<Subtractor> getSubtractors() {
        return subtractors;
    }

    @Override
    public Set<Multiplier> getMultipliers() {
        return multipliers;
    }

    @Override
    public Set<Divisor> getDivisors() {
        return divisors;
    }

    @Override
    public String toString() {
        return ToString.of(this,
                           "adds", adders.size(),
                           "subs", subtractors.size(),
                           "divs", divisors.size(),
                           "muls", multipliers.size());
    }

    @Override
    public void dependenciesLost() {
        System.out.println(this + " became unresolved!");
    }
}
