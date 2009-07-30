package vanadis.blueprints;

import vanadis.core.lang.ToString;

import java.net.URI;

public class RelativeURIResolver implements BundleResolver {

    private final URI root;

    public RelativeURIResolver(URI root) {
        this.root = root;
    }

    @Override
    public URI resolve(BundleSpecification bundleSpecification) {
        URI repo = bundleSpecification.getRepo();
        URI uri = repo == null ? root : repo;
        return bundleSpecification.getCoordinate().uriIn(uri, true, false);
    }

    @Override
    public String toString() {
        return ToString.of(this, root);
    }
}
