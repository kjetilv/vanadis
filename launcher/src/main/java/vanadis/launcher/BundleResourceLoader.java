package vanadis.launcher;

import vanadis.blueprints.ResourceLoader;
import vanadis.core.lang.Not;
import org.osgi.framework.BundleContext;

import java.net.URL;

public class BundleResourceLoader implements ResourceLoader {

    private final BundleContext bundleContext;

    @Override
    public URL get(String res) {
        return bundleContext.getBundle().getResource(res);
    }

    public BundleResourceLoader(BundleContext bundleContext) {
        this.bundleContext = Not.nil(bundleContext, "bundle context");
    }
}
