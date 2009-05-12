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
 * Configuration fields and methods are automatically assigned the
 * full configuration {@link vanadis.core.properties.PropertySet properties}.
 * Fields should be of type Dictionary, Properties, Map, Hashtable or
 * {@link vanadis.core.properties.PropertySet}.  Methods should have a
 * single argument, typed to one of the same types.
 */
@Documented
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Configuration {

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
}
