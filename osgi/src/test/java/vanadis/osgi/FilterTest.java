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
package vanadis.osgi;

import junit.framework.Assert;
import vanadis.core.collections.Generic;
import vanadis.core.properties.PropertySets;
import vanadis.core.test.VAsserts;
import static vanadis.osgi.Filters.*;
import org.junit.Test;

public class FilterTest {

    private static void assertFilter(String string, Filter filter) {
        Assert.assertEquals(string, filter.toFilterString());
        VAsserts.assertEqHcMatch(filter, filter);
    }

    @Test
    public void testNoopAnd() {
        Filter expr1 = eq("foo", 5);
        assertFilter("(foo=5)", expr1);
        Filter anded = expr1.and();
        assertFilter("(foo=5)", anded);
        Filter anded2 = expr1.and(NULL, NULL);
        assertFilter("(foo=5)", anded2);
    }

    @Test
    public void testNoopOr() {
        Filter expr1 = eq("foo", 5);
        assertFilter("(foo=5)", expr1);
        Filter ored = expr1.or();
        assertFilter("(foo=5)", ored);
        Filter ored2 = expr1.or(NULL, NULL);
        assertFilter("(foo=5)", ored2);
    }

    @Test
    public void testAnd() {
        Filter expr1 = eq("foo", 5);
        Filter expr2 = eq("bar", 6);
        Filter and = expr1.and(expr2);
        assertFilter("(&(foo=5)(bar=6))", and);
        assertFilterEquals(and, expr1.and(expr2));
    }

    @Test
    public void testMultiAnd() {
        Filter expr1 = eq("foo", 5);
        Filter expr2 = eq("bar", 6);
        Filter expr3 = eq("zot", 7);
        Filter and = expr1.and(expr2);
        assertFilter("(&(foo=5)(bar=6))", and);
        assertFilterEquals(and, expr1.and(expr2));
        Filter and2 = and.and(expr3);
        assertFilter("(&(foo=5)(bar=6)(zot=7))", and2);
        assertFilterEquals(and2, expr1.and(expr2).and(expr3));
    }

    @Test
    public void testMoreMultiStuff() {
        Filter expr1 = eq("foo", 5);
        Filter expr2 = eq("bar", 6);
        Filter expr3 = eq("zot", 7);
        Filter and = expr1.and(expr2);
        assertFilter("(&(foo=5)(bar=6)(zot=7))", and.and(expr3));
        assertFilter("(|(&(foo=5)(bar=6))(zot=7))", and.or(expr3));
    }

    @Test
    public void testMultiOr() {
        Filter expr1 = eq("foo", 5);
        Filter expr2 = eq("bar", 6);
        Filter expr3 = eq("zot", 7);
        Filter or = expr1.or(expr2);
        assertFilter("(|(foo=5)(bar=6))", or);
        assertFilterEquals(or, expr1.or(expr2));
        Filter or2 = or.or(expr3);
        assertFilter("(|(foo=5)(bar=6)(zot=7))", or2);
        assertFilterEquals(or2, expr1.or(expr2).or(expr3));
    }

    private static void assertFilterEquals(Filter one, Filter two) {
        Assert.assertEquals(one, two);
        VAsserts.assertEqHcMatch(one, two);
    }

    @Test
    public void testEqHc() {
        Filter expr1 = eq("foo", 5);
        Filter expr2 = eq("bar", 6);
        Filter and = expr1.and(expr2);
        VAsserts.assertEqHcMatch(and, expr1.and(expr2));
    }

    @Test
    public void simpleNegationOfNot() {
        Filter filter = Filters.eq("foo", "bar");
        assertFilter("(foo=bar)", filter);
        Assert.assertTrue(filter.matches
                (ServiceProperties.create(Object.class, PropertySets.create("foo", "bar"))));
        Filter negated = filter.not();
        assertFilter("(!(foo=bar))", negated);
        Assert.assertTrue(negated.matches
                (ServiceProperties.create(Object.class, PropertySets.create("foo", "zot"))));
        Filter nenegated = negated.not();
        assertFilter("(foo=bar)", nenegated);
        Assert.assertTrue(nenegated.matches
                (ServiceProperties.create(Object.class, PropertySets.create("foo", "bar"))));
    }

    @Test
    public void simpleNegationOfCompare() {
        Filter filter = Filters.isFalse("foo");
        assertFilter("(foo=false)", filter);
        Assert.assertTrue(filter.matches
                (ServiceProperties.create(Object.class, PropertySets.create("foo", false))));
        Filter negated = filter.not();
        assertFilter("(foo=true)", negated);
        Assert.assertTrue(negated.matches
                (ServiceProperties.create(Object.class, PropertySets.create("foo", true))));
    }

    @Test
    public void testOr() {
        Filter expr1 = lt("foo", 5);
        Filter expr2 = gt("bar", 6);
        Filter or = expr1.or(expr2);
        assertFilter("(|(foo<=5)(bar>=6))", or);
        assertFilterEquals(or, expr1.or(expr2));
    }

    @Test
    public void testNull() {
        Filter or = lt("foo", 5).or(gt("bar", 6));
        Filter orNulled = lt("foo", 5).or(NULL, gt("bar", 6));
        Filter orNulledAgain = lt("foo", 5).or(NULL, gt("bar", 6), NULL);
        Assert.assertEquals(or, orNulled);
        Assert.assertEquals(or, orNulledAgain);
        Assert.assertEquals(orNulled, orNulledAgain);
        assertNullBehavior(or);
        Filter and = lt("foo", 5).and(gt("bar", 6));
        Filter andNulled = lt("foo", 5).and(NULL, gt("bar", 6));
        Assert.assertEquals(and, andNulled);
        assertNullBehavior(and);
        assertFilterEquals(NULL, NULL.not());
    }

    private void assertNullBehavior(Filter expr) {
        assertFilterEquals(expr, expr.and(NULL));
        assertFilterEquals(expr, expr.or(NULL));
        assertFilterEquals(expr, expr.or(NULL, NULL));
        assertFilterEquals(expr, NULL.and(expr.or(NULL, NULL)));
//        assertFilterEquals(expr.and(expr), NULL.and(expr.or(NULL, NULL), expr));
        assertFilterEquals(expr, NULL.and(expr));
        assertFilterEquals(expr, NULL.and(expr, NULL));
        assertFilterEquals(expr, NULL.or(expr));
        assertFilterEquals(expr, NULL.or(expr, NULL));
        //assertFilterEquals(expr.or(expr), NULL.or(expr, NULL, expr));
    }

    @Test
    public void testNot() {
        Filter expr = lt("foo", 5);
        Filter not = expr.not();
        assertFilter("(!(foo<=5))", not);
        assertFilterEquals(not, expr.not());
    }

    @Test
    public void testApprox() {
        Filter approx = approx("foo", 5);
        assertFilter("(foo~=5)", approx);
    }

    @Test
    public void testPresent() {
        assertFilter("(foo=*)", present("foo"));
    }

    @Test
    public void testSubstring() {
        assertFilter("(foo=*ab*)", substring("foo", "*ab*"));
    }

    @Test
    public void testObjectClasses() {
        assertFilter("(objectclass=java.lang.String)", objectClasses(String.class));
        assertFilter("(objectclass=[java.lang.String,java.lang.Integer])", objectClasses(String.class, Integer.class));
    }

    @Test
    public void testBooleanShortHandsAnd() {
        Assert.assertEquals(Filters.isTrue("foo").and(Filters.isFalse("bar")),
                            Filters.isTrue("foo").andNot("bar"));
        Assert.assertEquals(Filters.isTrue("foo").and(Filters.isTrue("bar")),
                            Filters.isTrue("foo").and("bar"));
    }

    @Test
    public void testBooleanShortHandsOr() {
        Assert.assertEquals(Filters.isTrue("foo").or(Filters.isFalse("bar")),
                            Filters.isTrue("foo").orNot("bar"));
        Assert.assertEquals(Filters.isTrue("foo").or(Filters.isTrue("bar")),
                            Filters.isTrue("foo").or("bar"));
    }

    @Test
    public void testArrayValue() {
        assertFilter("(foo=[5,6])", eq("foo", 5, 6));
    }

    @Test
    public void testLessThanMatch() {
        Assert.assertTrue(lt("foo", 5).matches(ServiceProperties.create(Object.class, Generic.map("foo", 2))));
        Assert.assertFalse(lt("foo", 5).matches(ServiceProperties.create(Object.class, Generic.map("foo", 7))));
    }

    @Test
    public void testGreaterThanMatch() {
        Assert.assertFalse(gt("foo", 5).matches(ServiceProperties.create(Object.class, Generic.map("foo", 2))));
        Assert.assertTrue(gt("foo", 5).matches(ServiceProperties.create(Object.class, Generic.map("foo", 7))));
    }

    @Test
    public void testBooleanMatch() {
        Assert.assertTrue(isTrue("foo").matches(ServiceProperties.create(Object.class, Generic.map("foo", true))));
        Assert
                .assertTrue(
                        isTrue("foo").not().matches(ServiceProperties.create(Object.class, Generic.map("foo", false))));
        Assert.assertTrue(isFalse("foo").matches(ServiceProperties.create(Object.class, Generic.map("foo", false))));
    }

    @Test
    public void testEqualsMatch() {
        Assert.assertFalse(eq("foo", 5).matches(ServiceProperties.create(Object.class, Generic.map("foo", 2))));
        Assert.assertTrue(eq("foo", 5).matches(ServiceProperties.create(Object.class, Generic.map("foo", 5))));
    }

    @Test
    public void testPresentMatch() {
        Assert.assertTrue(present("foo").matches(ServiceProperties.create(Object.class, Generic.map("foo", "bar"))));
        Assert.assertFalse(present("foo").matches(ServiceProperties.create(Object.class, Generic.map("fox", "bar"))));
    }

    @Test
    public void testSubstringMatch() {
        Assert.assertTrue(substring("foo", "bar").matches
                (ServiceProperties.create(Object.class, Generic.map("foo", "babarian"))));
        Assert.assertFalse(substring("foo", "bar").matches
                (ServiceProperties.create(Object.class, Generic.map("fox", "babazian"))));
    }

    @Test
    public void testNotMatch() {
        Assert.assertTrue(present("foo").not().matches(ServiceProperties.create(Object.class)));
        Assert.assertFalse(present("foo").not().matches(ServiceProperties.create(Object.class, Generic.map("foo", 2))));
    }

    @Test
    public void testAndMatch() {
        Assert.assertTrue(present("foo").and(gt("zip", 5)).matches
                (ServiceProperties.create(Object.class, Generic.map("foo", "bar", "zip", 10))));
        Assert.assertFalse(present("foo").and(gt("zip", 5)).matches
                (ServiceProperties.create(Object.class, Generic.map("foz", "bar", "zip", 10))));
        Assert.assertFalse(present("foo").and(gt("zip", 5)).matches
                (ServiceProperties.create(Object.class, Generic.map("foz", "bar", "zip", 2))));
    }

    @Test
    public void testOrMatch() {
        Assert.assertTrue(present("foo").or(gt("zip", 5)).matches
                (ServiceProperties.create(Object.class, Generic.map("foo", "bar"))));
        Assert.assertTrue(present("foo").or(gt("zip", 5)).matches
                (ServiceProperties.create(Object.class, Generic.map("zip", 10))));
        Assert.assertFalse(present("foo").or(gt("zip", 5)).matches
                (ServiceProperties.create(Object.class, Generic.map("zip", 2))));
    }

    @Test
    public void testNullIsNull() {
        Assert.assertTrue(Filters.NULL.and(Filters.NULL).isNull());
        Assert.assertTrue(Filters.NULL.or(Filters.NULL).isNull());
        Assert.assertTrue(Filters.NULL.not().isNull());
    }

    @Test
    public void testFilterBuilding() {
        Filter filter = Filters.eq("foo", "bar").and(Filters.eq("zot", "zip"));
        Assert.assertEquals
                ("(&(foo=bar)(zot=zip))",
                 filter.toFilterString());
        Assert.assertEquals("(&(foo=bar)(zot=zip)(hip=hop))",
                            filter.and(Filters.eq("hip", "hop")).toFilterString());
    }

}
