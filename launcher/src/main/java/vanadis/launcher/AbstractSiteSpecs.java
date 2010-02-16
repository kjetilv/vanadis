package vanadis.launcher;

import vanadis.core.collections.Generic;
import vanadis.common.io.Location;
import vanadis.core.lang.Strings;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"OverridableMethodCallDuringObjectConstruction"})
public abstract class AbstractSiteSpecs<T> implements SiteSpecs {

    private final List<String> blueprintPaths;

    private final List<String> blueprintResources;

    private final File home;

    private final Location location;

    private final List<String> blueprintNames;

    private final URI repoRoot;

    private final List<String> uriPatterns;

    private final List<String> launcherClasses;

    private final T source;

    protected AbstractSiteSpecs(T source) {
        this.source = source;
        blueprintPaths = blueprintPathsArg();
        uriPatterns = uriPatterns();
        blueprintResources = blueprintResourcesArg();
        home = homeArg();
        repoRoot = repoArg();
        location = locationArg();
        launcherClasses = launcherClass();
        blueprintNames = parseAdditionalBlueprintNames(blueprints(), source);
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

    @Override
    public List<String> getBlueprintNames() {
        return blueprintNames;
    }

    @Override public URI getRepoRoot() {
        return repoRoot;
    }

    @Override public List<String> getLauncherClasses() {
        return launcherClasses;
    }

    private List<String> blueprintResourcesArg() {
        return split(parseOption(source, "blueprint-resources"));
    }

    private List<String> blueprintPathsArg() {
        return split(parseOption(source, "blueprint-paths"));
    }

    private List<String> blueprints() {
        return split(parseOption(source, "blueprints"));
    }

    private List<String> uriPatterns() {
        return split(parseOption(source, "uri-patterns"));
    }

    private List<String> launcherClass() {
        return split(parseOption
                (source, "launcher-class", "vanadis.felix.FelixOSGiLauncher,vanadis.equinox.EquinoxOSGiLauncher"));
    }

    protected static List<String> remainingBlueprints(List<String> args) {
        if (args.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> names = Generic.list();
        for (String arg : args) {
            names.addAll(split(arg));
        }
        return names;
    }

    private URI repoArg() {
        String repo = parseOption(source, "repo");
        if (repo == null) {
            String defaultRepo = parseOption(source, "defaultRepo");
            return defaultRepo == null ? null : toUri(defaultRepo);
        }
        return toUri(repo);
    }

    private static URI toUri(String repo) {
        URI uri = URI.create(repo);
        if (uri.isAbsolute()) {
            return uri;
        }
        String scheme = uri.getScheme();
        if (Strings.isBlank(scheme)) {
            return toUri(new File(repo));
        }
        if (scheme.toLowerCase().trim().equals("file")) {
            return toUri(new File(uri.getPath()));
        }
        throw new IllegalArgumentException("Invalid repo: " + repo);
    }

    private static URI toUri(File file) {
        return file.getAbsoluteFile().toURI();
    }

    private File homeArg() {
        String home = parseOption(source, "home");
        if (home == null) {
            String defaultHome = parseOption(source, "defaultHome");
            return defaultHome == null ? null
                    : new File(defaultHome).getAbsoluteFile();
        }
        return new File(home).getAbsoluteFile();
    }

    private Location locationArg() {
        String spec = parseOption(source, "location");
        if (spec == null) {
            String defaultLocation = parseOption(source, "defaultLocation");
            return defaultLocation == null ? null
                    : Location.parse(defaultLocation);
        }
        return Location.parse(spec);
    }

    private String parseOption(T source, String option) {
        return parseOption(source, option, null);
    }

    protected abstract String parseOption(T source, String option, String def);

    private static List<String> split(String string) {
        return string == null || string.trim().isEmpty()
                ? Collections.<String>emptyList()
                : Arrays.asList(string.split(","));
    }

    protected List<String> parseAdditionalBlueprintNames(List<String> blueprintNames, T source) {
        return blueprintNames;
    }
}
