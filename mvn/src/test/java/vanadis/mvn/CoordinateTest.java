/*
 * Copyright 2008 Kjetil Valstadsve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vanadis.mvn;

import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import vanadis.common.io.Files;
import vanadis.core.test.VAsserts;
import vanadis.common.ver.Version;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class CoordinateTest {

    @Test
    public void artifactId() {
        Assert.assertEquals("foo.bar.zot", Coordinate.at("zip:foo.bar.zot:2.0-SNAPSHOT").getArtifactId());
    }

    @Test
    public void groupId() {
        Assert.assertEquals("zip", Coordinate.at("zip:foo.bar.zot:2.0-SNAPSHOT").getGroupId());
    }

    @Test
    public void version() {
        Assert.assertEquals(new Version("2.0-SNAPSHOT"), Coordinate.at("zip:foo.bar.zot:2.0-SNAPSHOT").getVersion());
    }

    @Test
    public void uris() throws URISyntaxException {
        Assert.assertEquals
            (new URI("http://my.repo.org/maven2/org/apache/framewerk/werk/0.1.2/werk-0.1.2.jar"),
             Coordinate.versioned("org.apache.framewerk", "werk", new Version("0.1.2")).uriIn
                 (new URI("http://my.repo.org/maven2/")));
        Assert.assertEquals
            (new URI("http://my.repo.org/maven2/org/apache/framewerk/werk/0.1.2/werk-0.1.2.jar"),
             Coordinate.versioned("org.apache.framewerk", "werk", new Version("0.1.2")).uriIn
                 (new URI("http://my.repo.org/maven2")));
    }

    @Test
    public void equality() {
        VAsserts.assertEqHcMatch(Coordinate.at("vanadis:vanadis.core:1.0-SNAPSHOT"),
                                 Coordinate.at("vanadis:vanadis.core:1.0-SNAPSHOT"));
        VAsserts.assertEqHcMatch(Coordinate.at("vanadis:vanadis.core:1.0-SNAPSHOT"),
                                 Coordinate.versioned("vanadis", "vanadis.core", new Version("1.0-SNAPSHOT")));
    }

    @Test @Ignore
    public void findUnversionedArtifact() {
        File file = Coordinate.at("vanadis:vanadis.core").fileIn(Repo.DEFAULT);
        Assert.assertNotNull(file);
        Assert.assertEquals("Expected the latest junit here",
                            "vanadis.core-1.0-SNAPSHOT.jar", file.getName());
    }

    @Test @Ignore
    public void findRawVersionedArtifact() {
        File versionDir = Files.getExistingDirectory(Repo.DEFAULT, "vanadis", "vanadis.core", "1.0-SNAPSHOT");
        File file = Coordinate.at("vanadis:vanadis.core:1.0-SNAPSHOT").collapsedFileIn(versionDir);
        Assert.assertTrue(file.getName().equals("vanadis.core-1.0-SNAPSHOT.jar"));
    }

    @Test @Ignore
    public void mapCoordinate() {
        File repo = Repo.DEFAULT;
        File jarFile = Coordinate.at("vanadis:vanadis.core:1.0-SNAPSHOT").fileIn(repo);
        Assert.assertTrue(jarFile.exists());
    }

    @Test
    public void packagingCoordinate() {
        Coordinate coordinate = Coordinate.at("foo:bar:jar:1.0");
        Assert.assertEquals(new Version("1.0"), coordinate.getVersion());
        Assert.assertEquals("jar", coordinate.getPackaging());

        Coordinate unpackaged = Coordinate.at("foo:bar:1.0");
        Assert.assertEquals(new Version("1.0"), unpackaged.getVersion());
        Assert.assertEquals("jar", unpackaged.getPackaging());
    }
}
