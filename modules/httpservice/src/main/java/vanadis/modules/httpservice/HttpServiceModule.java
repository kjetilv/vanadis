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
package vanadis.modules.httpservice;

import vanadis.core.collections.Generic;
import vanadis.core.io.Closeables;
import vanadis.core.io.Location;
import vanadis.ext.*;
import vanadis.osgi.ServiceProperties;
import org.osgi.service.http.HttpService;

import javax.servlet.Servlet;
import java.io.File;
import java.util.Map;
import java.util.Set;

@Module(moduleType = "httpservice")
public class HttpServiceModule extends AbstractContextAware {

    @Configure(def = "+80")
    private Location location;

    @Configure(def = "./var/HTTPROOT")
    private File root;

    private GrizzlyHttpService grizzlyHttpService;

    private final Map<String, ServletRegistration> aliasedServlets = Generic.map();

    private final Set<ServletRegistration> servletRegistrations = Generic.set();

    @Override
    public void dependenciesResolved() {
        grizzlyHttpService = new GrizzlyHttpService(requiredContext(), location);
    }

    @Expose
    public HttpService getHttpService() {
        return grizzlyHttpService;
    }

    @Inject(required = false)
    public void addServlet(Servlet servlet, ServiceProperties<Servlet> sp) {
        newRegistration(new ServletRegistration(servlet, sp));
    }

    @Retract
    public void removeServlet(Servlet servlet) {
        String alias = aliasOf(servlet);
        if (alias != null) {
            try {
                grizzlyHttpService.unregister(alias);
            } finally {
                aliasedServlets.remove(alias);
            }
        }
    }

    private void newRegistration(ServletRegistration servletRegistration) {
        if (grizzlyHttpService != null) {
            servletRegistration.activate(grizzlyHttpService);
        } else {
            servletRegistrations.add(servletRegistration);
        }
        aliasedServlets.put(servletRegistration.getAlias(), servletRegistration);
    }

    private String aliasOf(Servlet servlet) {
        String alias = null;
        for (Map.Entry<String, ServletRegistration> entry : aliasedServlets.entrySet()) {
            if (entry.getValue().isFor(servlet)) {
                alias = entry.getKey();
            }
        }
        return alias;
    }

    @Override
    public void activate() {
        grizzlyHttpService.open();
        try {
            for (ServletRegistration servletRegistration : servletRegistrations) {
                servletRegistration.activate(grizzlyHttpService);
            }
        } finally {
            servletRegistrations.clear();
        }
    }

    @Override
    public void closed() {
        for (ServletRegistration servletRegistration : servletRegistrations) {
            servletRegistration.deactivate(grizzlyHttpService);
        }
        Closeables.close(grizzlyHttpService);
    }
}
