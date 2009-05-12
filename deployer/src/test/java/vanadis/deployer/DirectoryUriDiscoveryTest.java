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
package vanadis.deployer;

import static junit.framework.Assert.*;
import vanadis.core.collections.Generic;
import vanadis.core.system.VM;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DirectoryUriDiscoveryTest {

    private final List<String> strings = Generic.list();

    private final AtomicBoolean match = new AtomicBoolean(true);

    private final AtomicBoolean updated = new AtomicBoolean(false);

    private final AbstractDirectoryUriExplorer disco = new ListDiscoverer();

    @After
    public void after() {
        strings.clear();
        updated.set(false);
        match.set(true);
    }

    @Test
    public void discoverNone() {
        DiscoveredUris ls = disco.discover();
        assertTrue(ls.news().isEmpty());
        assertTrue(ls.updates().isEmpty());
        assertTrue(ls.removes().isEmpty());
    }

    @Test
    public void discoverOne() {
        Collection<URI> urls = discoverFooDotBar().news();
        assertEquals(1, urls.size());
    }

    private DiscoveredUris discoverFooDotBar() {
        strings.add("foo.bar");
        return disco.discover();
    }

    @Test
    public void discoverOneOnce() {
        discoverOne();
        assertEquals(0, disco.discover().news().size());
    }

    @Test
    public void discoverOneAndAnUpdate() {
        URI url = singleURI(discoverFooDotBar().news());
        updated.set(true);
        DiscoveredUris ls = disco.discover();
        assertEquals(0, ls.news().size());
        assertEquals(1, ls.updates().size());
        assertSame(url, ls.updates().iterator().next());
    }

    @Test
    public void nowYouDiscoverNowYouDont() {
        URI url = singleURI(discoverFooDotBar().news());
        strings.clear();
        DiscoveredUris ls = disco.discover();
        assertEquals(0, ls.news().size());
        assertEquals(0, ls.updates().size());
        assertEquals(1, ls.removes().size());
        assertSame(url, ls.removes().iterator().next());
    }

    private static URI singleURI(Collection<URI> urls) {
        return urls.iterator().next();
    }

    private class ListDiscoverer extends AbstractDirectoryUriExplorer {

        private ListDiscoverer() {
            super(VM.TMP);
        }

        @Override
        protected String[] list(File root) {
            return strings.toArray(new String[strings.size()]);
        }

        @Override
        protected boolean updated(Discovery oldDiscovery, File currentFile) {
            return updated.get();
        }

        @Override
        protected boolean isMatch(File file) {
            return match.get();
        }
    }
}
