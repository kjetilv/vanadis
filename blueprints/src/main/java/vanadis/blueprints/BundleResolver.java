package vanadis.blueprints;

import vanadis.util.mvn.Coordinate;

import java.net.URI;

public interface BundleResolver {

    URI resolve(Coordinate coordinate);
}