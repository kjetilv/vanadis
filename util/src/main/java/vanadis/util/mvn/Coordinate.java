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

package vanadis.util.mvn;

import vanadis.core.collections.Generic;
import vanadis.core.io.Files;
import vanadis.core.io.IORuntimeException;
import vanadis.core.lang.EqHc;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;
import vanadis.core.ver.Version;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The coordinate class models resolving of resources against a standard maven 2
 * type repository.
 */
public final class Coordinate implements Serializable {

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
        return this.version == null;
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

    public URI uriIn(URI repo) {
        return in(repo);
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

    private URI in(URI repo) {
        if (version == null) {
            throw new IllegalStateException(this + " has no version, cannot resolve to URI in repo @ " + repo);
        }
        String ver = version.toVersionString();
        String subpath = slash(slash(slash(groupIdPath) + artifactId) + ver);
        String file = slash(repo.getRawPath()) + subpath + artifactId + "-" + ver + packaging();
        try {
            return new URI(repo.getScheme(),
                           repo.getUserInfo(),
                           repo.getHost(), repo.getPort(),
                           file, repo.getQuery(), repo.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI base: " + repo, e);
        }
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
        File[] directories = artifactIdDir.listFiles(new VersionDirectories());
        if (directories == null || directories.length == 0) {
            return null;
        }
        List<Version> versions = versionsByIncreasingAge(directories);
        return versions.get(0);
    }

    private class PackagingFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(packaging);
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
