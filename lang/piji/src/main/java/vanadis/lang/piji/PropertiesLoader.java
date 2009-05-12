/*
 * Copyright 2008 Kjetil Valstadsve
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

package net.sf.vanadis.lang.piji;

import net.sf.vanadis.core.collections.Generic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

class PropertiesLoader {

    private static void load(Properties props, InputStream stream)
        throws IOException {
        props.load(stream);
    }

    public static Properties load(InputStream stream)
        throws IOException {
        Properties props = new Properties();
        load(props, stream);
        return props;
    }

    public static void load(Properties props, String resource)
        throws IOException, PropertyException {
        load(props, PropertiesLoader.class, resource);
    }

    private static void load(Properties props, Class<?> clazz, String resource)
        throws IOException, PropertyException {
        InputStream stream;
        if (resource.startsWith("http://") ||
            resource.startsWith("https://") ||
            resource.startsWith("ftp://")) {
            URL url = new URL(resource);
            stream = url.openStream();
        } else {
            stream = clazz == null ?
                ClassLoader.getSystemResourceAsStream(resource) :
                clazz.getResourceAsStream(resource);
            if (stream == null) {
                Thread t = Thread.currentThread();
                stream =
                    t.getContextClassLoader().getResourceAsStream(resource);
                if (stream == null) {
                    try {
                        stream = new FileInputStream(resource);
                    } catch (FileNotFoundException ignore) {
                        try {
                            stream = new FileInputStream
                                (System.getProperty("user.dir") + "/" +
                                    resource);
                        } catch (FileNotFoundException e) {
                            throw new PropertyException
                                (PropertiesLoader.class +
                                    " could not locate resources or file " +
                                    resource + (clazz == null ?
                                    "" :
                                    " in " + clazz),
                                    e);
                        }
                    }
                }
            }
        }
        load(props, stream);
        stream.close();
    }

    private static Properties load(Class<?> clazz, String resource)
        throws IOException, PropertyException {
        Properties props = new Properties();
        load(props, clazz, resource);
        return props;
    }

    public static Properties load(String resource)
        throws IOException, PropertyException {
        return load(PropertiesLoader.class, resource);
    }

    public static Properties loadMultiple(String commaSepResources)
        throws IOException, PropertyException {
        return loadMultiple(PropertiesLoader.class, commaSepResources);
    }

    private static Properties loadMultiple(Class<?> clazz,
                                           String commaSepResources)
        throws IOException, PropertyException {
        StringTokenizer tokenizer =
            new StringTokenizer(commaSepResources, ",");
        List<String> resources = Generic.list();
        while (tokenizer.hasMoreTokens()) {
            String resource = tokenizer.nextToken();
            resources.add(resource);
        }
        return loadMultiple(clazz, resources);
    }

    public static Properties loadMultiple(List<String> resources)
        throws IOException, PropertyException {
        return loadMultiple(PropertiesLoader.class, resources);
    }

    private static Properties loadMultiple(Class<?> clazz, List<String> resources)
        throws IOException, PropertyException {
        Collections.reverse(resources);
        Iterator<String> iterator = resources.iterator();
        Properties props = new Properties();
        while (iterator.hasNext()) {
            String resource = iterator.next();
            load(props, clazz, resource);
        }
        return props;
    }

}

