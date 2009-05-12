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
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Exposed fields, methods and classes are automatically registered to the service registry
 * as OSGi services.
 */
@Documented
@Target({METHOD, TYPE, FIELD})
@Retention(RUNTIME)
public @interface Expose {

    String attributeName() default "";

    String[] requiredDependencies() default {};

    int ranking() default 0;

    String description() default "";

    String pid() default "";

    boolean persistent() default false;

    boolean optional() default false;

    boolean remotable() default false;

    Class<?>[] objectClasses() default {};

    Property[] properties() default {};

    /**
     * If changed, this type will be used instead of the argument type.
     *
     * @return Override type
     */
    Class<?> exposedType() default Derived.class;

    String objectName() default "";

    String simpleObjectName() default "";

    boolean managed() default false;
}
