package vanadis.mojo.runner;

import org.apache.maven.plugin.MojoExecutionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @goal run
 * @inheritByDefault true
 * @noinspection JavaDoc,UnusedDeclaration
 */
public class ExecVanadisMojo extends ExecJavaMojo {

    /**
     * @parameter
     */
    private String blueprintPaths;

    /**
     * @parameter
     */
    private String blueprintResources;

    /**
     * @parameter
     */
    private String blueprintSheets;

    /**
     * @parameter
     */
    private String location;

    /**
     * @parameter
     */
    private String home;

    protected String[] parseCommandlineArgs() throws MojoExecutionException {
        List args = new ArrayList(Arrays.asList(super.parseCommandlineArgs()));
        add("blueprint-paths", blueprintPaths, args);
        add("blueprint-resources", blueprintResources, args);
        add("location", location, args);
        add("home", home, args);
        add("blueprint-sheets", blueprintSheets, args);
        return (String[])args.toArray(new String[args.size()]);
    }

    private static void add(String preamble, String value, List args) {
        if (value != null) {
            args.add("-" + preamble);
            args.add(value);
        }
    }
}
