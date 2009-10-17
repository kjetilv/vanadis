package vanadis.launcher;

import java.util.Properties;

public class PropertiesSpecs extends AbstractSiteSpecs<Properties> {

    public PropertiesSpecs(Properties source) {
        super(source);
    }

    @Override
    protected String parseOption(Properties source, String option, String def) {
        return source.getProperty(option, source.getProperty(prefixedOp(option), def));
    }

    private static String prefixedOp(String option) {
        return "vanadis." + option;
    }
}
