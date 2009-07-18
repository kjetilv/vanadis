package vanadis.main;

import vanadis.core.collections.Generic;
import vanadis.core.io.Location;
import vanadis.core.test.ForTestingPurposes;
import vanadis.launcher.SiteSpecs;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class ArgumentsSpecs implements SiteSpecs {

    private final List<String> blueprintPaths;

    private final List<String> blueprintResources;

    private final File home;

    private final Location location;

    private final List<String> blueprintNames;

    private final URI repoRoot;

    private List<String> uriPatterns;

    @ForTestingPurposes
    ArgumentsSpecs(String args) {
        this(Generic.linkedList(args.split("\\s")));
    }

    ArgumentsSpecs(List<String> args) {
        this.blueprintPaths = blueprintPathsArg(args);
        this.blueprintResources = blueprintResourcesArg(args);
        this.uriPatterns = uriPatterns(args);
        this.home = homeArg(args);
        this.location = locationArg(args);
        this.repoRoot = repoArg(args);
        List<String> names = Generic.list(blueprints(args));
        addRemainingArgumentsAsBlueprintNames(args, names);
        this.blueprintNames = Generic.seal(names);
    }

    @Override public List<String> getBlueprintPaths() {
        return blueprintPaths;
    }

    @Override public List<String> getUriPatterns() {
        return uriPatterns;
    }

    @Override public List<String> getBlueprintResources() {
        return blueprintResources;
    }

    @Override public File getHome() {
        return home;
    }

    @Override public Location getLocation() {
        return location;
    }

    @Override public List<String> getBlueprintNames() {
        return blueprintNames;
    }

    @Override public URI getRepoRoot() {
        return repoRoot;
    }

    private static void addRemainingArgumentsAsBlueprintNames(List<String> remainingArgs,
                                                              List<String> blueprintNames) {
        blueprintNames.addAll(remainingBlueprints(remainingArgs));
    }

    private static List<String> blueprintResourcesArg(List<String> args) {
        return split(parseOption(args, "blueprint-resources"));
    }

    private static List<String> blueprintPathsArg(List<String> args) {
        return split(parseOption(args, "blueprint-paths"));
    }

    private static List<String> blueprints(List<String> args) {
        return split(parseOption(args, "blueprints"));
    }

    private static List<String> uriPatterns(List<String> args) {
        return split(parseOption(args, "uri-patterns"));
    }

    private static List<String> remainingBlueprints(List<String> args) {
        if (args.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> names = Generic.list();
        for (String arg : args) {
            names.addAll(split(arg));
        }
        return names;
    }

    private static URI repoArg(List<String> args) {
        String repo = parseOption(args, "repo");
        if (repo == null) {
            String defaultRepo = parseOption(args, "defaultRepo");
            return defaultRepo == null ? null : URI.create(defaultRepo);
        }
        return URI.create(repo);
    }

    private static File homeArg(List<String> args) {
        String home = parseOption(args, "home");
        if (home == null) {
            String defaultHome = parseOption(args, "defaultHome");
            return defaultHome == null ? null
                    : new File(defaultHome).getAbsoluteFile();
        }
        return new File(home).getAbsoluteFile();
    }

    private static Location locationArg(List<String> args) {
        String spec = parseOption(args, "location");
        if (spec == null) {
            String defaultLocation = parseOption(args, "defaultLocation");
            return defaultLocation == null ? null
                    : Location.parse(defaultLocation);
        }
        return Location.parse(spec);
    }

    private static String parseOption(List<String> args, String option) {
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
        return null;
    }

    private static String dedash(String arg) {
        String dedashed = arg;
        while (dedashed.startsWith("-")) {
            dedashed = dedashed.substring(1);
        }
        return dedashed;
    }

    private static List<String> split(String string) {
        return string == null || string.trim().isEmpty()
                ? Collections.<String>emptyList()
                : Arrays.asList(string.split(","));
    }
}
