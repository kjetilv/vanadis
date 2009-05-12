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

import java.lang.annotation.*;

/**
 * <P>Indicates a module class. Objects with this annotation are picked up from bundles at
 * load time and managed as modules.  It defines a unique
 * {@link net.sf.vanadis.ext.Module#moduleType() module type}
 * and an {@link net.sf.vanadis.ext.ObjectManagerFactory} is created for it.  If the Module
 * has any {@link #launch() launch specifications}, it will immediately be called upon
 * to {@link ObjectManagerFactory#autoLaunch() auto-launch} them.</P>
 *
 * <P>The {@link #moduleType module type} is optional.  If unchanged from its default value
 * (empty string), the symbolic-name of the module's host bundle is used.</P>
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Module {

    /**
     * <P>Logical name for the module type.  If not set, it defaults to the
     * {@link org.osgi.framework.Bundle#getSymbolicName()
     * symbolic name} of the containing bundle.</P>
     *
     * <P>This gives a clear and understandable module type, but obviously, it only
     * makes sense if the bundle contains a <em>single</em> module!  Generally, attempts
     * to define multiple modules with the same type will fail.</P>
     *
     * @return Type
     */
    String moduleType() default "";

    /**
     * Auto-launched instances of the module.
     *
     * @return Auto-launched instances
     */
    AutoLaunch[] launch() default {};

    /**
     * Short-hand for a single {@link @AutoLaunch}: Launch a single instance, named after the type.
     *
     * @return If true, equivalent to <code>launch = @AutoLaunch</code>
     */
    boolean autolaunch() default false;
}