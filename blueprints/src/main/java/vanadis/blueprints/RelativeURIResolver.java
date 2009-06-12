package vanadis.blueprints;

import vanadis.core.lang.ToString;
import vanadis.util.mvn.Coordinate;

import java.net.URI;

public class RelativeURIResolver implements BundleResolver {

    private final URI root;

    public RelativeURIResolver(URI root) {
        this.root = root;
    }

    @Override
    public URI resolve(Coordinate coordinate) {
        return coordinate.uriIn(root);
    }

    @Override
    public String toString() {
        return ToString.of(this, root);
    }
}
