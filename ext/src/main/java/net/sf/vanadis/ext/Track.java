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
package net.sf.vanadis.ext;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Tracked methods and fields track a collection of services, with the
 * semantics of a {@link net.sf.vanadis.osgi.Mediator Mediator}.  The annotated method should return
 * a {@link java.util.Collection collection}, which will be
 * {@link java.util.Collection#add(Object) added} to and
 * {@link java.util.Collection#remove(Object) removed} from, according
 * to the mediator's setup.
 */
@Documented
@Retention(RUNTIME)
@Target({METHOD, FIELD})
public @interface Track {

    /**
     * The tracked type needs to be here, since the collection
     * is untyped at runtime.
     *
     * @return Override type
     */
    Class<?> trackedType();

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

    boolean remotable() default false;

    Property[] properties() default {};

    boolean trackReferences() default false;

    boolean retained() default true;
}