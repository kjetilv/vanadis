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

import junit.framework.Assert;
import org.junit.Test;

public class ExpressionTest {

    @Test
    public void malformed() {
        try {
            Assert.fail(Expression.parse("(+ 2 2") + " should not work");
        } catch (IllegalArgumentException ignore) {
        }
        try {
            Assert.fail(Expression.parse("(+ 2 2) (") + " should not work");
        } catch (IllegalArgumentException ignore) {
        }
        try {
            Assert.fail(Expression.parse("(+ 2, 2)") + " should not work");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testPlus() {
        Assert.assertEquals
                (Expression.parse("(add 2 2)"), new Expression(Expression.Type.ADD, 2, 2));
        Assert.assertEquals
                (new Expression(Expression.Type.ADD, 2, 3), Expression.parse("(add 2 3)"));
    }

    @Test
    public void testSub() {
        Assert.assertEquals
                (Expression.parse("(sub 2 1)"), new Expression(Expression.Type.SUB, 2, 1));
    }

}
