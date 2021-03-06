package vanadis.blueprints;

import vanadis.common.ver.Version;
import vanadis.core.lang.ToString;
import vanadis.core.properties.PropertySet;
import vanadis.mvn.Coordinate;

import java.math.BigInteger;
import java.net.URI;

final class BundleBuilder {

    private String group;

    private String artifact;

    private String artifactPrefix;

    private String groupPrefix;

    private static final String DOT = ".";

    private Integer startLevel;

    private Boolean globalProperties;

    private PropertySet propertySet;

    private String configurationPid;

    private String repo;

    private String uri;

    private String version;

    BundleBuilder() {
        this(null, null);
    }

    BundleBuilder(String defaultVersion, String defaultRepo) {
        this.version = defaultVersion;
        this.repo = defaultRepo;
    }

    BundleBuilder copy() {
        return new BundleBuilder(version, repo).
                setUri(uri).
                setArtifact(artifact).
                setArtifactPrefix(artifactPrefix).
                setGroup(group).
                setGroupPrefix(groupPrefix).
                setStartLevel(startLevel).
                setArtifact(artifact);
    }

    BundleBuilder setVersion(String version) {
        if (version != null) {
            this.version = version;
        }
        return this;
    }

    BundleBuilder setRepo(String repo) {
        if (repo != null) {
            this.repo = repo;
        }
        return this;
    }

    BundleBuilder setConfigurationPid(String configurationPid) {
        if (configurationPid != null) {
            this.configurationPid = configurationPid;
        }
        return this;
    }

    BundleBuilder setUri(String uri) {
        if (uri != null) {
            this.uri = uri;
        }
        return this;
    }

    BundleBuilder setGroup(String group) {
        if (group != null) {
            this.group = group;
        }
        return this;
    }

    BundleBuilder setArtifact(String artifact) {
        if (artifact != null) {
            this.artifact = artifact;
        }
        return this;
    }

    BundleBuilder setArtifactPrefix(String artifactPrefix) {
        if (artifactPrefix != null) {
            this.artifactPrefix = artifactPrefix;
        }
        return this;
    }

    BundleBuilder setGroupPrefix(String groupPrefix) {
        if (groupPrefix != null) {
            this.groupPrefix = groupPrefix;
        }
        return this;
    }

    BundleBuilder setStartLevel(Integer startLevel) {
        if (startLevel != null) {
            this.startLevel = startLevel;
        }
        return this;
    }

    BundleBuilder addArtifactPrefix(String artifactPrefix) {
        this.artifactPrefix = append(this.artifactPrefix, artifactPrefix);
        return this;
    }

    BundleBuilder addGroupPrefix(String groupPrefix) {
        this.groupPrefix = append(this.groupPrefix, groupPrefix);
        return this;
    }

    BundleSpecification build() {
        if (uri != null) {
            return BundleSpecification.createFixed(URI.create(uri), startLevel, propertySet, globalProperties);
        }
        if (artifact == null) {
            throw new IllegalStateException(this + " has no uri or artifact");
        }
        String group = groupPrefix == null
                ? notNil(this.group, "group")
                : append(groupPrefix, this.group);
        String artifact = artifactPrefix == null
                ? notNil(this.artifact, "artifact")
                : append(artifactPrefix, this.artifact);
        Coordinate coordinate = coordinate(group, artifact);
        return BundleSpecification.create(repoUri(), coordinate,
                                          startLevel, propertySet,
                                          globalProperties, configurationPid);
    }

    BundleBuilder addPropertySet(PropertySet propertySet) {
        this.propertySet = append(this.propertySet, propertySet);
        return this;
    }

    BundleBuilder setStartLevel(BigInteger startLevel) {
        if (startLevel != null) {
            this.startLevel = startLevel.intValue();
        }
        return this;
    }

    BundleBuilder setGlobalProperties(Boolean globalProperties) {
        if (globalProperties != null) {
            this.globalProperties = globalProperties;
        }
        return this;
    }

    BundleBuilder setPropertySet(PropertySet propertySet) {
        if (propertySet != null) {
            this.propertySet = propertySet;
        }
        return this;
    }

    private Coordinate coordinate(String group, String artifact) {
        return this.version == null
                ? Coordinate.unversioned(group, artifact)
                : Coordinate.versioned(group, artifact, new Version(this.version));
    }

    private URI repoUri() {
        return repo == null ? null : URI.create(repo);
    }

    private <T> T notNil(T t, String what) {
        if (t == null) {
            throw new IllegalStateException(this + " missing " + what);
        }
        return t;
    }

    private static String append(String current, String addition) {
        return addition == null || addition.trim().isEmpty() ? current
                : current == null ? addition
                        : sensiblyAppended(current, addition);
    }

    private static String sensiblyAppended(String current, String addition) {
        boolean leftEnd = current.endsWith(DOT);
        boolean rightEnd = addition.startsWith(DOT);
        return leftEnd && rightEnd ? current + addition.substring(1)
                : leftEnd || rightEnd ? current + addition
                        : current + DOT + addition;
    }

    private static PropertySet append(PropertySet current, PropertySet addition) {
        return addition == null || addition.isEmpty() ? current
                : current == null ? addition
                        : current.with(addition);
    }

    @Override
    public String toString() {
        return ToString.of(this, "group", group,
                           "artifact", artifact,
                           "version", version,
                           "artifactPrefix", artifactPrefix,
                           "groupPrefix", groupPrefix,
                           "startLevel", startLevel,
                           "globalProperties", globalProperties,
                           "propertySet", propertySet);
    }
}
