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

import vanadis.ext.*;
import vanadis.jmx.Managed;

import javax.servlet.Servlet;

@Managed(desc = "Repo Servlet Module: Local repository access over HTTP")
@Module(moduleType = "reposerv")
public class RepoServletModule extends AbstractModule {

    @Configure private String repo;

    private RepoServlet repoServlet;

    private static final String REPO = "repo";

    private static final String SERVLET_ALIAS = "servlet.alias";

    @Override
    public void configured() {
        repoServlet = new RepoServlet(repo, REPO);
    }

    @Expose(properties = @Property(name = SERVLET_ALIAS, value = REPO))
    public Servlet getRepoServlet() {
        return repoServlet;
    }
}
