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
package vanadis.ext;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Configured fields and methods are automatically set at startup time.  To find the
 * values, we look at service
 * specification {@link ModuleSpecification#getPropertySet() properties},
 * {@link vanadis.osgi.Context#getPropertySet() context properties,
 * or System properties.
 */
@Documented
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Configure {

    /**
     * Name of property.  If not set, it is derived from the
     * annotated field type, or the method return type.
     *
     * @return Explicit property name.
     */
    String name() default "";

    /**
     * If true, the property is <em>not</em> optional.
     *
     * @return True iff required
     */
    boolean required() default false;

    /**
     * Default value.
     *
     * @return Default value, in string form.  (Love that static typing.)
     */
    String def() default "";

    /**
     * If true, the setter/field will be invoked even when the value is null.
     *
     * @return True if null values are used
     */
    boolean setNull() default false;
}