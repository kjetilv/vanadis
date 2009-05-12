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
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Retractions must be used to match multi-value injections with their corresponding
 * {@link Inject injections}.
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface Retract {

    /**
     * The attribute name must match the corresponding {@link Inject Inject} point.  If
     * the inject point is prefixed {@code add-} and this method is prefixed {@code remove-},
     * with <em>the same prefixed name</em>, it will automatically resolve, and setting this attribute
     * is redundant.
     *
     * @return Attribute name
     */
    String attributeName() default "";
}