package vanadis.launchsite;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import vanadis.launcher.*;

public class LaunchBundleActivator implements BundleActivator {

    private LaunchSite launchSite;

    @Override
    public void start(BundleContext bundleContext) {
        launchSite = LaunchSite.create(siteSpecs(),
                new VanadisLauncher(bundleContext),
                new BundleResourceLoader(bundleContext));
        if (!launchSite.launch(System.out)) {
            System.err.println("Startup failed");
        }
    }

    private SiteSpecs siteSpecs() {
        return new PropertiesSpecs(System.getProperties());
    }

    @Override
    public void stop(BundleContext bundleContext) {
        launchSite.close();
    }
}
