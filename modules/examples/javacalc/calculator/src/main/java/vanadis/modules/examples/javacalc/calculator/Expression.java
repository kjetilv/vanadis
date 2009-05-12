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
package vanadis.modules.examples.javacalc.calculator;

import vanadis.core.lang.EqHc;
import vanadis.core.lang.Not;

import java.util.Arrays;

final class Expression {

    private final Type type;

    private final int[] args;

    public static Expression parse(String string) {
        failMalformed(Not.nil(string, "string").trim());
        return parseDown(string);
    }

    private static Expression parseDown(String string) {
        String[] contents = string.substring(1, string.length() - 1).split("\\s+");
        Expression.Type type = Expression.Type.valueOf(contents[0].toUpperCase());
        int[] args = new int[contents.length - 1];
        for (int i = 0; i < args.length; i++) {
            args[i] = Integer.parseInt(contents[i + 1]);
        }
        return new Expression(type, args);
    }

    @Override
    public String toString() {
        return "(" + type + Arrays.toString(args) + ")";
    }

    private static void failMalformed(String string) {
        int parCount = 0;
        int charCount = 0;
        for (char c : string.toCharArray()) {
            charCount++;
            switch (c) {
                case '(':
                    parCount++;
                    break;
                case ')':
                    parCount--;
                    if (parCount == 0 && charCount < (string.length() - 1)) {
                        throw new IllegalArgumentException("Not a single expression: " + string);
                    }
                    break;
                case ',':
                    throw new IllegalArgumentException("Illegal char : " + c);
                default:
            }
        }
        if (parCount > 0) {
            throw new IllegalArgumentException("Unclosed parantheses: " + string);
        }
    }

    public enum Type {

        ADD, SUB, DIV, MUL
    }

    Expression(Type type, int... args) {
        this.type = type;
        this.args = args;
    }

    public Type getType() {
        return type;
    }

    public int[] getArgs() {
        return args;
    }

    @Override
    public int hashCode() {
        return EqHc.hc(new int[]{type.ordinal()}, args);
    }

    @Override
    public boolean equals(Object obj) {
        Expression expression = EqHc.retyped(this, obj);
        return expression != null &&
                EqHc.eq(expression.type, type) &&
                EqHc.eq(expression.args, args);
    }
}
