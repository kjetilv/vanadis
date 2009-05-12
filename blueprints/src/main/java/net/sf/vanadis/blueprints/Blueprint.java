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
package net.sf.vanadis.blueprints;

import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.lang.EqHc;
import net.sf.vanadis.core.lang.Not;
import net.sf.vanadis.core.lang.ToString;

import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A boot config models part of a {@link Blueprints boot config set},
 * and are used to build
 * {@link SystemSpecification boot checklists}.
 */
public final class Blueprint implements Serializable, Iterable<ModuleSpecification> {

    private final String name;

    private final String extendz;

    private final boolean abstrackt;

    private boolean leaf = true;

    private Blueprint parent;

    private final URI source;

    private final Collection<BundleSpecification> autoCoordinates;

    private final Collection<BundleSpecification> dynaCoordinates;

    private final Collection<ModuleSpecification> moduleSpecifications;

    public Blueprint(URI source, String name, String extendz, Boolean abstrackt) {
        this(source, name, extendz, null, abstrackt, null, null, null);
    }

    public Blueprint(URI source, String name, String extendz, Boolean abstrackt,
                     List<BundleSpecification> autoCoordinates,
                     List<BundleSpecification> dynaCoordinates,
                     List<ModuleSpecification> moduleSpecifications) {
        this(source, name, extendz, null, abstrackt,
             autoCoordinates,
             dynaCoordinates,
             moduleSpecifications);
    }

    public Blueprint(URI source, String name, String extendz, Blueprint parent, Boolean abstrackt,
                     Collection<BundleSpecification> autoCoordinates,
                     Collection<BundleSpecification> dynaCoordinates,
                     Collection<ModuleSpecification> moduleSpecifications) {
        this.source = source;
        this.autoCoordinates = Generic.seal(autoCoordinates);
        this.dynaCoordinates = Generic.seal(dynaCoordinates);
        this.moduleSpecifications = Generic.seal(moduleSpecifications);
        this.name = Not.nil(name, "name");
        this.extendz = extendz;
        this.parent = parent;
        this.abstrackt = abstrackt != null && abstrackt;
    }

    @Override
    public Iterator<ModuleSpecification> iterator() {
        return moduleSpecifications.iterator();
    }

    public Iterable<BundleSpecification> getAutoBundles() {
        return autoCoordinates;
    }

    public Iterable<BundleSpecification> getDynaBundles() {
        return dynaCoordinates;
    }

    public String getName() {
        return name;
    }

    public String getExtends() {
        return extendz;
    }

    public boolean isAbstract() {
        return abstrackt;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public SystemSpecification createSpecification(URI root) {
        return SystemSpecification.createSystemSpecifiction(root, extendedBlueprints());
    }

    public Blueprint getParent() {
        return parent;
    }

    boolean contains(BundleSpecification bundleSpecification) {
        for (Blueprint blueprint : extendedBlueprints()) {
            if (blueprint.dynaCoordinates.contains(bundleSpecification) ||
                    blueprint.autoCoordinates.contains(bundleSpecification)) {
                return true;
            }
        }
        return false;
    }

    SystemSpecification createSystemSpecification(URI root) {
        return new SystemSpecification(source, name, root,
                                       autoCoordinates,
                                       dynaCoordinates,
                                       moduleSpecifications);
    }

    void setParentRuntime(Blueprint parentBlueprint) {
        Not.nil(parentBlueprint, "parent blueprint");
        if (parentBlueprint.equals(parent)) {
            return;
        }
        if (this.parent == null) {
            failOnCycle(parentBlueprint);
            this.parent = parentBlueprint;
            parentBlueprint.leaf = false;
        } else {
            throw new IllegalStateException(this + " already has parent " + parentBlueprint);
        }
    }

    private BlueprintExtendsTraverser extendedBlueprints() {
        return new BlueprintExtendsTraverser(this);
    }

    private void failOnCycle(Blueprint parent) {
        for (Blueprint elder : new BlueprintExtendsTraverser(parent)) {
            if (elder == this) {
                throw new IllegalArgumentException("Cycle! " + this + " cannot parent itself!");
            }
        }
    }

    private static final long serialVersionUID = -6951559860905287939L;

    @Override
    public int hashCode() {
        return EqHc.hc(name, parent);
    }

    @Override
    public boolean equals(Object obj) {
        Blueprint sheet = EqHc.retyped(this, obj);
        return sheet != null && EqHc.eq(name, sheet.name,
                                        parent, sheet.parent);
    }

    @Override
    public String toString() {
        return ToString.of(this, name, "parent", extendz);
    }
}
