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

package vanadis.lang.piji.loading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vanadis.core.collections.Generic;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class ClassResolver {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ClassLoader loader;

    private final Map<String, Class<?>> cache = Generic.map();

    private final Map<String, Class<?>> shortNameCache = Generic.map();

    private final Map<String, Package> packagesMap = Generic.map();

    private final List<Package> packages = Generic.list();

    private final Object packagesLock = new Object();

    private void initCache() {
        cache.put("int", Integer.TYPE);
        cache.put("long", Long.TYPE);
        cache.put("float", Float.TYPE);
        cache.put("double", Double.TYPE);
        cache.put("byte", Byte.TYPE);
        cache.put("short", Short.TYPE);
        cache.put("boolean", Boolean.TYPE);
        cache.put("char", Character.TYPE);
    }

    private void initPackages() {
        rememberPackage(Object.class.getPackage());
        rememberPackage(List.class.getPackage());
        rememberPackage(IOException.class.getPackage());
        getPackages();
    }

    public ClassResolver(ClassLoader loader) {
        if (loader == null) {
            throw new IllegalArgumentException(this + " cannot live without a classLoader!");
        }
        this.loader = loader;
        initCache();
        initPackages();
        log.debug(this + " was created");
    }

    private List<Package> getPackages() {
        synchronized (packagesLock) {
            if (packages.isEmpty()) {
                Package[] ps = Package.getPackages();
                for (Package p : ps) {
                    rememberPackage(p);
                }
                log.info(this + " learned of  " + ps.length + " packages");
            }
            return Generic.list(packages);
        }
    }

    private void rememberPackage(Package p) {
        synchronized (packagesLock) {
            if (p == null) {
                return;
            }
            String key = p.getName();
            boolean known = packagesMap.containsKey(key);
            if (!known) {
                packagesMap.put(key, p);
                packages.add(p);
            }
        }
    }

    public ClassLoader getClassLoader() {
        return this.loader;
    }

    public String getQualifiedName(String name) throws ClassNotFoundException {
        return getQualifiedName(name, true);
    }

    String getQualifiedName(String name, boolean fail) throws ClassNotFoundException {
        findClass(name, fail);
        return (shortNameCache.get(name)).getName();
    }

    public final Class<?> findClass(String name) throws ClassNotFoundException {
        return this.findClass(name, true);
    }

    public final Class<?> findClass(String name, boolean fail) throws ClassNotFoundException {
        if (name == null || name.trim().length() == 0) {
            throw new NullPointerException("Null or empty string given (" + name + ")");
        }
        if (name.endsWith("[]")) {
            throw new UnsupportedOperationException(this + " cannot talk about array classes yet: " + name);
        }

        ClassNotFoundException exception = null;
        Class<?> found = checkCaches(name);

        if (found == null) {
            try {
                found = checkSimpleName(name);
            }
            catch (ClassNotFoundException e) {
                log.debug(this + " found no " + name + " in " + loader + ": " + e);
                exception = e;
            }
        }

        if (found == null) {
            found = probePackages(name);
        }

        if (found == null) {
            log.warn(this + " could not resolve \"" + name + "\", " +
                    (fail ? "throwing" : "ignoring") + " " + exception);
            if (fail) {
                throw exception;
            }
            return null;
        } else {
            updateCaches(found);
            return found;
        }
    }

    private Class<?> checkSimpleName(String name) throws ClassNotFoundException {
        Class<?> found = this.loader.loadClass(name);
        if (found != null) {
            rememberPackage(found.getPackage());
        }
        return found;
    }

    private Class<?> checkCaches(String name) {
        return this.cache.containsKey(name) ? this.cache.get(name)
                : this.shortNameCache.containsKey(name) ? this.shortNameCache.get(name)
                        : null;
    }

    private void updateCaches(Class<?> foundClass) {
        this.cache.put(foundClass.getName(), foundClass);
        this.shortNameCache.put(shortName(foundClass), foundClass);
    }

    private Class<?> probePackages(String name) {
        List<Package> packages = getPackages();
        log.debug(this + " looks for \"" + name + "\" in " +
                packages.size() + " packages ..." +
                (packages.size() < 10 ? packagesMap.keySet().toString() : ""));
        for (Package packidch : packages) {
            Class<?> found = probeClass(packidch, name);
            if (found != null) {
                log.debug(this + " found " + found);
                return found;
            }
        }
        // Could it be... a package-less class!
        return probeClass(null, name);
    }

    private Class<?> probeClass(Package pack, String name) {
        try {
            String fullName = fullName(pack, name);
            return this.loader.loadClass(fullName);
        }
        catch (ClassNotFoundException ignore) {
            return null;
        }
    }

    private static String fullName(Package p, String name) {
        return p == null ? name : p.getName() + "." + name;
    }

    private static String shortName(Class<?> type) {
        Package p = type.getPackage();
        return p == null
                ? type.getName()
                : type.getName().substring(1 + p.getName().length());
    }

    @Override
    public String toString() {
        return "ClassResolver[" + loader +
                " packages:" + packages.size() +
                " cached:" + this.cache.size() + "]";
    }

}
