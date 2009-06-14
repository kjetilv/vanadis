package vanadis.blueprints;

import java.net.URI;

public interface BundleResolver {

    URI resolve(BundleSpecification bundleSpecification);
}