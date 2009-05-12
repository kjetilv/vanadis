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

package net.sf.vanadis.extrt;

import net.sf.vanadis.blueprints.ModuleSpecification;
import net.sf.vanadis.osgi.MediatorListener;
import net.sf.vanadis.osgi.Reference;

class ModuleSpecMediatorListener implements MediatorListener<ModuleSpecification> {

    private final ModuleSystemListener moduleSystemListener;

    ModuleSpecMediatorListener(ModuleSystemListener moduleSystemListener) {
        this.moduleSystemListener = moduleSystemListener;
    }

    @Override
    public void removed(Reference<ModuleSpecification> reference,
                        ModuleSpecification moduleSpecification) {
        moduleSystemListener.moduleSpecificationRemoved(moduleSpecification);
    }

    @Override
    public void added(Reference<ModuleSpecification> reference,
                      ModuleSpecification moduleSpecification) {
        moduleSystemListener.moduleSpecificationAdded(moduleSpecification);
    }
}
