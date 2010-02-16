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
package vanadis.remoting;

import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vanadis.common.io.Location;
import vanadis.core.lang.ToString;
import vanadis.core.system.VM;
import vanadis.common.time.Time;
import vanadis.common.time.TimeSpan;
import vanadis.services.remoting.RemotingException;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

public abstract class AbstractHttpInfrastructure extends AbstractRemotingInfrastructure {

    private static final Logger log = LoggerFactory.getLogger(AbstractHttpInfrastructure.class);

    private static final String LOCALHOST = "localhost";

    protected static final TimeSpan DEFAULT_KEEPALIVE = TimeSpan.MINUTE;

    static final int DEFAULT_THREADS = 5;

    private static File ROOT;

    private final int coreThreads;

    private static File root() {
        File grizzlys = new File(VM.TMP, "grizzly-rootdirs");
        String runtimeName = VM.pid() + "-" + Time.mark().getEpoch();
        File directory = new File(grizzlys, runtimeName);
        try {
            ROOT = directory.getCanonicalFile();
        } catch (IOException ignore) {
            ROOT = directory;
        }
        return ROOT;
    }

    static {
        SelectorThread.setWebAppRootPath(root().getAbsolutePath());
    }

    private final int maxThreads;

    private final int keepAliveSeconds;

    private SelectorThread select;

    AbstractHttpInfrastructure(Location location,
                               TimeSpan keepAlive,
                               int coreThreads,
                               int maxThreads,
                               boolean endPoint) {
        super(location, endPoint);
        long seconds = (keepAlive == null ? DEFAULT_KEEPALIVE : keepAlive).secondTime();
        if (keepAlive != null && seconds > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    ("Excessive timeout: " + keepAlive.approximate());
        }
        this.keepAliveSeconds = (int) seconds;
        this.maxThreads = maxThreads == 0 ? DEFAULT_THREADS : maxThreads;
        this.coreThreads = coreThreads < this.maxThreads ? this.maxThreads : coreThreads;
    }

    @Override
    protected void setupServer(ClassLoader classLoader) {
        SelectorThread select = selectorThread(adapter(classLoader));
        try {
            select.start();
        } catch (Exception e) {
            throw new RemotingException(this + " failed to start", e);
        }
    }

    protected static File getRootPath() {
        return ROOT;
    }

    protected abstract GrizzlyAdapter adapter(ClassLoader classLoader);

    private SelectorThread selectorThread(GrizzlyAdapter adapter) {
        if (select == null) {
            select = newSelector(adapter);
        }
        return select;
    }

    private SelectorThread newSelector(GrizzlyAdapter adapter) {
        SelectorThread selectorThread = new SelectorThread();
        Location location = getLocation();
        if (location != null) {
            selectorThread.setInet(location.toInetAddress());
            selectorThread.setPort(location.getPort());
        }
        selectorThread.setKeepAliveTimeoutInSeconds(keepAliveSeconds);
        selectorThread.setCoreThreads(coreThreads);
        selectorThread.setMaxThreads(maxThreads);
        selectorThread.setAdapter(adapter);
        try {
            selectorThread.initEndpoint();
        } catch (Exception e) {
            throw new RemotingException(this + " failed to set up selector thread " + selectorThread +
                    " against " + adapter + "!", e);
        }
        return selectorThread;
    }

    @Override
    protected boolean tearDownServer(boolean force) {
        if (select == null || !select.isRunning()) {
            return true;
        }
        select.stopEndpoint();
        return !select.isRunning();
    }

    @Override
    public final String toString() {
        return ToString.of(this, select,
                           "keepAliveSeconds", keepAliveSeconds,
                           "coreThreads", coreThreads,
                           "maxThreads", maxThreads);
    }

    @Override
    protected Location subclassProvidedLocation() {
        return select == null
                ? null
                : new Location(getHostName(), getLowLevelPort());
    }

    private String getHostName() {
        InetAddress address = select.getAddress();
        return address == null ? LOCALHOST : address.getHostName();
    }

    private int getLowLevelPort() {
        try {
            return select.getPortLowLevel();
        } catch (Exception e) {
            log.warn(this + " failed to get low level port from " + select +
                    ", using 0", e);
            return 0;
        }
    }
}
