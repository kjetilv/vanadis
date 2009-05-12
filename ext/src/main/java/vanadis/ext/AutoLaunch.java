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

import java.lang.annotation.*;

/**
 * Auto-launch annotation.  Decorate a {@link vanadis.ext.Module module} to
 * trigger instantiation(s) of the module at load time.  The extender will launch
 * the instances with a call to {@link ObjectManagerFactory#autoLaunch()}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
public @interface AutoLaunch {

    /**
     * Named instance to be launched.  If not set, use the type name.
     *
     * @return Instance name
     */
    String name() default "";

    Property[] properties() default {};
}
