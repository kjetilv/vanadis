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

import net.sf.vanadis.blueprints.gen.BlueprintsType;
import net.sf.vanadis.core.io.Closeables;
import net.sf.vanadis.core.lang.Not;

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

public class BlueprintHelper {

    private static final String BASE_RESOURCE = "vanadis.xml";

    private static final Set<String> BASE_RESOURCES = Collections.singleton(BASE_RESOURCE);

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

    private static Blueprints readBlueprints(URI source, Blueprints base, InputStream stream, Unmarshaller unmarshaller) {
        List<Blueprint> list = unmarshal(source, stream, unmarshaller);
        return new Blueprints(source, base, list).validate();
    }

    public static Blueprints readBootConfigSet(ClassLoader loader,
                                               List<String> bootConfigPaths,
                                               List<String> bootConfigResources) {
        return unmarshalFromResources(Not.nil(loader, "loader"),
                                      bootConfigPaths,
                                      bootConfigResources);
    }

    public static ModuleSpecification readModuleSpecification(URI uri) {
        Unmarshaller unmarshaller = unmarshaller();
        return BlueprintsReader.toModuleSpecification(parse(uri, unmarshaller));
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

    private static Blueprints unmarshalFromResources(ClassLoader loader,
                                                     List<String> bootConfigPaths,
                                                     List<String> bootConfigResources) {
        Iterable<String> resources = resolveResources(bootConfigPaths, bootConfigResources);
        Unmarshaller unmarshaller = unmarshaller();
        Blueprints blueprints = null;
        for (String resource : resources) {
            URL url = resourceToNonNullURL(loader, resource);
            URI source = toResourceURI(resource, url);
            InputStream stream = streamResource(loader, resource, url);
            blueprints = readBlueprints(source, blueprints, stream, unmarshaller);
        }
        if (bootConfigPaths != null) {
            for (String path : bootConfigPaths) {
                File file = new File(path);
                URI source = file.toURI();
                InputStream stream = streamFile(file);
                List<Blueprint> list = unmarshal(source, stream, unmarshaller);
                blueprints = new Blueprints(source, blueprints, list);
            }
        }
        return blueprints.validate();
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
        return BlueprintsReader.read(source, type);
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
}
