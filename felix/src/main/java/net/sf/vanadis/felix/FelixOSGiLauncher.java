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

package net.sf.vanadis.felix;

import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.io.Files;
import net.sf.vanadis.core.io.Location;
import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.launcher.AbstractOSGiLauncher;
import net.sf.vanadis.launcher.ShutdownException;
import net.sf.vanadis.launcher.StartupException;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.cache.BundleCache;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

public class FelixOSGiLauncher extends AbstractOSGiLauncher {

    private Felix felix;

    @Override
    protected BundleContext launchedBundleContext() {
        clearCache(getHome());
        felix = new Felix(createNewConfiguration());
        try {
            felix.start();
        } catch (BundleException e) {
            throw new StartupException("Felix instance (" + felix + ") failed to start", e);
        }
        return felix.getBundleContext();
    }

    @Override
    protected void stopFramework() {
        try {
            felix.stop();
        } catch (BundleException e) {
            throw new ShutdownException(this + " failed to stop", e);
        } finally {
            felix = null;
        }
    }

    private Map<String, Object> createNewConfiguration() {
        Map<String, Object> configuration = Generic.map();
        configuration.put(Constants.FRAMEWORK_SYSTEMPACKAGES, systemPackages());
        configuration.put(Constants.FRAMEWORK_BOOTDELEGATION, bootDelegationPackages());
        configuration.put(BundleCache.CACHE_ROOTDIR_PROP, cacheDirectory(homeDirectory(getHome())).getAbsolutePath());
        setHome(configuration, getHome());
        setRepo(configuration, getSystemSpecification().getRepo());
        setLocation(configuration, getLocation());
        return Generic.seal(configuration);
    }

    private static void clearCache(URI home) {
        if (home != null) {
            File cache = new File(homeDirectory(home), DEFAULT_CACHE);
            if (cache.canWrite()) {
                deleteDirectory(cache);
            }
        }
    }

    private static File homeDirectory(URI home) {
        return home == null ? null : Files.create(home);
    }

    private static void setHome(Map<String, Object> cnf, URI home) {
        if (home != null) {
            vanadis("home", home.toASCIIString(), cnf);
        }
    }

    private static void setRepo(Map<String, Object> cnf, URI repo) {
        if (repo != null) {
            vanadis("repo", repo.toASCIIString(), cnf);
        }
    }

    private static void vanadis(String property, Object value, Map<String, Object> cnf) {
        cnf.put("vanadis." + property, value);
    }

    private static void setLocation(Map<String, Object> cnf, Location location) {
        if (location != null) {
            vanadis("location", location.toLocationString(), cnf);
            vanadis("host", location.getHost(), cnf);
            vanadis("port", location.getPort(), cnf);
        }
    }

    private static File cacheDirectory(File home) {
        return home == null ? makeShiftTmpDir() : new File(home, DEFAULT_CACHE);
    }

    private static File makeShiftTmpDir() {
        File tempFile = tempFile();
        return existingTempDir(tempFile);
    }

    private static File existingTempDir(File tempFile) {
        File tempDir = new File(tempFile.getParent(), FELIX_CACHE);
        if (!tempDir.mkdirs()) {
            throw new StartupException("Failed to create temp dir " + tempDir + " for caching");
        }
        return tempDir;
    }

    private static File tempFile() {
        try {
            return File.createTempFile(FELIX_CACHE, ".empty");
        } catch (IOException e) {
            throw new StartupException("Failed to set up cache in temp directory", e);
        }
    }

    private static boolean deleteDirectory(File path) {
        for (File file : path.listFiles()) {
            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                file.delete();
            }
        }
        return path.delete();
    }

    @Override
    public String toString() {
        return ToString.of(this, felix);
    }

    private static final String DEFAULT_CACHE = "var/felix/cache";
}
