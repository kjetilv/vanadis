package vanadis.blueprints;

import vanadis.core.collections.Generic;
import vanadis.core.lang.ToString;
import vanadis.core.lang.VarArgs;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class ManyBundleResolvers implements BundleResolver {

    private final Iterable<BundleResolver> bundleResolvers;

    ManyBundleResolvers(BundleResolver... bundleResolvers) {
        this(Generic.list(bundleResolvers));
    }

    ManyBundleResolvers(Iterable<BundleResolver> bundleResolvers, BundleResolver... andMore) {
        List<BundleResolver> brs = Generic.list(bundleResolvers);
        if (VarArgs.present(andMore)) {
            brs.addAll(Arrays.asList(andMore));
        }
        this.bundleResolvers = brs;
    }

    ManyBundleResolvers append(BundleResolver bundleResolver) {
        List<BundleResolver> list = listMine();
        list.add(bundleResolver);
        return new ManyBundleResolvers(list);
    }

    ManyBundleResolvers prepend(BundleResolver bundleResolver) {
        List<BundleResolver> list = Generic.list(bundleResolver);
        list.addAll(listMine());
        return new ManyBundleResolvers(list);
    }

    ManyBundleResolvers append(Iterable<BundleResolver> bundleResolvers) {
        List<BundleResolver> list = listMine();
        list.addAll(Generic.list(bundleResolvers));
        return new ManyBundleResolvers(list);
    }

    ManyBundleResolvers prepend(Iterable<BundleResolver> bundleResolvers) {
        List<BundleResolver> list = Generic.list(bundleResolvers);
        list.addAll(listMine());
        return new ManyBundleResolvers(list);
    }

    private List<BundleResolver> listMine() {
        return Generic.list(this.bundleResolvers);
    }

    @Override
    public URI resolve(BundleSpecification bundleSpecification) {
        URI uri;
        for (BundleResolver bundleResolver : bundleResolvers) {
            uri = bundleResolver.resolve(bundleSpecification);
            if (uri != null) {
                String scheme = uri.getScheme().toLowerCase();
                if (scheme.equals("http")) {
                    return uri;
                }
                if (scheme.equals("file")) {
                    if (new File(uri).exists()) {
                        return uri;
                    }
                }
                if (scheme.equals("mvn")) {
                    return uri;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return ToString.of(this, bundleResolvers);
    }
}
