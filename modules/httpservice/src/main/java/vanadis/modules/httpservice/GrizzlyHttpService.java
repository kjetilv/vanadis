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

import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.grizzly.standalone.StaticStreamAlgorithm;
import vanadis.core.collections.EnumerationIterable;
import vanadis.core.io.Location;
import vanadis.core.lang.ToString;
import vanadis.osgi.Context;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.Closeable;
import java.io.IOException;
import java.util.Dictionary;

final class GrizzlyHttpService implements HttpService, Closeable {

    private final Context context;

    private final Location location;

    private SelectorThread selectorThread;

    GrizzlyHttpService(Context context, Location location) {
        this.context = context;
        this.location = location;
    }

    @Override
    @SuppressWarnings({"RawUseOfParameterizedType"})
    public void registerServlet(String alias,
                                Servlet servlet,
                                Dictionary dictionary,
                                HttpContext httpContext)
            throws ServletException, NamespaceException {
        selectorThread.setAdapter(newAdapter(alias, servlet, dictionary));
        selectorThread.setDisplayConfiguration(true);
        try {
            selectorThread.listen();
        } catch (IOException e) {
            throw new ServletException(this + " failed to setup servlet " + servlet, e);
        } catch (InstantiationException e) {
            throw new ServletException(this + " failed to setup servlet " + servlet, e);
        }
    }

    private static ServletAdapter newAdapter(String alias,
                                             Servlet servlet,
                                             Dictionary<?, ?> dictionary) {
        ServletAdapter adapter = new ServletAdapter();
        adapter.setRootFolder(SelectorThread.getWebAppRootPath());
        adapter.setHandleStaticResources(true);

        // programatically configure the servlet (like web.xml usually doing)
        adapter.setServletInstance(servlet);
        for (Object key : EnumerationIterable.createGeneric(dictionary.keys())) {
            Object value = dictionary.get(key);
            if (value != null) {
                adapter.addInitParameter(String.valueOf(key), String.valueOf(value));
            }
        }
        adapter.setServletPath("/" + alias);
        return adapter;
    }

    @Override
    public void registerResources(String alias, String name, HttpContext httpContext) {
    }

    @Override
    public void unregister(String alias) {
    }

    @Override
    public HttpContext createDefaultHttpContext() {
        return new DefaultHttpContext(context);
    }

    @Override
    public void close() {
    }

    public void open() {
        selectorThread = new SelectorThread();
        selectorThread.setAlgorithmClassName(StaticStreamAlgorithm.class.getName());
        selectorThread.setPort(location.getPort());
    }

    @Override
    public String toString() {
        return ToString.of(this, "context", context, "selectorThread", selectorThread);
    }
}
