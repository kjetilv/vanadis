package vanadis.launcher;

import vanadis.core.collections.Generic;
import vanadis.core.test.ForTestingPurposes;

import java.util.List;

public class ArgumentsSpecs extends AbstractSiteSpecs<List<String>> {

    public ArgumentsSpecs(String args) {
        this(Generic.linkedList(args.split("\\s")));
    }

    public ArgumentsSpecs(List<String> args) {
        super(args);
    }

    @Override
    protected List<String> parseAdditionalBlueprintNames(List<String> blueprints, List<String> source) {
        List<String> names = Generic.list(blueprints);
        addRemainingArgumentsAsBlueprintNames(source, names);
        return names;
    }

    private static void addRemainingArgumentsAsBlueprintNames(List<String> remainingArgs,
                                                              List<String> blueprintNames) {
        blueprintNames.addAll(remainingBlueprints(remainingArgs));
    }

    @Override
    protected String parseOption(List<String> args, String option, String def) {
        for (int i = 0; i < args.size() - 1; i++) {
            if (args.get(i).startsWith("-")) {
                String arg = dedash(args.get(i).toLowerCase());
                if (option.toLowerCase().startsWith(arg)) {
                    try {
                        return args.get(i + 1);
                    } finally {
                        args.remove(i + 1);
                        args.remove(i);
                    }
                }
            }
        }
        return def;
    }

    private static String dedash(String arg) {
        String dedashed = arg;
        while (dedashed.startsWith("-")) {
            dedashed = dedashed.substring(1);
        }
        return dedashed;
    }
}
