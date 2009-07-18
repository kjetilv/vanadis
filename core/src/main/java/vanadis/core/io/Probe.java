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

package vanadis.core.io;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public final class Probe {

    @SuppressWarnings({"UnusedCatchParameter"})
    public static boolean detectedActivity(Location location) {
        InetAddress address = address(location);
        Socket socket = null;
        try {
            socket = new Socket(address, location.getPort());
        } catch (ConnectException ignore) {
            return false; // OK, we expected no connection here.
        } catch (IOException e) {
            throw new IllegalArgumentException("Illegal location: " + location, e);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignore) { }
            }
        }
        return true;
    }

    private static InetAddress address(Location location) {
        try {
            return InetAddress.getByName(location.getHost());
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Illegal location host: " + location, e);
        }
    }

}
