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
package vanadis.common.test;

import java.lang.annotation.*;

/**
 * Indicator that a method is only visible for testing purposes.  IDEA
 * is configurable to treat it as an entry point annotation, for purposes
 * of code analysis.
 */
@Documented
@Target({ElementType.FIELD, ElementType.TYPE,
         ElementType.METHOD, ElementType.ANNOTATION_TYPE,
         ElementType.CONSTRUCTOR, ElementType.PACKAGE})
@Retention(RetentionPolicy.SOURCE)
public @interface ForTestingPurposes {

    /**
     * Optional explanation.
     *
     * @return Excuses, excuses...
     */
    String value() default "";
}
