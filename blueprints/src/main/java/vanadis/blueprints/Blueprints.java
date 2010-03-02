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

import vanadis.common.test.ForTestingPurposes;
import vanadis.core.collections.Generic;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;

import java.io.Serializable;
import java.net.URI;
import java.util.*;

public final class Blueprints implements Iterable<String>, Serializable {

    private final Map<String, Blueprint> blueprints;

    private final List<URI> sources;

    @ForTestingPurposes
    Blueprints(URI source, Blueprint... blueprints) {
        this(Collections.singletonList(source),
             null,
             Arrays.<Blueprint>asList(Not.emptyVarArgs(blueprints, "blueprints")),
             false);
    }

    public Blueprints(URI source, Blueprints blueprints, Collection<Blueprint> blueprintCollection) {
        this(Collections.singletonList(source), blueprints, blueprintCollection, false);
    }

    private Blueprints(List<URI> sources, Blueprints blueprints, Collection<Blueprint> blueprintCollection, boolean validate) {
        failOnEmptyArguments(blueprintCollection);
        this.blueprints = verifiedForNameclashes(blueprints, blueprintCollection);
        this.sources = appended(blueprints, sources);
        if (validate) {
            connectFamilies();
            failOnAbstractLeaves();
        }
    }

    public Blueprints validate() {
        return new Blueprints(sources, null, blueprints.values(), true);
    }

    @Override
    public Iterator<String> iterator() {
        return blueprints.keySet().iterator();
    }

    public SystemSpecification getSystemSpecification(URI root, String... blueprintNames) {
        return getSystemSpecification(root, Arrays.asList(blueprintNames));
    }

    public SystemSpecification getSystemSpecification(URI root, Iterable<String> blueprintNames) {
        Not.empty(blueprintNames, "blueprint names");
        SystemSpecification systemSpecification = null;
        for (String blueprintName : blueprintNames) {
            Blueprint blueprint = getBlueprint(blueprintName);
            validateTemplate(blueprintName, blueprint);
            SystemSpecification partialSpecification = blueprint.createSpecification(root);
            systemSpecification = systemSpecification == null
                    ? partialSpecification
                    : systemSpecification.combinedWith(partialSpecification);
        }
        return systemSpecification;
    }


    public Blueprint getBlueprint(String nodeName) {
        return blueprints.get(nodeName);
    }

    private void validateTemplate(String name, Blueprint blueprint) {
        if (blueprint == null) {
            throw new IllegalArgumentException(this + ": No such node specification: " + name);
        }
        if (blueprint.isAbstract()) {
            throw new IllegalArgumentException(blueprint + " is abstract!");
        }
    }

    private static List<URI> appended(Blueprints blueprints, List<URI> sources) {
        if (blueprints == null) {
            return sources;
        }
        List<URI> appended = Generic.list();
        appended.addAll(blueprints.sources);
        appended.addAll(sources);
        return Collections.unmodifiableList(appended);
    }

    private static void failOnEmptyArguments(Collection<Blueprint> blueprints) {
        if (blueprints == null || blueprints.isEmpty()) {
            throw new IllegalArgumentException("No nodes!");
        }
    }

    private static Map<String, Blueprint> verifiedForNameclashes(Blueprints blueprints,
                                                                 Collection<Blueprint> blueprintCollection) {
        Map<String, Blueprint> workingSet = blueprints == null
                ? Generic.<String, Blueprint>map()
                : Generic.map(blueprints.blueprints);
        for (Blueprint blueprint : blueprintCollection) {
            Blueprint previous = workingSet.put(blueprint.getName(), blueprint);
            if (previous != null) {
                throw new IllegalArgumentException("Multiple nodes named " + blueprint.getName());
            }
        }
        return Generic.seal(workingSet);
    }

    private void connectFamilies() {
        for (Blueprint blueprint : blueprints.values()) {
            String[] extendz = blueprint.getExtends();
            if (extendz != null && extendz.length > 0) {
                for (String extend : extendz) {
                    Blueprint parent = blueprints.get(extend);
                    if (parent == null) {
                        throw new IllegalArgumentException(blueprint + " extends unknown parent '" + extend + "'");
                    }
                    blueprint.addParent(parent);
                }
            }
        }
    }

    private void failOnAbstractLeaves() {
        for (Blueprint blueprint : blueprints.values()) {
            if (blueprint.isAbstract() && blueprint.isLeaf()) {
                throw new IllegalArgumentException(blueprint + " is an abstract leaf!");
            }
        }
    }

    private static final long serialVersionUID = 8135496286266586437L;

    @Override
    public String toString() {
        return ToString.of(this, blueprints.keySet());
    }
}
