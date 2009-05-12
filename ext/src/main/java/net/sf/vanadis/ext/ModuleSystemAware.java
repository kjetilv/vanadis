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

/**
 * Modules can implement this interface to receive a callback interface
 * towards the module system.  One convenient way to do this is to extend
 * {@link net.sf.vanadis.ext.AbstractModule} and declare implementation this interface.
 */
public interface ModuleSystemAware {

    /**
     * This method will be invoked during module startup.
     *
     * @param systemCallback The callback
     */
    void setManagedObjectCallback(ModuleSystemCallback systemCallback);
}