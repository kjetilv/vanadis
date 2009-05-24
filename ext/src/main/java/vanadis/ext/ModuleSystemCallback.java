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

import vanadis.objectmanagers.ManagedState;

/**
 * A callback interface for the module.  The module receives an instance
 * of this interface by implementing {@link vanadis.ext.ModuleSystemAware}.
 */
public interface ModuleSystemCallback {

    /**
     * Notify module system that you have failed.
     *
     * @param cause Reasons for failing
     * @param msg   A comforting message and/or excuses
     */
    void fail(Throwable cause, Object... msg);

    /**
     * Get the managed state.
     *
     * @return Current managed state
     */
    ManagedState getState();
}
