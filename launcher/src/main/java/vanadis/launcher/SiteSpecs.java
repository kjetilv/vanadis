package vanadis.launcher;

import vanadis.core.io.Location;

import java.io.File;
import java.net.URI;
import java.util.List;

public interface SiteSpecs {

    File getHome();

    Location getLocation();

    List<String> getBlueprintNames();

    List<String> getBlueprintPaths();

    List<String> getUriPatterns();

    List<String> getBlueprintResources();

    List<String> getLauncherClasses();

    URI getRepoRoot();
}
