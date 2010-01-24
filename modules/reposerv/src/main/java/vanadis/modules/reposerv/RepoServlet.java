/*
 * Copyright 2009 Kjetil Valstadsve
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
package vanadis.modules.reposerv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vanadis.core.io.Closeables;
import vanadis.core.io.Files;
import vanadis.core.io.IO;
import vanadis.core.lang.ToString;
import vanadis.ext.Attr;
import vanadis.ext.Managed;
import vanadis.mvn.Repo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

@Managed(desc = "Repo Servlet: Local repository access")
class RepoServlet extends HttpServlet {

    private final File repoFile;

    private final AtomicLong bytesServed = new AtomicLong();

    private final AtomicLong requestsProcessed = new AtomicLong();

    private final AtomicLong bundlesServed = new AtomicLong();

    private final String prefix;

    RepoServlet(String repo, String prefix) {
        this.prefix = prefix.startsWith("/") ? prefix : "/" + prefix;
        this.repoFile = repo == null ? Repo.DEFAULT : new File(repo);
        if (repoFile.canRead() && repoFile.isDirectory()) {
            log.info(this + " serving repo at " + repoFile);
        } else {
            throw new IllegalArgumentException("Not a good repo: " + repo);
        }
    }

    @Attr(desc = "Count: Requests processed total")
    public long getRequestsProcessed() {
        return requestsProcessed.get();
    }

    @Attr(desc = "Count: Bundles served total")
    public long getBundlesServed() {
        return bundlesServed.get();
    }

    @Attr(desc = "Count: Bytes served total")
    public long getBytesServed() {
        return bytesServed.get();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        doGet(req, res);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            serve(response, request.getPathInfo());
        } finally {
            requestsProcessed.incrementAndGet();
        }
    }

    private void serve(HttpServletResponse res, String pathInfo) {
        File file = validFile(repoFile, prefix, pathInfo);
        if (file == null) {
            return;
        }
        transferFile(res, pathInfo, file);
    }

    private void transferFile(HttpServletResponse res, String pathInfo, File file) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = Files.streamFile(file);
            try {
                outputStream = res.getOutputStream();
            } catch (IOException e) {
                log.info(this + " failed to serve resource " + pathInfo, e);
                return;
            }
            bytesServed.addAndGet(IO.copy(inputStream, outputStream));
        } finally {
            bundlesServed.incrementAndGet();
            Closeables.close(inputStream, outputStream);
        }
    }

    private static File validFile(File repoFile, String prefix, String pathInfo) {
        File file = new File(repoFile, stripped(prefix, pathInfo));
        if (!file.exists()) {
            log.info(file + " could not be served, not found");
        } else if (!file.isFile()) {
            log.info(file + " could not be served, not a file!");
        } else if (!file.canRead()) {
            log.info(file + " could not be served, cannot read!");
        } else {
            return file;
        }
        return null;
    }

    private static String stripped(String prefix, String path) {
        return path.startsWith(prefix) ? path.substring(prefix.length()) : path;
    }

    private static final Logger log = LoggerFactory.getLogger(RepoServlet.class);

    private static final long serialVersionUID = -6057331810490806039L;

    @Override
    public String toString() {
        return ToString.of(this, "bytesServed", bytesServed,
                           "bundlesServed", bundlesServed,
                           "requestsProcessed", requestsProcessed);
    }
}
