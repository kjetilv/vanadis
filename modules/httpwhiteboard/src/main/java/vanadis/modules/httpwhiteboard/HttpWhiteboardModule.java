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
package net.sf.vanadis.modules.httpwhiteboard;

import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.ext.AutoLaunch;
import net.sf.vanadis.ext.Inject;
import net.sf.vanadis.ext.Module;
import net.sf.vanadis.ext.Retract;
import net.sf.vanadis.osgi.ServiceProperties;
import net.sf.vanadis.util.log.Log;
import net.sf.vanadis.util.log.Logs;
import org.osgi.service.http.HttpService;

import javax.servlet.Servlet;
import java.util.Collection;
import java.util.Map;

@Module(moduleType = "httpwhiteboard", launch = @AutoLaunch(name = "httpwhiteboard"))
public class HttpWhiteboardModule {

    private final Collection<HttpService> httpServices = Generic.list();

    private final Map<String, ServletRegistration> servlets = Generic.linkedHashMap();

    private final Map<Servlet, String> aliasedServlets = Generic.linkedHashMap();

    @Inject(required = false)
    public void addHttpService(HttpService service) {
        store(service);
        add(service);
    }

    @Retract
    public void removeHttpService(HttpService service) {
        if (clearedService(service)) {
            remove(service);
        }
    }

    @Inject(required = false)
    public void addServlet(Servlet servlet, ServiceProperties<Servlet> properties) {
        ServletRegistration registration = new ServletRegistration(servlet, properties);
        String alias = availableAlias(registration);
        storeAlias(servlet, registration, alias);
        add(registration);
    }

    @Retract
    public void removeServlet(Servlet servlet) {
        String alias = clearedAlias(servlet);
        if (alias == null) {
            log.info(this + " was asked to remove unknown servlet " + servlet);
        } else {
            remove(clearedRegistration(alias));
        }
    }

    private String availableAlias(ServletRegistration registration) {
        String alias = registration.getAlias();
        if (inUse(alias)) {
            throw new IllegalArgumentException(this + " could not add " + registration + ", alias already in use!");
        }
        return alias;
    }

    private boolean inUse(String alias) {
        return servlets.containsKey(alias);
    }

    private void storeAlias(Servlet servlet, ServletRegistration registration, String alias) {
        servlets.put(alias, registration);
        aliasedServlets.put(servlet, alias);
    }

    private ServletRegistration clearedRegistration(String alias) {
        return servlets.remove(alias);
    }

    private String clearedAlias(Servlet servlet) {
        return aliasedServlets.remove(servlet);
    }

    private boolean clearedService(HttpService service) {
        return httpServices.remove(service);
    }

    private void store(HttpService service) {
        httpServices.add(service);
    }

    private Iterable<ServletRegistration> servlets() {
        return servlets.values();
    }

    private void add(HttpService service) {
        for (ServletRegistration registration : servlets()) {
            registration.activateIn(service);
        }
    }

    private void remove(HttpService service) {
        for (ServletRegistration registration : servlets()) {
            registration.deactivateIn(service);
        }
    }

    private void add(ServletRegistration registration) {
        for (HttpService service : httpServices) {
            registration.activateIn(service);
        }
    }

    private void remove(ServletRegistration registration) {
        for (HttpService service : httpServices) {
            registration.deactivateIn(service);
        }
    }

    private static final Log log = Logs.get(HttpWhiteboardModule.class);

    @Override
    public String toString() {
        return ToString.of(this,
                           "servlets", servlets.size(),
                           "services", httpServices.size(),
                           "aliases", servlets);
    }
}
