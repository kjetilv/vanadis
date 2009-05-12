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

import vanadis.blueprints.gen.*;
import vanadis.core.collections.Generic;
import vanadis.core.lang.Strings;
import vanadis.core.properties.PropertySet;
import vanadis.core.properties.PropertySets;
import vanadis.core.reflection.Retyper;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBElement;
import java.net.URI;
import java.util.Collections;
import java.util.List;

final class BlueprintsReader {

    static <T> List<T> scan(Class<T> clazz, Iterable<JAXBElement<?>> elements) {
        return scan(clazz, null, elements);
    }

    static <T> List<T> scanChildren(Class<T> clazz, Iterable<?> elements) {
        List<T> ts = Generic.list();
        for (Object object : elements) {
            if (clazz.isInstance(object)) {
                ts.add(clazz.cast(object));
            }
        }
        return ts;
    }

    static <T> List<T> scan(Class<T> clazz, String name, Iterable<JAXBElement<?>> elements) {
        List<T> ts = Generic.list();
        for (JAXBElement<?> element : elements) {
            if (element.getDeclaredType().equals(clazz)) {
                if (name == null || element.getName().getLocalPart().equals(name)) {
                    ts.add(clazz.cast(element.getValue()));
                }
            }
        }
        return ts;
    }

    static List<Blueprint> read(URI source, BlueprintsType blueprintsType) {
        List<Blueprint> blueprints = Generic.list();
        BundleBuilder builder = new BundleBuilder();
        builder.setVersion(blueprintsType.getDefaultVersion());
        for (BlueprintType blueprintType : blueprintsType.getBlueprint()) {
            blueprints.add(processBlueprint(source, builder.copy(), blueprintType));
        }
        return blueprints;
    }

    private static Blueprint processBlueprint(URI source, BundleBuilder builder, BlueprintType blueprint) {
        List<JAXBElement<?>> children = blueprint.getBundlesOrAutoBundleOrBundle();
        List<BundleSpecification> dynaBundles = Generic.list();
        List<BundleSpecification> autoBundles = Generic.list();
        List<ModuleSpecification> modules = Generic.list();
        for (BundlesType bundlesType : scan(BundlesType.class, children)) {
            addBundles(copyFor(bundlesType, builder.copy()), bundlesType.getBundlesOrAutoBundleOrBundle(),
                       autoBundles, dynaBundles);
        }
        addBundles(builder, children, autoBundles, dynaBundles);
        addModules(children, modules);
        return new Blueprint(source, blueprint.getName(), blueprint.getExtends(), blueprint.isAbstract(),
                             autoBundles,
                             dynaBundles,
                             modules);
    }

    private static void addModules(List<JAXBElement<?>> children, List<ModuleSpecification> modules) {
        for (ModuleType moduleType : scan(ModuleType.class, children)) {
            modules.add(toModuleSpecification(moduleType)); // features(moduleType)));
        }
    }

    public static ModuleSpecification toModuleSpecification(JAXBElement<?> moduleType) {
        if (moduleType.getDeclaredType().equals(ModuleType.class)) {
            return toModuleSpecification((ModuleType) moduleType.getValue());
        } else if (moduleType.getDeclaredType().equals(BlueprintType.class)) {
            return toModuleSpecification(((BlueprintType)moduleType.getValue()).getBundlesOrAutoBundleOrBundle().get(0));
        } else if (moduleType.getDeclaredType().equals(BlueprintsType.class)) {
            return toModuleSpecification(((BlueprintsType)moduleType.getValue()).getBlueprint().get(0));
        } else {
            throw new IllegalArgumentException
                ("Invalid JAXBElement, expected value of " + ModuleType.class +
                        ", got " + moduleType.getDeclaredType());
        }
    }

    private static ModuleSpecification toModuleSpecification(BlueprintType blueprintType) {
        return toModuleSpecification(scan(ModuleType.class, blueprintType.getBundlesOrAutoBundleOrBundle()).iterator().next());
    }

    private static ModuleSpecification toModuleSpecification(ModuleType moduleType) {
        String name = moduleType.getName();
        String type = moduleType.getType();
        return ModuleSpecification.create(type, name == null ? type : name,
                                          properties(scan(PropertiesType.class, moduleType.getPropertiesOrInjectOrExpose())),
                                          moduleType.isGlobalProperties(),
                                          features(moduleType));
    }

    private static Iterable<ModuleSpecificationFeature> features(ModuleType moduleType) {
        List<ModuleSpecificationFeature> features = Generic.list();
        features(moduleType, features, ModuleSpecificationFeatureType.EXPOSE);
        features(moduleType, features, ModuleSpecificationFeatureType.TRACK);
        features(moduleType, features, ModuleSpecificationFeatureType.INJECT);
        return features;
    }

    private static void features(ModuleType moduleType, List<ModuleSpecificationFeature> features, ModuleSpecificationFeatureType type) {
        String msft = type.name().toLowerCase();
        Iterable<FeatureType> iterable = scan(FeatureType.class,
                                              msft,
                                              moduleType.getPropertiesOrInjectOrExpose());
        for (FeatureType featureType : iterable) {
            PropertySet propertySet = properties(featureType.getProperties());
            features.add(new ModuleSpecificationFeature(featureType.getName(), type, propertySet));
        }
    }

    private static PropertySet properties(PropertiesType propertiesType) {
        return properties(Collections.singletonList(propertiesType));
    }

    private static PropertySet properties(Iterable<PropertiesType> propertiesTypeIterable) {
        if (propertiesTypeIterable == null) {
            return PropertySets.EMPTY;
        }
        PropertySet propertySet = PropertySets.create();
        for (PropertiesType properties : propertiesTypeIterable) {
            if (properties == null) {
                continue;
            }
            List<Object> children = properties.getPropertyOrMultiPropertyOrXml();
            for (PropertyType property : scanChildren(PropertyType.class, children)) {
                propertySet.set(property.getName(), Retyper.coerce(property.getType(), Strings.trim(property.getValue())));
            }
            for (MultiPropertyType property : scanChildren(MultiPropertyType.class, children)) {
                List<PropertyValue> values = property.getValue();
                String[] array = new String[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    array[i] = values.get(i).getValue();
                }
                propertySet.set(property.getName(), Retyper.coerceArray(property.getType(), array));
            }
            for (XmlPropertyType property : scanChildren(XmlPropertyType.class, children)) {
                List<Element> elementList = property.getAny();
                if (!elementList.isEmpty()) {
                    if (elementList.size() > 1) {
                        propertySet.set(property.getName(), Generic.seal(elementList));
                    } else {
                        propertySet.set(property.getName(), elementList.get(0));
                    }
                }
            }
        }
        return propertySet.isEmpty() ? PropertySets.EMPTY : propertySet.copy(false);
    }

    private static void addBundles(BundleBuilder builder,
                                   List<JAXBElement<?>> children,
                                   List<BundleSpecification> autoBundles, List<BundleSpecification> dynaBundles) {
        for (BundleType bundle : scan(BundleType.class, "bundle", children)) {
            dynaBundles.add(copyFor(bundle, builder.copy()).build());
        }
        for (BundleType bundle : scan(BundleType.class, "auto-bundle", children)) {
            autoBundles.add(copyFor(bundle, builder.copy()).build());
        }
        List<BundlesType> bundlesNested = scan(BundlesType.class, children);
        if (!bundlesNested.isEmpty()) {
            for (BundlesType bundles : bundlesNested) {
                List<JAXBElement<?>> elements = bundles.getBundlesOrAutoBundleOrBundle();
                addBundles(copyFor(bundles, builder.copy()), elements, autoBundles, dynaBundles);
            }
        }
    }

    private static BundleBuilder copyFor(BundleType bundle, BundleBuilder original) {
        return original.copy()
                .setArtifact(bundle.getArtifact())
                .setGroup(bundle.getGroup())
                .setVersion(bundle.getVersion())
                .setStartLevel(bundle.getStartLevel())
                .setGlobalProperties(bundle.isGlobalProperties())
                .addPropertySet(properties(bundle.getProperties()));

    }

    private static BundleBuilder copyFor(BundlesType bundles, BundleBuilder original) {
        return original.copy()
                .setVersion(bundles.getVersion())
                .setGroup(bundles.getGroup())
                .addArtifactPrefix(bundles.getArtifactPrefix())
                .addGroupPrefix(bundles.getGroupPrefix())
                .setStartLevel(bundles.getStartLevel());
    }
}
