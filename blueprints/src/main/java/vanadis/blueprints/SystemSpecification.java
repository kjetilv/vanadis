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
import vanadis.core.collections.Iterables;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;
import vanadis.util.mvn.Repo;

import java.net.URI;
import java.util.*;

public class SystemSpecification {

    public static SystemSpecification createSystemSpecifiction(URI root, Iterable<Blueprint> blueprints) {
        SystemSpecification specification = null;
        for (Blueprint blueprint : blueprints) {
            SystemSpecification partial = blueprint.createSystemSpecification(root);
            specification = specification == null ? partial
                    : specification.combinedWith(partial);
        }
        return specification;
    }

    private final Set<ModuleSpecification> moduleSpecifications;

    private final Set<BundleSpecification> dynaCoordinates;

    private final Set<BundleSpecification> autoCoordinates;

    private final List<String> names;

    private final URI root;

    private final List<URI> sources;

    public SystemSpecification(URI source, String name, URI root,
                               Collection<BundleSpecification> autoCoordinates,
                               Collection<BundleSpecification> dynaCoordinates,
                               Collection<ModuleSpecification> moduleSpecifications) {
        this(source == null ? Collections.<URI>emptyList() : Collections.singletonList(source),
             Collections.singletonList(name), root,
             autoCoordinates,
             dynaCoordinates,
             moduleSpecifications);
    }

    public SystemSpecification(List<URI> sources, List<String> names, URI root,
                               Collection<BundleSpecification> autoCoordinates,
                               Collection<BundleSpecification> dynaCoordinates,
                               Collection<ModuleSpecification> moduleSpecifications) {
        this.sources = Generic.seal(Generic.list(Generic.linkedHashSet(sources)));
        this.names = Generic.seal(names);
        this.root = root == null ? Repo.DEFAULT_URI : root;
        this.moduleSpecifications = moduleSpecifications == null
                ? Collections.<ModuleSpecification>emptySet()
                : Generic.seal(Generic.linkedHashSet(moduleSpecifications));
        this.dynaCoordinates = dynaCoordinates == null
                ? Collections.<BundleSpecification>emptySet()
                : Generic.seal(Generic.linkedHashSet(dynaCoordinates));
        this.autoCoordinates = autoCoordinates == null
                ? Collections.<BundleSpecification>emptySet()
                : Generic.seal(Generic.linkedHashSet(autoCoordinates));
    }

    public List<URI> getSources() {
        return sources;
    }

    public List<String> getNames() {
        return names;
    }

    public Set<BundleSpecification> getDynaCoordinates() {
        return dynaCoordinates;
    }

    public Set<BundleSpecification> getAutoCoordinates() {
        return autoCoordinates;
    }

    public Set<ModuleSpecification> getModuleSpecifications() {
        return moduleSpecifications;
    }

    public URI getRepo() {
        return root;
    }

    public boolean containsModuleSpecification(String name) {
        Not.nil(name, "name");
        for (ModuleSpecification serviceSpecification : moduleSpecifications) {
            if (serviceSpecification.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    SystemSpecification combinedWith(SystemSpecification systemSpecification) {
        validateCombinationWith(systemSpecification);
        List<String> names = Generic.list(this.names);
        names.addAll(systemSpecification.names);
        List<URI> sources = Generic.list(this.sources);
        sources.addAll(systemSpecification.sources);
        return new SystemSpecification(sources, names, root, combineAutoCoordinates(systemSpecification),
                                       combineDynaCoordinates(systemSpecification),
                                       combineModuleSpecifications(systemSpecification));
    }

    public List<BundleSpecification> getDynaBundles() {
        return getDynaBundles(null);
    }

    public List<BundleSpecification> getDynaBundles(Iterable<BundleResolver> resolvers) {
        return resolved(resolvers, dynaCoordinates);
    }

    public List<BundleSpecification> getAutoBundles() {
        return getAutoBundles(null);
    }

    private RelativeURIResolver resolver() {
        return new RelativeURIResolver(this.root);
    }

    public List<BundleSpecification> getAutoBundles(Iterable<BundleResolver> resolvers) {
        return resolved(resolvers, autoCoordinates);
    }

    public Set<ModuleSpecification> moduleSpecifications() {
        return moduleSpecifications;
    }

    private List<ModuleSpecification> combineModuleSpecifications(SystemSpecification systemSpecification) {
        int size = this.moduleSpecifications.size() + systemSpecification.moduleSpecifications.size();
        List<ModuleSpecification> serviceSpecifications = Generic.list(size);
        serviceSpecifications.addAll(this.moduleSpecifications);
        serviceSpecifications.addAll(systemSpecification.moduleSpecifications);
        return serviceSpecifications;
    }

    private List<BundleSpecification> combineDynaCoordinates(SystemSpecification systemSpecification) {
        List<BundleSpecification> coordinates = Generic.list(this.dynaCoordinates);
        coordinates.addAll(systemSpecification.dynaCoordinates);
        return coordinates;
    }

    private List<BundleSpecification> combineAutoCoordinates(SystemSpecification systemSpecification) {
        List<BundleSpecification> coordinates = Generic.list(this.autoCoordinates);
        coordinates.addAll(systemSpecification.autoCoordinates);
        return coordinates;
    }

    private void validateCombinationWith(SystemSpecification systemSpecification) {
        Map<String, BundleSpecification> otherNodeCoordinates = mapOtherCoordinates(systemSpecification);
        detectVersionConflicts(systemSpecification, otherNodeCoordinates);
        Set<String> otherServiceSpecifications = otherModuleSpecifications(systemSpecification);
        detectOverlaps(systemSpecification, otherServiceSpecifications);
    }

    private void detectOverlaps(SystemSpecification systemSpecification, Set<String> otherModuleSpecifications) {
        for (ModuleSpecification specification : moduleSpecifications) {
            if (otherModuleSpecifications.contains(specification.getName())) {
                throw new IllegalArgumentException
                        (this + " could not combine with " + systemSpecification +
                                ", duplicate module specification '" + specification + "'");
            }
        }
    }

    private void detectVersionConflicts(SystemSpecification systemSpecification,
                                        Map<String, BundleSpecification> otherNodeCoordinates) {
        for (BundleSpecification myCoordinate : Iterables.chain(autoCoordinates, dynaCoordinates)) {
            BundleSpecification otherNodeCoordinate = otherNodeCoordinates.get(myCoordinate.getBase());
            if (otherNodeCoordinate != null && !otherNodeCoordinate.sameVersion(myCoordinate)) {
                throw new IllegalArgumentException
                        (this + " could not combine with " + systemSpecification +
                                ", local artifact " + myCoordinate + " has a version conflict with " +
                                otherNodeCoordinate);
            }
        }
    }

    private List<BundleSpecification> resolved(Iterable<BundleResolver> resolvers,
                                               Collection<BundleSpecification> bundleSpecifications) {
        return bundleSpecifications == null
                ? Collections.<BundleSpecification>emptyList()
                : resolved(bundleSpecifications, new ManyBundleResolvers(resolvers, resolver()));
    }

    private static List<BundleSpecification> resolved(Collection<BundleSpecification> bundleSpecifications,
                                                      BundleResolver resolver) {
        List<BundleSpecification> resolvedSpecifications = Generic.list();
        for (BundleSpecification bundleSpecification : bundleSpecifications) {
            BundleSpecification resolvedSpecification = bundleSpecification.resolve(resolver);
            if (bundleSpecification == null) {
                throw new IllegalArgumentException
                        ("Unable to resolve specification " + bundleSpecification + " using " + resolver);
            }
            resolvedSpecifications.add(resolvedSpecification);
        }
        return resolvedSpecifications;
    }

    private static Set<String> otherModuleSpecifications(SystemSpecification systemSpecification) {
        Set<String> otherServiceSpecifications = Generic.set();
        for (ModuleSpecification serviceSpecification : systemSpecification.moduleSpecifications) {
            otherServiceSpecifications.add(serviceSpecification.getName());
        }
        return otherServiceSpecifications;
    }

    private static Map<String, BundleSpecification> mapOtherCoordinates(SystemSpecification systemSpecification) {
        Map<String, BundleSpecification> otherNodeCoordinates = Generic.map();
        for (BundleSpecification coordinate : systemSpecification.dynaCoordinates) {
            otherNodeCoordinates.put(coordinate.getBase(), coordinate);
        }
        return otherNodeCoordinates;
    }

    @Override
    public String toString() {
        return ToString.of(this, names, "auto", autoCoordinates.size(),
                           "dyna", dynaCoordinates.size(),
                           "modules", moduleSpecifications.size());
    }
}
