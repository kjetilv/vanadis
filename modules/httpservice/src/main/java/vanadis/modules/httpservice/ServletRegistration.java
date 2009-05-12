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
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;
import vanadis.core.properties.PropertySet;
import vanadis.osgi.ServiceProperties;

import javax.servlet.Servlet;
import java.util.Dictionary;

final class ServletRegistration {

    private static final String SERVLET_ALIAS = "servlet.alias";

    private final String alias;

    private final Dictionary<String, Object> dictionary;

    private static String newAlias(Servlet servlet, ServiceProperties<Servlet> serviceProperties) {
        PropertySet propertySet = serviceProperties.getPropertySet();
        if (propertySet.has(SERVLET_ALIAS)) {
            return propertySet.getString(SERVLET_ALIAS);
        }
        String info = servlet.getServletInfo();
        if (info == null) {
            return ToString.objectToString(servlet);
        }
        return info;
    }

    private final Servlet servlet;

    private final ServiceProperties<Servlet> serviceProperties;

    ServletRegistration(Servlet servlet, ServiceProperties<Servlet> serviceProperties) {
        this.servlet = Not.nil(servlet, "servlet");
        this.serviceProperties = serviceProperties;
        this.alias = slashed(newAlias(servlet, serviceProperties));
        dictionary = Generic.dictionary(serviceProperties.getPropertySet().toMap());
    }

    public String getAlias() {
        return alias;
    }

    public boolean isFor(Servlet servlet) {
        return servlet.equals(this.servlet);
    }

    public void activate(GrizzlyHttpService grizzlyHttpService) {
        try {
            grizzlyHttpService.registerServlet(alias, servlet, dictionary, null);
        } catch (Exception e) {
            throw new IllegalArgumentException
                    (this + " failed to register " + servlet + " on " + serviceProperties, e);
        }
    }

    public void deactivate(GrizzlyHttpService grizzlyHttpService) {
        grizzlyHttpService.unregister(alias);
    }

    private static final String SLASH = "/";

    private static String slashed(String string) {
        return string.startsWith(SLASH) ? string : SLASH + string;
    }
}
