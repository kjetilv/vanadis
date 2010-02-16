package vanadis.blueprints;

import junit.framework.Assert;
import org.junit.Test;
import vanadis.common.ver.Version;
import vanadis.mvn.Coordinate;

import java.net.URI;

public class URIPatternResolverTest {

    @Test public void check() {
        try {
            Assert.fail(new URIPatternResolver(null) + " is not valid");
        } catch (Exception ignore) { }
    }

    @Test public void simplePattern() {
        URIPatternResolver res = new URIPatternResolver
                ("file://foo/bar/${artifactId}/${groupId}/hoopla/${version}/${artifactId}-${version}.exe!");
        URI actual = res.resolve(BlueprintsReaderTest.bs(Coordinate.versioned("zip", "zot", new Version("3.2"))));
        URI expect = URI.create("file://foo/bar/zot/zip/hoopla/3.2/zot-3.2.exe!");
        Assert.assertEquals(expect, actual);
    }
}
