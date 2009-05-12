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
package net.sf.vanadis.ext;

import javax.management.MBeanOperationInfo;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * For tagging methods as JMX operations.  Applies to
 * {@link net.sf.vanadis.ext.Module modules} and
 * {@link net.sf.vanadis.ext.Expose exposed} objects.
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface Operation {

    /**
     * Flip to true to return a String attribute instead of the attribute value
     *
     * @return True iff stringify
     */
    boolean string() default false;

    String desc() default "";

    Param[] params() default {};

    int impact() default MBeanOperationInfo.INFO;
}