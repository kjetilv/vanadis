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

package vanadis.extrt;

import vanadis.blueprints.ModuleSpecification;
import vanadis.core.lang.EqHc;
import vanadis.core.lang.ToString;

final class ServiceStatus {

    private BundleManager bundleManager;

    private final ModuleSpecification moduleSpecification;

    ServiceStatus(ModuleSpecification moduleSpecification) {
        this.moduleSpecification = moduleSpecification;
    }

    public ModuleSpecification getModuleSpecification() {
        return moduleSpecification;
    }

    public void nowHostedBy(BundleManager bundleManager) {
        this.bundleManager = bundleManager;
    }

    public void becameUnhosted() {
        this.bundleManager = null;
    }

    public boolean isHosted() {
        return bundleManager != null;
    }

    @Override
    public boolean equals(Object obj) {
        ServiceStatus serviceStatus = EqHc.retyped(this, obj);
        return serviceStatus != null && EqHc.eq(moduleSpecification, serviceStatus.moduleSpecification);
    }

    @Override
    public int hashCode() {
        return EqHc.hc(moduleSpecification);
    }

    @Override
    public String toString() {
        return ToString.of(this, moduleSpecification, "hostedBy", bundleManager);
    }
}
