package vanadis.blueprints;

import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;
import vanadis.core.properties.PropertySet;
import vanadis.core.properties.PropertySets;
import vanadis.core.text.Resolve;
import vanadis.mvn.Coordinate;

import java.net.URI;

public class URIPatternResolver implements BundleResolver {

    private final String pattern;

    public URIPatternResolver(String pattern) {
        this.pattern = Not.nil(pattern, "pattern");
    }

    /**
     * @param bundleSpecification Bundle specification
     * @return URI
     */
    @Override
    public URI resolve(BundleSpecification bundleSpecification) {
        if (bundleSpecification.getCoordinate().isVersioned()) {
            try {
                return URI.create(Resolve.resolve(pattern, props(bundleSpecification.getCoordinate())));
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException
                        (this + " could not transform " + bundleSpecification + " to URI, invalid pattern!", e);
            }
        }
        throw new IllegalArgumentException(bundleSpecification + " must be versioned");
    }

    private static PropertySet props(Coordinate coordinate) {
        return PropertySets.create("groupId", coordinate.getGroupId(),
                                   "artifactId", coordinate.getArtifactId(),
                                   "version", coordinate.getVersion().toVersionString());
    }

    @Override
    public String toString() {
        return ToString.of(this, pattern);
    }
}
