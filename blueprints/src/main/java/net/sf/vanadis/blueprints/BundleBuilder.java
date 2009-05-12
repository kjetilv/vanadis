package net.sf.vanadis.blueprints;

import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.core.properties.PropertySet;
import net.sf.vanadis.core.ver.Version;
import net.sf.vanadis.util.mvn.Coordinate;

import java.math.BigInteger;

final class BundleBuilder {

    private String group;

    private String artifact;

    private String version;

    private String artifactPrefix;

    private String groupPrefix;

    private static final String DOT = ".";

    private Integer startLevel;

    private Boolean globalProperties;

    private PropertySet propertySet;

    BundleBuilder copy() {
        return new BundleBuilder().
                setArtifact(artifact).
                setArtifactPrefix(artifactPrefix).
                setGroup(group).
                setGroupPrefix(groupPrefix).
                setStartLevel(startLevel).
                setArtifact(artifact).
                setVersion(version);
    }

    BundleBuilder setVersion(String version) {
        if (version != null) {
            this.version = version;
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
        String group = groupPrefix == null
                ? notNil(this.group, "group")
                : append(groupPrefix, this.group);
        String artifact = artifactPrefix == null
                ? notNil(this.artifact, "artifact")
                : append(artifactPrefix, this.artifact);
        Coordinate coordinate = this.version == null
                ? Coordinate.unversioned(group, artifact)
                : Coordinate.versioned(group, artifact, new Version(this.version));
        return BundleSpecification.create(coordinate, startLevel, propertySet, globalProperties);
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

    private static PropertySet append(PropertySet current, PropertySet addition) {
        return addition == null || addition.isEmpty() ? current
                : current == null ? addition
                        : current.with(addition);
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
