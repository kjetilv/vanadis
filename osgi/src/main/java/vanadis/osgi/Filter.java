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
package net.sf.vanadis.osgi;

import java.io.Serializable;

/**
 * A programmatic model of the string-based OSGi service filter.
 * To create instances of this interface, use the {@link net.sf.vanadis.osgi.Filters}
 * class and its static factory methods.
 */
public interface Filter extends Serializable {

    /**
     * Create a valid filter string for feeding to OSGi
     * @return Filter string
     */
    String toFilterString();

    /**
     * Create a filter which is an AND (&) of this filter and
     * the argument filters.
     *
     * @param exprs Filter expressions
     * @return New AND filter
     */
    Filter and(Filter... exprs);

    /**
     * Create a filter which is an OR (?) of this filter or
     * the argument filters.
     *
     * @param exprs Filter expressions
     * @return New OR filter
     */
    Filter or(Filter... exprs);

    /**
     * Create a filter which is a negation of this filter
     * @return Negated filter
     */
    Filter not();

    /**
     * <P>Convenience method for creating an AND (&) of this
     * filter and a (foo=true) filter for input argument foo.</P>
     *
     * <P>For instance, sending
     * <code>eq(no=1)</code> the message <code>and("obvious")</code>
     * would produce <code>(&(eq(no=1),obvious=true))</code>.
     * @param attribute Attribute which should be true
     * @return AND of this filter and attribute=true
     */
    Filter and(String attribute);

    /**
     * <P>Convenience method for creating an OR (|) of this
     * filter or a (foo=true) filter for input argument foo.</P>
     *
     * <P>For instance, sending
     * <code>eq(no=1)</code> the message <code>or("obvious")</code>
     * would produce <code>(&(eq(no=1),obvious=true))</code>.
     * @param attribute Attribute which should be true
     * @return OR of this filter or attribute=true
     */
    Filter or(String attribute);

    /**
     * As {@link #and(String)}, but negated.
     *
     * @param attribute Attribute which should be false
     * @return AND of this filter and attribute=false
     */
    Filter andNot(String attribute);

    /**
     * As {@link #or(String)}, but negated.
     *
     * @param attribute Attribute which should be false
     * @return OR of this filter or attribute=false
     */
    Filter orNot(String attribute);

    /**
     * True iff this filter involves an objectClasses expression,
     * meaning it specifies type.
     * @return True iff typed
     */
    boolean isTyped();

    /**
     * True only if this is the {@link Filters#NULL null filter}.
     * @return True iff null filter
     */
    boolean isNull();

    /**
     * True iff this filter matches the service properties.
     * Experimental, not verified against official OSGi
     * semantics.
     *
     * @param properties Properties
     * @return True iff properties match this filter
     */
    boolean matches(ServiceProperties<?> properties);
}
