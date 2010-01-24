package vanadis.modules.examples.scalamodule;

import org.osgi.framework._

class BundleActivator extends org.osgi.framework.BundleActivator {

  def start(context: BundleContext) {
    var bundleNames = context.getBundles().
            map(b => b.getSymbolicName()).
            filter(b => b != context.getBundle());
    printf("Installed bundles: {0}", bundleNames.toString());
  }

  def stop(context: BundleContext) {}
}
