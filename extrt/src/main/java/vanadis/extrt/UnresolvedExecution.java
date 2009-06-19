package vanadis.extrt;

import org.osgi.framework.Bundle;
import vanadis.core.text.Printer;
import vanadis.ext.CommandExecution;
import vanadis.osgi.Context;

public class UnresolvedExecution implements CommandExecution {

    private final Bundles bundles;

    public UnresolvedExecution(Bundles bundles) {
        this.bundles = bundles;
    }

    @Override
    public void exec(String command, String[] args, Printer ps, Context context) {
        writeBundleResolveErrors(ps);
    }

    private void writeBundleResolveErrors(Printer p) {
        if (bundles.hasUnresolved()) {
            p.p("Unresolved bundles: ").p(bundles.uresolvedNames()).cr().ind();
            for (Long id : bundles.uresolvedIds()) {
                Bundle bundle = bundles.getBundle(id);
                String name = bundle.getSymbolicName();
                p.p(id).p(" [").p(name).p("] ").p(bundles.resolveError(name)).cr();
            }
            p.outd();
        }
    }
}
