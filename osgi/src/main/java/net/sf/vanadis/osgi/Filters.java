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
package net.sf.vanadis.osgi;

import net.sf.vanadis.core.lang.VarArgs;
import net.sf.vanadis.core.properties.PropertySet;

import java.util.Collection;

/**
 * Static factory methods for creating {@link Filter Filter} instances.
 */
public final class Filters {

    /**
     * Null filter.
     */
    public static final Filter NULL = new NullFilter();

    /**
     * Return filter requiring attribute to be true.
     *
     * @param attribute Attribute which should be true
     * @return Filter
     */
    public static Filter isTrue(String attribute) {
        return eq(attribute, true);
    }

    /**
     * Return filter requiring attribute to be false.
     *
     * @param attribute Attribute which should be false
     * @return Filter
     */
    public static Filter isFalse(String attribute) {
        return isTrue(attribute).not();
    }

    /**
     * Return equality filter for attribute and values.
     *
     * @param attribute Attribute
     * @param values    Values
     * @return New filter
     */
    public static Filter eq(String attribute, Object... values) {
        return compare(CompareOperator.EQUAL, attribute, values);
    }

    public static Filter approx(String attribute, Object... values) {
        return compare(CompareOperator.APPROX, attribute, values);
    }

    public static Filter gt(String attribute, Object... values) {
        return compare(CompareOperator.GREATER, attribute, values);
    }

    public static Filter lt(String attribute, Object... values) {
        return compare(CompareOperator.LESS, attribute, values);
    }

    public static Filter present(String attribute) {
        return new PresentFilter(attribute);
    }

    public static Filter substring(String attribute, String substring) {
        return new SubstringFilter(attribute, substring);
    }

    public static Filter typeFilter(Class<?> serviceInterface) {
        return serviceInterface == null ? NULL
                : objectClasses(serviceInterface);
    }

    public static Filter objectClasses(Collection<String> objectClasses) {
        return objectClasses == null || objectClasses.isEmpty() ? NULL
                : new ObjectClassesFilter(objectClasses.toArray(new String[objectClasses.size()]));
    }

    public static Filter objectClassesNames(Collection<String> objectClasses) {
        return objectClasses == null || objectClasses.isEmpty() ? NULL
                : new ObjectClassesFilter(objectClasses);
    }

    public static Filter objectClasses(String... objectClasses) {
        return VarArgs.notPresent(objectClasses) ? NULL
                : new ObjectClassesFilter(objectClasses);
    }

    public static Filter objectClasses(Class<?>... objectClasses) {
        return VarArgs.notPresent(objectClasses) ? NULL
                : new ObjectClassesFilter(objectClasses);
    }

    /**
     * Create a filter which matches the properties.
     *
     * @param propertySet Properties
     * @return Matching filter
     */
    public static Filter filter(PropertySet propertySet) {
        Filter filter = Filters.NULL;
        for (String property : propertySet) {
            filter = filter.and(Filters.eq(property, propertySet.get(property)));
        }
        return filter;
    }

    private static Filter compare(CompareOperator op, String attribute, Object... values) {
        return new CompareFilter(op, attribute, values);
    }

    private Filters() {
        // Don't make me
    }
}
