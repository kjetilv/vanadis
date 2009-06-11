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
package vanadis.blueprints;

import vanadis.core.collections.Generic;
import vanadis.core.lang.EqHc;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;

import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;
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

    private final String[] extendz;

    private final boolean abstrackt;

    private final List<Blueprint> parents = Generic.list();

    private final URI source;

    private final Collection<BundleSpecification> autoCoordinates;

    private final Collection<BundleSpecification> dynaCoordinates;

    private final Collection<ModuleSpecification> moduleSpecifications;

    private boolean leaf = true;

    public Blueprint(URI source, String name, String extend, Boolean abstrackt,
                     List<BundleSpecification> autoCoordinates,
                     List<BundleSpecification> dynaCoordinates,
                     List<ModuleSpecification> moduleSpecifications) {
        this(source, name, new String[] { extend }, abstrackt,
             autoCoordinates,
             dynaCoordinates,
             moduleSpecifications);
    }

    public Blueprint(URI source, String name, String[] extendz, Boolean abstrackt,
                     List<BundleSpecification> autoCoordinates,
                     List<BundleSpecification> dynaCoordinates,
                     List<ModuleSpecification> moduleSpecifications) {
        this.source = source;
        this.autoCoordinates = Generic.seal(autoCoordinates);
        this.dynaCoordinates = Generic.seal(dynaCoordinates);
        this.moduleSpecifications = Generic.seal(moduleSpecifications);
        this.name = Not.nil(name, "name");
        this.extendz = extendz != null && extendz.length > 0 ? extendz : NO_PARENTS;
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

    public String[] getExtends() {
        return extendz;
    }

    public boolean isAbstract() {
        return abstrackt;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public SystemSpecification createSpecification(URI root) {
        return SystemSpecification.createSystemSpecifiction(root, new BlueprintExtendsTraverser(this));
    }

    public List<Blueprint> getParents() {
        return Generic.seal(parents);
    }

    boolean contains(BundleSpecification spec) {
        for (Blueprint blueprint : new BlueprintExtendsTraverser(this)) {
            if (blueprint.dynaCoordinates.contains(spec) || blueprint.autoCoordinates.contains(spec)) {
                return true;
            }
        }
        return false;
    }

    SystemSpecification createSystemSpecification(URI root) {
        return new SystemSpecification(source, name, root, autoCoordinates, dynaCoordinates, moduleSpecifications);
    }

    void addParent(Blueprint parentBlueprint) {
        Not.nil(parentBlueprint, "parent blueprint");
        if (parents.contains(parentBlueprint)) {
            return;
        }
        failOnCycle(parentBlueprint);
        this.parents.add(parentBlueprint);
        parentBlueprint.leaf = false;
    }

    private void failOnCycle(Blueprint parent) {
        for (Blueprint elder : new BlueprintExtendsTraverser(parent)) {
            if (elder == this) {
                throw new IllegalArgumentException("Cycle! " + this + " cannot parent itself!");
            }
        }
    }

    private static final long serialVersionUID = -6951559860905287939L;

    private static final String[] NO_PARENTS = new String[] {};

    @Override
    public int hashCode() {
        return EqHc.hc(name);
    }

    @Override
    public boolean equals(Object obj) {
        Blueprint blueprint = EqHc.retyped(this, obj);
        return blueprint != null && EqHc.eq(name, blueprint.name);
    }

    @Override
    public String toString() {
        return ToString.of(this, name, "parents", Arrays.toString(extendz));
    }
}
