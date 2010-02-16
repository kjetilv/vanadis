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
package vanadis.common.io;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import vanadis.common.io.Files;
import vanadis.common.time.TimeSpan;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class FilesTest {

    @Before
    public void before() {
        TimeSpan.ms(2).sleep();
    }

    @Test
    public void testCreateDir() {
        File directory = fooBarDir();
        Assert.assertNotNull(directory);
        Assert.assertTrue(directory.exists());
        Assert.assertTrue(directory.isDirectory());
        directory.deleteOnExit();
    }

    @Test
    public void testCreateFile() throws IOException {
        File file = writtenFile();
        assertContents(file);
    }

    private void assertContents(File file) throws IOException {
        LineNumberReader reader = new LineNumberReader(new FileReader(file));
        Assert.assertEquals("line1", reader.readLine());
        Assert.assertEquals("line2", reader.readLine());
        String none = reader.readLine();
        Assert.assertNull("Not a null: " + none, none);
    }

    @Test
    public void testCopyFile() throws IOException {
        File file = writtenFile();
        File copy = Files.copy(file, new File(file.getParentFile(), file.getName() + ".copy"));
        copy.deleteOnExit();
        Assert.assertEquals(file.length(), copy.length());
        assertContents(copy);
    }

    private static File writtenFile() {
        File directory = fooBarDir();
        File file = new File(directory, "foo.txt");
        Files.writeFile(file, "line1", "line2");
        file.deleteOnExit();
        return file;
    }

    private static File fooBarDir() {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        return Files.createDirectory(tmpDir, "foo", "bar", "zot", String.valueOf
            (System.currentTimeMillis()));
    }

}
