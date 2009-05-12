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
package vanadis.deployer;

import vanadis.core.collections.Generic;
import vanadis.core.io.Closeables;
import vanadis.core.io.IORuntimeException;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;
import vanadis.core.time.Time;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

abstract class AbstractDirectoryUriExplorer implements UriExplorer {

    private final String[] suffixes;

    private final File dir;

    private final Map<File, Discovery> discoveries = Generic.map();

    private Time lastCheckTime;

    private static final Set<File> NO_FILES = Collections.emptySet();

    protected AbstractDirectoryUriExplorer(File dir, String... suffixes) {
        this.suffixes = suffixes;
        this.dir = validate(dir);
    }

    /**
     * The type is the name of the directory.  So the type of "deploy/bundle"
     * is "bundle".
     *
     * @return Deploy type
     */
    @Override
    public String getType() {
        return dir.getName().toLowerCase();
    }

    boolean isSuffixedFile(File file) {
        for (String suffix : suffixes) {
            if (file.getName().endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    final File validate(File dir) {
        if (!valid(dir)) {
            throw new IllegalArgumentException(this + " could not work from " + dir);
        }
        return dir;
    }

    private static boolean valid(File dir) {
        return dir.exists() && isDirectory(dir) || dir.mkdirs();
    }

    private Discovery oldDiscovery(File currentFile) {
        return discoveries.get(currentFile);
    }

    private boolean newDiscovery(File path) {
        return !discoveries.containsKey(path);
    }

    private void clickWatch() {
        lastCheckTime = Time.mark();
    }

    @Override
    public Time getLastCheckTime() {
        return lastCheckTime;
    }

    @Override
    public DiscoveredUris discover() {
        return discover(null);
    }

    @Override
    public DiscoveredUris discover(String[] prefixes) {
        clickWatch();
        Collection<URI> news = Generic.list();
        Collection<URI> updates = Generic.list();
        Collection<URI> removes = Generic.list();
        discover(dir, prefixes, news, updates, removes);
        return new DiscoveredUrisImpl(news, updates, removes);
    }

    protected String[] list(File root) {
        return root.list();
    }

    protected abstract boolean isMatch(File file);

    private void discover(File root, String[] prefixes,
                          Collection<URI> news,
                          Collection<URI> updates,
                          Collection<URI> removes) {
        Set<File> currentFiles = currentFiles(root);
        for (File currentFile : currentFiles) {
            if (isURLSource(prefixes, currentFile)) {
                try {
                    collectURL(news, updates, currentFile);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (isDirectory(currentFile)) {
                discover(currentFile, prefixes, news, updates, removes);
            }
        }
        Set<File> knownFiles = Generic.set(discoveries.keySet());
        for (File knownFile : knownFiles) {
            if (goneFrom(currentFiles, knownFile)) {
                forget(removes, knownFile);
            }
        }
    }

    private boolean isURLSource(String[] prefixes, File currentFile) {
        boolean prefixMatch = prefixes == null || isPrefixed(currentFile, prefixes);
        boolean probableFile = isMatch(currentFile) || isURLFile(currentFile);
        return prefixMatch && probableFile;
    }

    private void forget(Collection<URI> removedUris, File knownFile) {
        Discovery undiscovery = discoveries.remove(knownFile);
        removedUris.add(undiscovery.getURL());
    }

    private Set<File> currentFiles(File root) {
        String[] fileNames = list(root);
        if (fileNames == null || fileNames.length == 0) {
            return NO_FILES;
        }
        Set<File> currentFiles = Generic.set(fileNames.length);
        for (String fileName : fileNames) {
            currentFiles.add(canonicalFile(root, fileName));
        }
        return currentFiles;
    }

    private File canonicalFile(File root, String fileName) {
        try {
            return new File(root, fileName).getAbsoluteFile().getCanonicalFile();
        } catch (IOException e) {
            throw new IORuntimeException(this + " failed to pinpoint file " + fileName, e);
        }
    }

    private URI toUri(File file) {
        File resolvedFile = resolveFile(file);
        if (isURLFile(file)) {
            String url = firstLineOf(file);
            try {
                return new URI(url);
            } catch (URISyntaxException e) {
                throw new DiscoveryException
                        (this + " failed find url in string '" + url + "', " +
                                "', the first line of url file " + resolvedFile, e);
            }
        } else {
            return resolvedFile.toURI();
        }
    }

    private String firstLineOf(File file) {
        FileInputStream fileStream = streamFrom(file);
        InputStreamReader reader = new InputStreamReader(fileStream, Charset.forName("utf-8"));
        LineNumberReader linesStream = new LineNumberReader(reader);
        try {
            return linesStream.readLine();
        } catch (Exception e) {
            throw new DiscoveryException(this + " failed to read first line of url file " + file, e);
        } finally {
            Closeables.close(linesStream);
        }
    }

    private FileInputStream streamFrom(File file) {
        try {
            return new FileInputStream(file);
        } catch (Exception e) {
            throw new DiscoveryException
                    (this + " failed to open file " + file, e);
        }
    }

    static boolean isDirectory(File file) {
        return file.isDirectory();
    }

    private void collectURL(Collection<URI> news, Collection<URI> updated, File currentFile) {
        if (newDiscovery(currentFile)) {
            remember(currentFile, null, news);
        } else {
            Discovery oldDiscovery = oldDiscovery(currentFile);
            if (oldDiscovery != null && updated(oldDiscovery, currentFile)) {
                remember(currentFile, oldDiscovery.getURL(), updated);
            }
        }
    }

    protected boolean updated(Discovery oldDiscovery, File currentFile) {
        return oldDiscovery.isUpdated(currentFile);
    }

    private void remember(File file, URI existingUri, Collection<URI> uris) {
        URI uri = existingUri == null ? toUri(Not.nil(file, "file")) : existingUri;
        if (uri != null) {
            uris.add(uri);
            discoveries.put(file, new Discovery(file, uri, lastCheckTime));
        } else {
            throw new DiscoveryException
                    (this + " failed to resolve file " + file + " to a URL, " +
                            "number of URLs resolved so for: " + uris.size());
        }
    }

    protected File resolveFile(File file) {
        return file;
    }

    private static boolean isPrefixed(File file, String[] prefixes) {
        String name = file.getName();
        for (String prefix : prefixes) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isURLFile(File file) {
        return file.isFile() && file.getName().endsWith(".url");
    }

    private static boolean goneFrom(Set<File> currentFiles, File knownFile) {
        return !currentFiles.contains(knownFile);
    }

    @Override
    public String toString() {
        return ToString.of
                (this, "discovered", discoveries.size(),
                 "@", lastCheckTime == null ? "<no time>" : lastCheckTime.getDate());
    }
}
