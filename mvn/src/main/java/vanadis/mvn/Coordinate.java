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

package vanadis.mvn;

import org.w3c.dom.Document;
import vanadis.core.collections.Generic;
import vanadis.core.io.Closeables;
import vanadis.core.io.Files;
import vanadis.core.io.IORuntimeException;
import vanadis.core.lang.EqHc;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;
import vanadis.core.ver.Version;
import vanadis.mvn.xml.Xml;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The coordinate class models resolving of resources against a standard maven 2
 * type repository.
 */
public final class Coordinate implements Serializable {

    private static final String[] ARCHIVE_TYPES = new String[] { "application/java-archive",
                                                                 "application/x-gzip",
                                                                 "application/zip",
                                                                 "multipart/x-gzip" };

    /**
     * <P>This factory method takes string specifications.  The coordinate is not
     * checked against any repository.  Specifications must be on
     * one of the the forms:</P>
     *
     * <ul>
     * <li><code>groupId:artifactId:packaging:version</code></li>
     * <li><code>groupId:artifactId:version</code></li>
     * <li><code>groupId:artifactId</code></li>
     * </ul>
     *
     * <P>The default packaging is <code>jar</code>.  Omitting the version and/or the
     * packaging will produce a <code>jar</code> coordinate.</P>
     *
     * <P>Omitting the version produces an {@link #isVersioned() unversioned} <code>jar</code>
     * type coordinate.</P>
     *
     * @param coordinate Coordinate specification
     * @return Coordinate
     */
    public static Coordinate at(String coordinate) {
        String[] spec = split(coordinate);
        boolean allFields = spec.length > 3;
        boolean versionField = spec.length > 2;
        String packaging = allFields ? spec[2] : DEFAULT_PACKAGING;
        Version version = allFields ? new Version(spec[3])
                : (versionField ? new Version(spec[2]) : null);
        return create(spec[0], spec[1], packaging, version);
    }

    /**
     * Create unversioned coordinate with the same groupId/artifactId.
     *
     * @param id Id
     * @return Coordinate
     */
    public static Coordinate unversioned(String id) {
        return unversioned(id, id);
    }

    /**
     * Create unversioned coordinate with the groupId and artifactId.
     *
     * @param groupId    Group ip
     * @param artifactId Artifact id
     * @return Coordinate
     */
    public static Coordinate unversioned(String groupId, String artifactId) {
        return create(groupId, artifactId, null, null);
    }

    /**
     * Create versioned coordinate with the groupId and artifactId.
     *
     * @param groupId    Group ip
     * @param artifactId Artifact id
     * @param version    Version
     * @return Coordinate
     */
    public static Coordinate versioned(String groupId, String artifactId, Version version) {
        return create(groupId, artifactId, null, version);
    }

    /**
     * Create versioned coordinate with same groupId/artifactId.
     *
     * @param id      Id
     * @param version Version
     * @return Coordinate
     */
    public static Coordinate versioned(String id, Version version) {
        return create(id, id, null, version);
    }

    /**
     * Create a coordinate.
     *
     * @param groupId    Group id
     * @param artifactId Artifact id
     * @param packaging  Packaging
     * @param version    Version
     * @return Full coordinate
     */
    public static Coordinate create(String groupId, String artifactId, String packaging, Version version) {
        return new Coordinate(groupId, artifactId, packaging, version);
    }

    private final String groupId;

    private final String groupIdPath;

    private final String artifactId;

    private final Version version;

    private final String packaging;

    private Coordinate(String groupId, String artifactId, String packaging, Version version) {
        this.packaging = packaging == null ? DEFAULT_PACKAGING : dedot(packaging);
        this.groupId = Not.nil(groupId, GROUP_ID);
        this.groupIdPath = groupId.replace('.', '/');
        this.artifactId = Not.nil(artifactId, ARTIFACT_ID);
        this.version = version;
    }

    /**
     * Group id.
     *
     * @return Group id
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Artifact id.
     *
     * @return Artifact id
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Version.
     *
     * @return version
     */
    public Version getVersion() {
        return version;
    }

    /**
     * True iff versioned.
     *
     * @return Versioned
     */
    public boolean isVersioned() {
        return this.version != null;
    }

    /**
     * Packaging.
     *
     * @return Packaging
     */
    public String getPackaging() {
        return packaging;
    }

    /**
     * Get "groupId:artifactId" base string.
     *
     * @return Base string
     */
    public String getBase() {
        return getBaseBuilder().toString();
    }

    /**
     * Return full coordinate string on the form groupId:artifactId:packaging:version.
     *
     * @return Coordinate string
     */
    public String toCoordinateString() {
        StringBuilder sb = getBaseBuilder();
        if (packaging != null) {
            sb.append(":").append(this.packaging);
        }
        if (version != null) {
            sb.append(":").append(this.version.toVersionString());
        }
        return sb.toString();
    }

    public String toMvnUri() {
        if (isVersioned()) {
            return "mvn:" + groupId + "/" + artifactId + "/" + version.toVersionString();
        }
        throw new IllegalStateException(this + " is not versioned");
    }

    public URI uriIn(URI repo) {
        return uriIn(repo, false);
    }

    public URI uriIn(URI repo, boolean validate) {
        return uriIn(repo, validate, true);
    }
    
    public URI uriIn(URI repo, boolean validate, boolean fail) {
        return in(repo, validate, fail);
    }

    public URI uriIn(File repo) {
        return toUri(fileIn(repo));
    }

    public File fileIn(File repo) {
        File groupIdDir = Files.getDirectory(repo, groupId.split("\\."));
        if (!groupIdDir.exists()) {
            throw new IllegalArgumentException(this + ": No artifacts found for " + groupId);
        }
        File artifactIdDir = new File(groupIdDir, artifactId);
        if (!artifactIdDir.exists()) {
            throw new IllegalArgumentException(this + ": No such artifact for " + groupId + ":" + artifactId);
        }
        Version version = resolveVersion(artifactIdDir);
        if (version == null) {
            String error = (isVersioned() ? "No versions found" : "No such version") + " found";
            throw new IllegalArgumentException(this + ": " + error);
        }
        File versionDir = new File(artifactIdDir, version.toVersionString());
        if (!versionDir.exists()) {
            throw new IllegalArgumentException(this + ": No such version found");
        }
        return collapsedInDirectory(versionDir);
    }

    public File collapsedFileIn(File dir) {
        return collapsedInDirectory(dir);
    }

    public URI collapsedUriIn(URI uri) {
        return toUri(collapsedInDirectory(Files.create(uri)));
    }

    public boolean sameVersion(Coordinate coordinate) {
        return EqHc.eq(Not.nil(coordinate, "coordinate").getVersion(), getVersion());
    }

    private StringBuilder getBaseBuilder() {
        return new StringBuilder(groupId).append(":").append(artifactId);
    }

    private URI in(URI repo, boolean validate, boolean fail) {
        if (version == null) {
            throw new IllegalStateException(this + " has no version, cannot resolve to URI in repo @ " + repo);
        }
        String ver = version.toVersionString();
        String artifactIdDir = slash(repo.getRawPath()) + slash(slash(slash(groupIdPath) + artifactId));
        String directory = slash(artifactIdDir + ver);
        URI directReference = toPath(repo, directory, ver);
        if (validate) {
            URI liveDirectReference = live(directReference);
            if (liveDirectReference != null) {
                return liveDirectReference;
            } else if (version.isSnapshot()) {
                return metadataDrivenReference(repo, directory, directReference);
            }
            if (fail) {
                throw new IllegalStateException
                        (this + " could not be found in " + repo + listVersionsForExceptionMessage(artifactIdDir));
            }
            return null;
        }
        return directReference;
    }

    private static String listVersionsForExceptionMessage(String directory) {
        try {
            List<Version> versions = versionsByIncreasingAge(new File(directory));
            return versions == null || versions.isEmpty() ? "" : ", found: " + versions;
        } catch (Exception ignore) {
            return "";
        }
    }

    private URI metadataDrivenReference(URI repo, String directory, URI directReference) {
        URI metadataUri = toURI(repo, directory + "maven-metadata.xml");
        Document metadata = readMetadata(metadataUri);
        if (metadata == null) {
            throw new IllegalStateException(this + " could not find metadata for " + directReference);
        }
        String buildNo = buildNo(metadata);
        String buildTime = buildTime(metadata);
        URI snapshotReference = toPath(repo, directory, buildTime + "-" + buildNo);
        return live(snapshotReference);
    }

    private URI toPath(URI repo, String directory, String ver) {
        String file =  directory + artifactId + "-" + ver + packaging();
        return toURI(repo, file);
    }

    private static String buildTime(Document metadata) {
        return Xml.content(metadata, "metadata", "versioning", "snapshot", "timestamp");
    }

    private static String buildNo(Document metadata) {
        return Xml.content(metadata, "metadata", "versioning", "snapshot", "buildNumber");
    }

    private static Document readMetadata(URI metadataUri) {
        return isFile(metadataUri)
                ? readFileMetadata(metadataUri)
                : readURLMetadata(metadataUri);
    }

    private static Document readURLMetadata(URI metadataUri) {
        URL url;
        try {
            url = metadataUri.toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid URI: " + metadataUri, e);
        }
        InputStream inputStream = null;
        URLConnection connection = null;
        try {
            connection = authenticated(url).openConnection();
            inputStream = connection.getInputStream();
            return Xml.readDocument(inputStream);
        } catch (IOException ignore) {
            return null;
        } finally {
            try {
                Closeables.close(inputStream);
            } finally {
                disconnectHttp(connection);
            }
        }
    }

    private static Document readFileMetadata(URI metadataUri) {
        File metadataFile = new File(metadataUri);
        if (metadataFile.isFile() && metadataFile.canRead()) {
            FileInputStream inputStream;
            try {
                inputStream = new FileInputStream(metadataFile);
            } catch (Exception ignore) {
                return null;
            }
            try {
                return Xml.readDocument(inputStream);
            } finally {
                Closeables.close(inputStream);
            }
        } else {
            return null;
        }
    }

    private static void disconnectHttp(URLConnection connection) {
        if (connection instanceof HttpURLConnection) {
            ((HttpURLConnection)connection).disconnect();
        }
    }

    private static URL authenticated(URL url) {
        if (url.getProtocol().toLowerCase().startsWith("http") && url.getUserInfo() != null) {
            final String[] userInfo = url.getUserInfo().split(":", 2);
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    char[] password = password(userInfo);
                    return new PasswordAuthentication(userInfo[0], password);
                }
            });
        }
        return url;
    }

    private static char[] password(String[] userInfo) {
        char[] password;
        if (userInfo.length > 1) {
            password = new char[userInfo[1].length()];
            userInfo[1].getChars(0, userInfo[1].length(), password, 0);
        } else {
            password = null;
        }
        return password;
    }

    private static URI live(URI uri) {
        return isHttp(uri) ? httpLive(uri)
                : isFile(uri) ? fileLive(uri)
                        : uri; // Oh, well ...
    }

    private static boolean isFile(URI uri) {
        return isScheme(uri, "file");
    }

    private static boolean isHttp(URI uri) {
        return isScheme(uri, "http");
    }

    private static boolean isScheme(URI uri, String scheme) {
        return uri.getScheme().toLowerCase().equals(scheme);
    }

    private static URI fileLive(URI uri) {
        File file = new File(uri);
        return file.isFile() && file.canRead() ? uri : null;
    }

    private static URI httpLive(URI uri) {
        HttpURLConnection conn = null;
        try {
            try {
                conn = (HttpURLConnection) uri.toURL().openConnection();
            } catch (IOException ignore) {
                return null;
            }
            if (isArchive(conn.getContentType().toLowerCase())) {
                return uri;
            }
        } finally {
            disconnectHttp(conn);
        }
        return null;
    }

    private File collapsedInDirectory(File dir) {
        File file = version == null ? singleMatchingArtifact(dir)
                : correctlyVersionedFile(dir, version);
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException
                    (this + " could not find " + (version == null ? "default" : version.toVersionString()) + " in " + dir);
        }
        try {
            return file.getAbsoluteFile().getCanonicalFile();
        } catch (IOException e) {
            throw new IORuntimeException("Failed to canonicalize file " + file, e);
        }
    }

    private File singleMatchingArtifact(File dir) {
        File[] files = dir.listFiles(new ArtifactIdFilter
                (this, "jar", "-sources.jar", "-javadoc.jar", "-tests.jar"));
        if (files == null || files.length == 0) {
            return null;
        }
        if (files.length > 1) {
            throw new IllegalStateException("Multiple artifacts in " + dir + ": " +
                    Arrays.asList(files));
        }
        return files[0];
    }

    private File correctlyVersionedFile(File dir, Version version) {
        if (version.isSnapshot()) {
            return new File(dir, VersionResolver.resolve(dir.listFiles(new PackagingFilter())));
        }
        File file = new File(dir, artifactId + "-" + version.toVersionString() + packaging());
        return file.exists() ? file : null;
    }

    private Version resolveVersion(File artifactIdDir) {
        if (version != null) {
            return version;
        }
        List<Version> versions = versionsByIncreasingAge(artifactIdDir);
        return versions == null ? null : versions.get(0);
    }

    private static List<Version> versionsByIncreasingAge(File artifactIdDir) {
        File[] directories = artifactIdDir.listFiles(new VersionDirectories());
        return directories == null || directories.length == 0 ? null
                : versionsByIncreasingAge(directories);
    }

    private class PackagingFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(packaging);
        }
    }

    private static boolean isArchive(String type) {
        for (String archiveType : ARCHIVE_TYPES) {
            if (type.contains(archiveType)) {
                return true;
            }
        }
        return false;
    }

    private static URI toURI(URI repo, String file) {
        try {
            return new URI(repo.getScheme(), repo.getUserInfo(), repo.getHost(), repo.getPort(),
                           file, repo.getQuery(), repo.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException
                    ("Invalid URI base: " + repo + " + " + file, e);
        }
    }

    private static List<Version> versionsByIncreasingAge(File[] directories) {
        List<Version> versions = Generic.list(directories.length);
        for (File directory : directories) {
            versions.add(new Version(directory.getName()));
        }
        Collections.sort(versions);
        Collections.reverse(versions);
        return versions;
    }

    private String packaging() {
        return "." + (packaging == null ? DEFAULT_PACKAGING : packaging);
    }

    private static final String GROUP_ID = "groupId";

    private static final String ARTIFACT_ID = "artifactId";

    private static final String VERSION = "version";

    private static final String TYPE = "type";

    private static final String DEFAULT_PACKAGING = "jar";

    private static final long serialVersionUID = 8763393780675226310L;

    private static String slash(String string) {
        return string.endsWith("/") ? string : string + "/";
    }

    private static URI toUri(File file) {
        try {
            return new URI("file://" + file);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid URL for " + file, e);
        }
    }

    private static String dedot(String type) {
        return type.startsWith(".") ? dedot(type.substring(1)) : type;
    }

    private static String[] split(String spec) {
        String[] split = spec.split(":", 4);
        if (split.length < 2) {
            throw new IllegalArgumentException("Malformed spec: " + spec);
        }
        return split;
    }

    @Override
    public int hashCode() {
        return EqHc.hc(groupId, artifactId, version, packaging);
    }

    @Override
    public boolean equals(Object obj) {
        Coordinate coordinate = EqHc.retyped(this, obj);
        return coordinate != null &&
                EqHc.eq(groupId, coordinate.groupId,
                        artifactId, coordinate.artifactId,
                        version, coordinate.version,
                        packaging, coordinate.packaging);
    }

    @Override
    public String toString() {
        return ToString.of
                (this, GROUP_ID, groupId,
                 ARTIFACT_ID, artifactId,
                 VERSION, version,
                 TYPE, packaging);
    }
}
