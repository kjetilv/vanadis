package vanadis.mvn;

import org.junit.Assert;
import org.junit.Test;
import static vanadis.mvn.VersionResolver.fileData;
import static vanadis.mvn.VersionResolver.resolve;

public class VersionResolverTest {
    private static final String NEWER_BUILD = "foo.bar-1.0-20090517.235501-234.jar";

    private static final String OLDER_BUILD = "foo.bar-1.0-20090509.111111-2340.jar";

    private static final String SNAPSHOT = "foo.bar-1.0-SNAPSHOT.jar";

    @Test
    public void olderSnapshotsDontWin() {
        String resolved = resolve(fileData(NEWER_BUILD, 1000),
                                  fileData(OLDER_BUILD, 1200),
                                  fileData(SNAPSHOT, 100));
        Assert.assertEquals("Expected newer build here, got snapshot",
                            NEWER_BUILD, resolved);
    }

    @Test
    public void newerSnapshotWins() {
        String resolved = resolve(fileData(NEWER_BUILD, 1000),
                                  fileData(OLDER_BUILD, 1200),
                                  fileData(SNAPSHOT, 1400));
        Assert.assertEquals("Expected snapshot here, got " + resolved,
                            SNAPSHOT, resolved);
    }
}
