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
import vanadis.common.io.Closeables;
import vanadis.core.collections.Generic;
import vanadis.core.lang.Not;
import vanadis.core.lang.Strings;
import vanadis.core.properties.PropertySet;
import vanadis.core.properties.PropertySets;
import vanadis.core.properties.Resolve;
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

    private static final PropertySet SYSTEM = PropertySets.systemProperties();

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

    public static Blueprints read(ResourceLoader resourceLoader,
                                  List<String> bootConfigPaths,
                                  List<String> bootConfigResources) {
        Not.nil(resourceLoader, "resourceLoader");
        Iterable<String> bootPaths =
                bootConfigPaths == null ? Collections.<String>emptyList() : bootConfigPaths;
        Iterable<String> bootResources =
                resolveResources(bootConfigPaths == null ? Collections.<String>emptyList() : bootConfigPaths,
                                 bootConfigResources);
        return unmarshal(resourceLoader, bootResources, bootPaths, unmarshaller());
    }

    private static Blueprints unmarshal(ResourceLoader loader,
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
        assert blueprints != null : "No blueprints created";
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

    private static URL resourceToNonNullURL(ResourceLoader loader, String resource) {
        URL url = loader.get(resource);
        if (url != null) {
            return url;
        }
        throw new IllegalArgumentException(loader + " did not find resource '" + resource + "'");
    }

    private static InputStream streamResource(ResourceLoader loader, String source, URL url) {
        URL stream = loader.get(source);
        if (stream == null) {
            throw new IllegalArgumentException
                    (loader + " found resource '" + source + "' at " + url +
                            ", but did not get a stream!");
        }
        try {
            return stream.openStream();
        } catch (IOException e) {
            throw new IllegalStateException
                    (url + " found by " + loader + " for resource " + source + " did not open", e);
        }
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
        BlueprintsType type = (BlueprintsType)element.getValue();
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
        BundleBuilder builder = basicBuilder(blueprintsType);
        for (BlueprintType blueprintType : blueprintsType.getBlueprint()) {
            BundleBuilder bundleBuilder = copyFor(blueprintType, builder);
            Blueprint blueprint = processBlueprint(source, bundleBuilder, blueprintType);
            blueprints.add(blueprint);
        }
        return blueprints;
    }

    private static BundleBuilder basicBuilder(BlueprintsType blueprintsType) {
        return new BundleBuilder(r(blueprintsType.getDefaultVersion()),
                                 r(blueprintsType.getRepo()));
    }

    private static String r(String value) {
        return Resolve.resolve(value, SYSTEM);
    }

    private static Blueprint processBlueprint(URI source, BundleBuilder builder, BlueprintType blueprint) {
        List<JAXBElement<?>> children = blueprint.getBundlesOrAutoBundleOrBundle();
        List<BundleSpecification> dynaBundles = Generic.list();
        List<BundleSpecification> autoBundles = Generic.list();
        List<ModuleSpecification> modules = Generic.list();
        for (BundlesType bundlesType : scan(BundlesType.class, children)) {
            processBundles(copyFor(bundlesType, builder),
                           bundlesType.getBundlesOrAutoBundleOrBundle(),
                           autoBundles, dynaBundles);
        }
        processBundles(builder, children, autoBundles, dynaBundles);
        addModules(children, modules);

        return new Blueprint(source, r(blueprint.getName()), split(r(blueprint.getExtends())),
                             toBool(blueprint.getAbstract()),
                             autoBundles, dynaBundles, modules);
    }

    private static String[] split(String ext) {
        if (ext == null) {
            return NO_PARENTS;
        }
        return ext.split(",");
    }

    private static void addModules(List<JAXBElement<?>> children, List<ModuleSpecification> modules) {
        for (ModuleType element : scan(ModuleType.class, children)) {
            modules.add(toModuleSpecification(element));
        }
    }

    public static ModuleSpecification toModuleSpecification(JAXBElement<?> element) {
        if (element.getDeclaredType().equals(ModuleType.class)) {
            return toModuleSpecification((ModuleType) element.getValue());
        }
        if (element.getDeclaredType().equals(BlueprintType.class)) {
            return toModuleSpecification(((BlueprintType)element.getValue()).getBundlesOrAutoBundleOrBundle().get(0));
        }
        if (element.getDeclaredType().equals(BlueprintsType.class)) {
            return toModuleSpecification(((BlueprintsType)element.getValue()).getBlueprint().get(0));
        }
        throw new IllegalArgumentException
                ("Invalid JAXBElement, expected value of " + ModuleType.class +
                        ", got " + element.getDeclaredType());
    }

    private static ModuleSpecification toModuleSpecification(BlueprintType blueprintType) {
        return toModuleSpecification
                (scan(ModuleType.class, blueprintType.getBundlesOrAutoBundleOrBundle()).iterator().next());
    }

    private static ModuleSpecification toModuleSpecification(ModuleType element) {
        String name = r(element.getName());
        String type = r(element.getType());
        return ModuleSpecification.create(type, name == null ? type : name,
                                          properties(scan(PropertiesType.class,
                                                          element.getPropertiesOrInjectOrExpose())),
                                          toBool(element.getGlobalProperties()),
                                          r(element.getConfigurationPid()),
                                          features(element));
    }

    private static Iterable<ModuleSpecificationFeature> features(ModuleType element) {
        List<ModuleSpecificationFeature> features = Generic.list();
        addTo(features, element, ModuleSpecificationFeatureType.EXPOSE);
        addTo(features, element, ModuleSpecificationFeatureType.TRACK);
        addTo(features, element, ModuleSpecificationFeatureType.INJECT);
        return features;
    }

    private static void addTo(List<ModuleSpecificationFeature> features,
                              ModuleType element,
                              ModuleSpecificationFeatureType type) {
        String msft = type.name().toLowerCase();
        Iterable<FeatureType> iterable = scan(FeatureType.class,
                                              msft,
                                              element.getPropertiesOrInjectOrExpose());
        for (FeatureType featureType : iterable) {
            PropertySet propertySet = properties(featureType.getProperties());
            features.add(new ModuleSpecificationFeature(r(featureType.getName()), type, propertySet));
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
                String valueString = r(property.getValue());
                Object value = Strings.isEmpty(valueString) ? EMPTY_STRING
                        : Retyper.coerce(r(property.getType()), Strings.trim(valueString));
                propertySet.set(r(property.getName()), value);
            }
            for (MultiPropertyType property : scanChildren(MultiPropertyType.class, children)) {
                List<PropertyValue> values = property.getValue();
                String[] array = new String[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    array[i] = r(values.get(i).getValue());
                }
                propertySet.set(r(property.getName()), Retyper.coerceArray(r(property.getType()), array));
            }
            for (XmlPropertyType property : scanChildren(XmlPropertyType.class, children)) {
                List<Element> elementList = property.getAny();
                if (!elementList.isEmpty()) {
                    String name = r(property.getName());
                    if (elementList.size() > 1) {
                        propertySet.set(name, Generic.seal(elementList));
                    } else {
                        propertySet.set(name, elementList.get(0));
                    }
                }
            }
        }
        return propertySet.isEmpty() ? PropertySets.EMPTY : propertySet.copy(false);
    }

    private static void processBundles(BundleBuilder builder,
                                       List<JAXBElement<?>> children,
                                       List<BundleSpecification> autoBundles,
                                       List<BundleSpecification> dynaBundles) {
        for (BundleType bundle : scan(BundleType.class, "bundle", children)) {
            dynaBundles.add(copyFor(bundle, builder).build());
        }
        for (BundleType bundle : scan(BundleType.class, "auto-bundle", children)) {
            autoBundles.add(copyFor(bundle, builder).build());
        }
        List<BundlesType> bundlesNested = scan(BundlesType.class, children);
        if (!bundlesNested.isEmpty()) {
            for (BundlesType bundles : bundlesNested) {
                List<JAXBElement<?>> elements = bundles.getBundlesOrAutoBundleOrBundle();
                processBundles(copyFor(bundles, builder.copy()), elements, autoBundles, dynaBundles);
            }
        }
    }

    private static BundleBuilder copyFor(BundleType bundle, BundleBuilder original) {
        return original.copy()
                .setArtifact(r(bundle.getArtifact()))
                .setGroup(r(bundle.getGroup()))
                .setVersion(r(bundle.getVersion()))
                .setConfigurationPid(r(bundle.getConfigurationPid()))
                .setGlobalProperties(toBool(bundle.getGlobalProperties()))
                .setStartLevel(toInt(bundle.getStartLevel()))
                .addPropertySet(properties(bundle.getProperties()))
                .setRepo(r(bundle.getRepo()));
    }

    private static BundleBuilder copyFor(BundlesType bundles, BundleBuilder original) {
        String value = bundles.getGlobalProperties();
        return original.copy()
                .setVersion(r(bundles.getVersion()))
                .setGroup(r(bundles.getGroup()))
                .addArtifactPrefix(r(bundles.getArtifactPrefix()))
                .addGroupPrefix(r(bundles.getGroupPrefix()))
                .setGlobalProperties(toBool(value))
                .setStartLevel(toInt(bundles.getStartLevel()))
                .setRepo(r(bundles.getRepo()));
    }

    private static BundleBuilder copyFor(BlueprintType blueprint, BundleBuilder original) {
        String level = blueprint.getStartLevel();
        return original.copy()
                .setVersion(r(blueprint.getDefaultVersion()))
                .setStartLevel(toInt(level))
                .setRepo(r(blueprint.getRepo()));
    }

    private static Boolean toBool(String value) {
        return value == null ? null : Boolean.parseBoolean(r(value));
    }

    private static Integer toInt(String str) {
        return str == null ? null : Integer.parseInt(r(str));
    }
}
