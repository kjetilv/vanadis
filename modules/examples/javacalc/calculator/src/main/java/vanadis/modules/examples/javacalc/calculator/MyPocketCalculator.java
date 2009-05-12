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
package vanadis.modules.examples.javacalc.calculator;

import vanadis.core.lang.ToString;
import vanadis.modules.examples.javacalc.calcservices.*;

import java.util.Set;

public class MyPocketCalculator implements PocketCalculator {

    private final OperatorWiring operatorWiring;

    public MyPocketCalculator(OperatorWiring operatorWiring) {
        this.operatorWiring = operatorWiring;
    }

    @Override
    public int calculate(String expr) {
        Expression expression = Expression.parse(expr);
        System.out.println(this + " -> " + expression);
        Expression.Type type = expression.getType();
        int[] args = expression.getArgs();
        if (type == Expression.Type.ADD) {
            return oneOf(adders()).add(args);
        } else if (type == Expression.Type.DIV) {
            return oneOf(divisors()).div(args);
        } else if (type == Expression.Type.MUL) {
            return oneOf(multipliers()).mul(args);
        } else if (type == Expression.Type.SUB) {
            return oneOf(subtractors()).sub(args);
        }
        throw new IllegalArgumentException("Unknown operator: " + type);
    }

    private Set<Subtractor> subtractors() {
        return operatorWiring.getSubtractors();
    }

    @Override
    public String toString() {
        return ToString.of(this, operatorWiring);
    }

    private Set<Multiplier> multipliers() {
        return operatorWiring.getMultipliers();
    }

    private Set<Divisor> divisors() {
        return operatorWiring.getDivisors();
    }

    private Set<Adder> adders() {
        return operatorWiring.getAdders();
    }

    private static <T> T oneOf(Set<T> set) {
        return set.iterator().next();
    }

}
