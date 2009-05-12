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
package vanadis.ext;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * <P>Injected fields and methods are automatically wired up to matching services in
 * the OSGi service registry.</P>
 *
 * <P>Note that multi-value injects must be matched up with a corresponding
 * {@link vanadis.ext.Retract retraction}.</P>
 *
 * @see vanadis.ext.Retract
 */
@Documented
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Inject {

    /**
     * Per-instance unique name.  If not set, it is derived from the method name.
     *
     * @return Name
     */
    String attributeName() default "";

    /**
     * Number of injections required for completion.
     *
     * @return Mininum injection count
     */
    int minimum() default 1;

    /**
     * If true, the instance will not expose any services before this injection point is complete.
     *
     * @return required
     */
    boolean required() default true;

    /**
     * If false, the instance will not retain any references passed to it, they
     * may be safely dropped by the framework.
     *
     * @return retained
     */
    boolean retained() default true;

    /**
     * If true, the instance will want an unregistered service replaced by
     * any qualifying candidate.  Only holds meaning for single-value injection points.
     *
     * @return replace
     */
    boolean replace() default true;

    /**
     * Iff true and {@link #replace()}, the instance will never see a null value
     * followed by a replacement, it will only see the replacement.  Unless
     *
     * @return replace
     */
    boolean replaceAtOnce() default false;

    boolean remotable() default false;

    Property[] properties() default {};

    /**
     * If changed, this type will be used instead of the argument type.
     *
     * @return Override type
     */
    Class<?> injectType() default Derived.class;

    /**
     * If true, also inject when service is <em>updated</em>.  When
     * false, inject only when service is <em>registered</em>.
     *
     * @return updates
     */
    boolean updates() default false;
}
