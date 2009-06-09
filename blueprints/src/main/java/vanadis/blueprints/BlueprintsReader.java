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

import org.w3c.dom.Element;
import vanadis.blueprints.gen.*;
import vanadis.core.collections.Generic;
import vanadis.core.io.Closeables;
import vanadis.core.lang.Not;
import vanadis.core.lang.Strings;
import vanadis.core.properties.PropertySet;
import vanadis.core.properties.PropertySets;
import vanadis.core.reflection.Retyper;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BlueprintsReader {

    private static final String BASE_RESOURCE = "vanadis.xml";

    private static final Set<String> BASE_RESOURCES = Collections.singleton(BASE_RESOURCE);

    private static final String EMPTY_STRING = "";

    private static final String[] NO_PARENTS = new String[] {};

    public static Blueprints readBlueprints(URI source) {
        return readBlueprints(source, null, openURLStream(uriToURL(source)));
    }

    public static Blueprints readBlueprints(URI source, InputStream stream) {
        return readBlueprints(source, null, stream);
    }

    public static Blueprints readBlueprints(URI source, Blueprints base, InputStream stream) {
        Unmarshaller unmarshaller = unmarshaller();
        return readBlueprints(source, base, stream, unmarshaller);
    }

    private static Blueprints readBlueprints(URI source, Blueprints base, InputStream stream,
                                             Unmarshaller unmarshaller) {
        List<Blueprint> list = unmarshal(source, stream, unmarshaller);
        return new Blueprints(source, base, list).validate();
    }

    public static Blueprints read(ClassLoader loader,
                                  List<String> bootConfigPaths,
                                  List<String> bootConfigResources) {
        Not.nil(loader, "loader");
        Iterable<String> bootPaths = bootConfigPaths == null ? Collections.<String>emptyList()
                : bootConfigPaths;
        Iterable<String> bootResources =
                resolveResources(bootConfigPaths == null ? Collections.<String>emptyList() : bootConfigPaths,
                                 bootConfigResources);
        return unmarshal(loader, bootResources, bootPaths, unmarshaller());
    }

    private static Blueprints unmarshal(ClassLoader loader,
                                        Iterable<String> bootResources, Iterable<String> bootPaths,
                                        Unmarshaller unmarshaller) {
        Blueprints blueprints = null;
        for (String resource : bootResources) {
            URL url = resourceToNonNullURL(loader, resource);
            URI source = toResourceURI(resource, url);
            InputStream stream = streamResource(loader, resource, url);
            blueprints = readBlueprints(source, blueprints, stream, unmarshaller);
        }
        for (String path : bootPaths) {
            File file = new File(path);
            URI source = file.toURI();
            InputStream stream = streamFile(file);
            List<Blueprint> list = unmarshal(source, stream, unmarshaller);
            blueprints = new Blueprints(source, blueprints, list);
        }
        return blueprints.validate();
    }

    public static ModuleSpecification readModuleSpecification(URI uri) {
        Unmarshaller unmarshaller = unmarshaller();
        return toModuleSpecification(parse(uri, unmarshaller));
    }

    private static Unmarshaller unmarshaller() {
        try {
            return jaxbContext(BlueprintsType.class).createUnmarshaller();
        } catch (JAXBException e) {
            throw new BlueprintReadingException("Failed to create unmarshaller for " + Blueprints.class, e);
        }
    }

    private static JAXBContext jaxbContext(Class<?> type) {
        try {
            return JAXBContext.newInstance(type);
        } catch (JAXBException e) {
            throw new BlueprintReadingException("Failed to setup jaxb context for " + type, e);
        }
    }

    private static InputStream streamFile(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new BlueprintReadingException("Failed to read file " + file, e);
        }
    }

    private static URL resourceToNonNullURL(ClassLoader loader, String resource) {
        URL url = loader.getResource(resource);
        if (url != null) {
            return url;
        }
        throw new IllegalArgumentException(loader + " did not find resource '" + resource + "'");
    }

    private static InputStream streamResource(ClassLoader loader, String source, URL url) {
        InputStream stream = loader.getResourceAsStream(source);
        if (stream == null) {
            throw new IllegalArgumentException
                    (loader + " found resource '" + source + "' at " + url +
                            ", but did not get a stream!");
        }
        return stream;
    }

    private static Iterable<String> resolveResources(List<String> bootConfigPaths,
                                                     List<String> bootConfigResources) {
        if (bootConfigResources == null || bootConfigResources.isEmpty()) {
            return contains(bootConfigPaths, BASE_RESOURCE)
                    ? Collections.<String>emptyList()
                    : BASE_RESOURCES;
        }
        return bootConfigResources;
    }

    private static boolean contains(List<String> bootConfigPaths, String base) {
        for (String path : bootConfigPaths) {
            if (path.endsWith(base)) {
                return true;
            }
        }
        return false;
    }

    private static URI toResourceURI(String source, URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid resource URL for " + source + ": " + url, e);
        }
    }

    private static List<Blueprint> unmarshal(URI source, InputStream input, Unmarshaller unmarshaller) {
        JAXBElement<?> element = parse(source, input, unmarshaller);
        BlueprintsType type = (BlueprintsType) element.getValue();
        return read(source, type);
    }

    private static JAXBElement<?> parse(URI source, Unmarshaller unmarshaller) {
        URL url = uriToURL(source);
        InputStream inputStream = openURLStream(url);
        return parse(source, inputStream, unmarshaller);
    }

    private static InputStream openURLStream(URL url) {
        try {
            return url.openStream();
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid URI, failed to open stream", e);
        }
    }

    private static URL uriToURL(URI source) {
        try {
            return source.toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URI, could not transform to URL: " + source, e);
        }
    }

    private static JAXBElement<?> parse(URI source, InputStream input, Unmarshaller unmarshaller) {
        try {
            return (JAXBElement<?>)unmarshaller.unmarshal(input);
        } catch (JAXBException e) {
            throw new BlueprintReadingException("Failed to unmarshal " + source, e);
        } finally {
            Closeables.close(input);
        }
    }

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
        return new Blueprint
                (source, blueprint.getName(), split(blueprint.getExtends()), blueprint.isAbstract(),
                 autoBundles,
                 dynaBundles,
                 modules);
    }

    private static String[] split(String ext) {
        if (ext == null) {
            return NO_PARENTS;
        }
        return ext.split(",");
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

    private static PropertySet properties(Iterable<PropertiesType> iterable) {
        if (iterable == null) {
            return PropertySets.EMPTY;
        }
        PropertySet propertySet = PropertySets.create();
        for (PropertiesType properties : iterable) {
            if (properties == null) {
                continue;
            }
            List<Object> children = properties.getPropertyOrMultiPropertyOrXml();
            for (PropertyType property : scanChildren(PropertyType.class, children)) {
                String valueString = property.getValue();
                Object value = Strings.isEmpty(valueString)
                        ? EMPTY_STRING
                        : Retyper.coerce(property.getType(), Strings.trim(valueString));
                propertySet.set(property.getName(), value);
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
                .setGlobalProperties(bundles.isGlobalProperties())
                .setStartLevel(bundles.getStartLevel());
    }
}
