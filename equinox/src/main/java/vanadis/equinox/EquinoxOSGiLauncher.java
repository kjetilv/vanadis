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

package vanadis.equinox;

import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.osgi.framework.BundleContext;
import vanadis.launcher.AbstractOSGiLauncher;
import vanadis.launcher.ShutdownException;
import vanadis.launcher.StartupException;

import java.util.Map;

public class EquinoxOSGiLauncher extends AbstractOSGiLauncher {
    private static final String EQUINOX_EXPORTS = "org.eclipse.osgi.event;version=\"1.0\",org.eclipse.osgi." +
            "framework.console;version=\"1.0\",org.eclipse.osgi.framework.eventmgr;v" +
            "ersion=\"1.1\",org.eclipse.osgi.framework.log;version=\"1.0\",org.eclipse" +
            ".osgi.service.datalocation;version=\"1.1\",org.eclipse.osgi.service.deb" +
            "ug;version=\"1.0\",org.eclipse.osgi.service.environment;version=\"1.1\",o" +
            "rg.eclipse.osgi.service.localization;version=\"1.0\",org.eclipse.osgi.s" +
            "ervice.pluginconversion;version=\"1.0\",org.eclipse.osgi.service.resolv" +
            "er;version=\"1.2\",org.eclipse.osgi.service.runnable;version=\"1.1\",org." +
            "eclipse.osgi.service.security; version=\"1.0\",org.eclipse.osgi.service" +
            ".urlconversion;version=\"1.0\",org.eclipse.osgi.signedcontent; version=" +
            "\"1.0\",org.eclipse.osgi.storagemanager;version=\"1.0\",org.eclipse.osgi." +
            "util;version=\"1.1\",org.osgi.framework;version=\"1.4\",org.osgi.service." +
            "condpermadmin;version=\"1.0\",org.osgi.service.packageadmin;version=\"1." +
            "2\",org.osgi.service.permissionadmin;version=\"1.2\",org.osgi.service.st" +
            "artlevel;version=\"1.1\",org.osgi.service.url;version=\"1.0\",org.osgi.ut" +
            "il.tracker;version=\"1.3.3\",org.eclipse.core.runtime.adaptor;x-friends" +
            ":=\"org.eclipse.core.runtime\",org.eclipse.core.runtime.internal.adapto" +
            "r;x-internal:=true,org.eclipse.core.runtime.internal.stats;x-friends:" +
            "=\"org.eclipse.core.runtime\",org.eclipse.osgi.baseadaptor;x-internal:=" +
            "true,org.eclipse.osgi.baseadaptor.bundlefile;x-internal:=true,org.ecl" +
            "ipse.osgi.baseadaptor.hooks;x-internal:=true,org.eclipse.osgi.baseada" +
            "ptor.loader;x-internal:=true,org.eclipse.osgi.framework.adaptor;x-int" +
            "ernal:=true,org.eclipse.osgi.framework.debug;x-internal:=true,org.ecl" +
            "ipse.osgi.framework.internal.core;x-internal:=true,org.eclipse.osgi.f" +
            "ramework.internal.protocol;x-internal:=true,org.eclipse.osgi.framewor" +
            "k.internal.protocol.bundleentry;x-internal:=true,org.eclipse.osgi.fra" +
            "mework.internal.protocol.bundleresource;x-internal:=true,org.eclipse." +
            "osgi.framework.internal.protocol.reference;x-internal:=true,org.eclip" +
            "se.osgi.framework.internal.reliablefile;x-internal:=true,org.eclipse." +
            "osgi.framework.launcher;x-internal:=true,org.eclipse.osgi.framework.u" +
            "til;x-internal:=true,org.eclipse.osgi.internal.baseadaptor;x-internal" +
            ":=true,org.eclipse.osgi.internal.module;x-internal:=true,org.eclipse." +
            "osgi.internal.profile;x-internal:=true,org.eclipse.osgi.internal.reso" +
            "lver;x-internal:=true,org.eclipse.osgi.internal.provisional.service.s" +
            "ecurity; x-friends:=\"org.eclipse.equinox.security.ui\";version=\"1.0.0\"" +
            ",org.eclipse.osgi.internal.provisional.verifier;x-friends:=\"org.eclip" +
            "se.update.core,org.eclipse.ui.workbench,org.eclipse.equinox.p2.artifa" +
            "ct.repository\",org.eclipse.osgi.internal.service.security;x-friends:=" +
            "\"org.eclipse.equinox.security.ui\",org.eclipse.osgi.internal.signedcon" +
            "tent; x-internal:=true";

    @Override
    protected BundleContext launchedBundleContext() {
        Map<String,Object> map = getStandardPackagesConfiguration();
        set(map, "vanadis.home", getHome().toASCIIString());
        set(map, "vanadis.location", getLocation().toLocationString());
        set(map, "eclipse.ignoreApp", "true");
        set(map, "eclipse.startTime", Long.toString(System.currentTimeMillis()));
        set(map, "osgi.clean", "true");
        set(map, "osgi.compatibility.bootdelegation", "true");
        set(map, EclipseStarter.PROP_CONSOLE, "");
        set(map, EclipseStarter.PROP_NOSHUTDOWN, "true");
        BundleContext bundleContext = newBundleContext();
        runIt();
        return bundleContext;
    }

    private static void set(Map<String,Object> map, String key, String value) {
        map.put(key, value);
    }

    private static void runIt() {
        try {
            EclipseStarter.run(null);
        } catch (Exception e) {
            throw new StartupException("Failed to run application", e);
        }
    }

    private static BundleContext newBundleContext() {
        try {
            return EclipseStarter.startup(new String[]{"-console"}, null);
        } catch (Exception e) {
            throw new StartupException("Failed to startup", e);
        }
    }

    @Override
    protected void stopFramework() {
        try {
            EclipseStarter.shutdown();
        } catch (Exception e) {
            throw new ShutdownException(this + " failed to shutdown", e);
        }
    }

    @Override
    protected String osgiExports() {
        return EQUINOX_EXPORTS;
    }
}