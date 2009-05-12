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
package net.sf.vanadis.modules.httpservice;

import net.sf.vanadis.osgi.Context;
import org.osgi.service.http.HttpContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;

class DefaultHttpContext implements HttpContext {

    private final Context context;

    DefaultHttpContext(Context context) {
        this.context = context;
    }

    @Override
    public boolean handleSecurity(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        return true;
    }

    @Override
    public URL getResource(String url) {
        try {
            return context.getResource(url).toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(this + " failed to load " + url, e);
        }
    }

    @Override
    public String getMimeType(String s) {
        return null;
    }
}
