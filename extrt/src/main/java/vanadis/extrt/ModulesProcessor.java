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

import org.osgi.framework.Bundle;
import vanadis.annopro.AnnotationDatum;
import vanadis.annopro.AnnotationsDigest;
import vanadis.annopro.AnnotationsDigests;
import vanadis.blueprints.ModuleSpecification;
import vanadis.concurrent.OperationQueuer;
import vanadis.core.collections.Generic;
import vanadis.common.io.Closeables;
import vanadis.core.lang.Strings;
import vanadis.common.test.ForTestingPurposes;
import vanadis.ext.AutoLaunch;
import vanadis.ext.Module;
import vanadis.ext.ModuleSystemException;
import vanadis.objectmanagers.ObjectManagerFactory;
import vanadis.osgi.Context;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

final class ModulesProcessor {

    private static final String CLASS_SUFFIX = ".class";

    private static final String ROOT = "/";

    private static final String DIR = "/";

    static Map<String, ObjectManagerFactory> managedFactories(Context context, Bundle bundle,
                                                              ObjectManagerObserver observer,
                                                              OperationQueuer dispatch) {
        Map<String, ObjectManagerFactory> factories = Generic.map();
        collect(ROOT, context, bundle, factories, observer, dispatch);
        return factories;
    }

    @ForTestingPurposes
    static ObjectManagerFactory objectManagerFactory(Context context,
                                                     ClassLoader classLoader,
                                                     String className,
                                                     InputStream inputStream,
                                                     ObjectManagerObserver observer,
                                                     OperationQueuer dispatch) {
        return objectManagerFactory(context, classLoader, null, className, inputStream, observer, dispatch);
    }

    private static Module module(AnnotationDatum<Class<?>> datum, Class<?> annotatedClass) {
        return datum.createProxy(annotatedClass.getClassLoader(), Module.class);
    }

    private static AnnotationsDigest annotations(InputStream inputStream) {
        try {
            return AnnotationsDigests.createFromStream(inputStream, Module.class.getName());
        } finally {
            Closeables.close(inputStream);
        }
    }

    private static AnnotationDatum<Class<?>> moduleData(Class<?> annotatedClass) {
        AnnotationsDigest digest = AnnotationsDigests.createFullFromType(annotatedClass);
        return digest.getClassDatum(Module.class);
    }

    private static Class<?> loadClass(ClassLoader classLoader, Bundle bundle, String moduleClassName) {
        if (bundle != null) {
            try {
                return bundle.loadClass(moduleClassName);
            } catch (ClassNotFoundException e) {
                throw new ModuleSystemException
                        ("Unable to load module class " + moduleClassName + " from " + bundle, e);
            }
        }
        try {
            return Class.forName(moduleClassName, true, classLoader);
        } catch (ClassNotFoundException e) {
            throw new ModuleSystemException
                    ("Unable to load module class " + moduleClassName + " from " + classLoader, e);
        }
    }

    private static Collection<ModuleSpecification> launches(String type, boolean autolaunch, AutoLaunch[] autoLaunches) {
        Collection<ModuleSpecification> moduleSpecifications = Generic.list();
        if (autolaunch) {
            moduleSpecifications.add(ModuleSpecification.create(type, type));
        }
        if (autoLaunches != null) {
            for (AutoLaunch autoLaunch : autoLaunches) {
                String name = autoLaunch.name();
                boolean useTypename = Strings.isBlank(name);
                boolean typeNamedAlreadyLaunched = useTypename && autolaunch;
                if (!typeNamedAlreadyLaunched) {
                    moduleSpecifications.add(ModuleSpecification.create
                            (type, useTypename ? type : name, PropertyUtils.read(autoLaunch.properties())));
                }
            }
        }
        return moduleSpecifications;
    }

    private static ObjectManagerFactory objectManagerFactory(Context context,
                                                             Bundle bundle,
                                                             String className,
                                                             InputStream inputStream,
                                                             ObjectManagerObserver observer,
                                                             OperationQueuer dispatch) {
        return objectManagerFactory(context, null, bundle, className, inputStream, observer, dispatch);
    }

    private static ObjectManagerFactory objectManagerFactory(Context context,
                                                             ClassLoader classLoader, Bundle bundle,
                                                             String className,
                                                             InputStream inputStream,
                                                             ObjectManagerObserver observer,
                                                             OperationQueuer dispatch) {
        AnnotationsDigest digest = annotations(inputStream);
        if (digest.hasClassData(Module.class)) {
            Class<?> annotatedClass = loadClass(classLoader, bundle, className);
            AnnotationDatum<Class<?>> datum = moduleData(annotatedClass);
            Module module = module(datum, annotatedClass);
            String type = moduleType(bundle, annotatedClass, module);
            return new ObjectManagerFactoryImpl(context, annotatedClass, type,
                                                launches(type, module.autolaunch(), module.launch()),
                                                observer, dispatch);
        }
        return null;
    }

    private static String moduleType(Bundle bundle, Class<?> annotatedClass, Module module) {
        String annotatedType = module.moduleType();
        if (Strings.isBlank(annotatedType)) {
            if (bundle != null) {
                return bundle.getSymbolicName();
            }
            return annotatedClass.getPackage().getName();
        }
        return annotatedType;
    }

    private static void collect(String prefix, Context context, Bundle bundle,
                                Map<String, ObjectManagerFactory> factories,
                                ObjectManagerObserver observer,
                                OperationQueuer dispatch) {
        for (Object pathObject : entryPaths(prefix, bundle)) {
            String path = pathObject.toString();
            if (isClassFile(path)) {
                ObjectManagerFactory factory =
                        objectManagerFactories(context, bundle, path, className(path), observer, dispatch);
                if (factory != null) {
                    map(bundle, factories, factory);
                }
            } else if (isDirectory(path)) {
                collect(path, context, bundle, factories, observer, dispatch);
            }
        }
    }

    private static boolean isClassFile(String path) {
        return path.endsWith(CLASS_SUFFIX);
    }

    private static boolean isDirectory(String path) {
        return path.endsWith(DIR);
    }

    private static void map(Bundle bundle, Map<String, ObjectManagerFactory> factories, ObjectManagerFactory factory) {
        ObjectManagerFactory existing = factories.put(factory.getType(), factory);
        if (existing != null) {
            throw new IllegalStateException
                    ("Multiple factories with type " + factory.getType() + " in bundle " +
                            bundle.getSymbolicName() + ": " + factory + " and " + existing);
        }
    }

    private static String className(String pathName) {
        return pathName.substring(0, pathName.length() - CLASS_SUFFIX.length()).replace('/', '.');
    }

    private static Iterable<String> entryPaths(String prefix, Bundle bundle) {
        Enumeration<?> enumeration = bundle.getEntryPaths(prefix);
        if (enumeration == null) {
            return Collections.emptyList();
        }
        Collection<String> entryPaths = Generic.list();
        while (enumeration.hasMoreElements()) {
            Object object = enumeration.nextElement();
            if (object != null) {
                entryPaths.add(object.toString());
            }
        }
        return entryPaths;
    }

    private static ObjectManagerFactory objectManagerFactories(Context context,
                                                               Bundle bundle,
                                                               String path,
                                                               String className,
                                                               ObjectManagerObserver observer,
                                                               OperationQueuer dispatch) {
        return objectManagerFactory(context, bundle, className, stream(bundle, path, className), observer, dispatch);
    }

    private static InputStream stream(Bundle bundle, String path, String className) {
        URL url = bundle.getEntry(path);
        try {
            return url.openStream();
        } catch (IOException e) {
            throw new IllegalStateException
                    ("Failed to open entry " + path + " to load " + className + " from " + bundle.getSymbolicName(), e);
        }
    }

    private ModulesProcessor() {
        // Don't make me.
    }
}
