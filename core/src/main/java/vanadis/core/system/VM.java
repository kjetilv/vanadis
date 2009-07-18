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

package vanadis.core.system;

import javax.management.MBeanServer;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

public class VM {

    private static Integer pid;

    public static final String VERSION = System.getProperty("java.version");

    public static final boolean MUSTANG = isMajor("6");

    public static final boolean TIGER = isMajor("5");

    public static final String LN = System.getProperty("line.separator");

    public static final MBeanServer JMX = ManagementFactory.getPlatformMBeanServer();

    public static final File CWD = new File(System.getProperty("user.dir"));

    public static final File TMP = new File(System.getProperty("java.io.tmpdir"));

    public static final File HOME = new File(System.getProperty("user.home"));

    public static final String USER = System.getProperty("user.name");

    public static final URI HOME_URI = HOME.toURI();

    public static final String OS = System.getProperty("os.name");

    public static final String OS_VERSION = System.getProperty("os.version");

    static InetAddress host() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException ignore) {
            return null;
        }
    }

    public static final InetAddress LOCALHOST = host();

    public static final String HOST = LOCALHOST.getHostName();

    public static final String CANONICAL_HOST = LOCALHOST.getCanonicalHostName();

    private static boolean isMajor(String major) {
        return VERSION.startsWith("1." + major) || VERSION.startsWith(major);
    }

    public static int pid() {
        if (pid == null) {
            pid = fetchPid();
        }
        return pid;
    }

    private static int fetchPid() {
        String runtimeMBeanName = ManagementFactory.getRuntimeMXBean().getName();
        try {
            String[] runtimeSplit = runtimeMBeanName.split("@");
            if (runtimeSplit.length != 0) {
                return Integer.parseInt(runtimeSplit[0]);
            }
        } catch (Throwable e) {
            System.err.println("pid unavailable: " + e);
        }
        return -1;
    }

}
