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

import vanadis.core.lang.EqHc;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;

import java.io.Serializable;
import java.net.*;
import java.util.regex.Pattern;

/**
 * A network location.
 */
public final class Location implements Serializable {

    public static Location parseRelative(Location base, String spec) {
        Integer portDelta = parsePortDelta(spec);
        return new Location(base.getHost(), base.getPort() + portDelta);
    }

    public static boolean isLocation(String value) {
        return LOCATION_PATTERN.matcher(value).matches();
    }

    public static Location parse(String spec) {
        if (validSpec(spec).contains(":")) {
            String[] strings = spec.split(":");
            if (strings.length == 2) {
                String host = strings[0];
                int port;
                try {
                    port = Integer.parseInt(strings[1].trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Malformed port: " + strings[1], e);
                }
                return new Location(host, port);
            } else {
                throw new IllegalArgumentException("Malformed host:port pair: " + spec);
            }
        } else {
            int port;
            try {
                port = Integer.parseInt(spec);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Malformed port: " + spec, e);
            }
            return new Location(port);
        }
    }

    private static String validSpec(String spec) {
        return Not.nil(spec, "location spec");
    }

    private final String host;

    private final int port;

    /**
     * Create a location on a given host.
     *
     * @param host Host
     * @param port Port
     */
    public Location(String host, int port) {
        this.host = Not.nil(host, "host name").trim();
        this.port = port;
    }

    /**
     * Create a location on localhost.
     *
     * @param port Port
     */
    public Location(int port) {
        this(LOCALHOST, port);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Location adjustLocation(int value) {
        return new Location(getHost(), getPort() + value);
    }

    /**
     * Get an InetAddress for this {@link #getHost() host}.
     *
     * @return InetAddress
     */
    public InetAddress toInetAddress() {
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new IllegalStateException(this + " could not be resolved", e);
        }
    }

    /**
     * Get an InetSocketAddress for this {@link #getHost() host} and {@link #getPort() port}.
     *
     * @return InetSocketAddress
     */
    public InetSocketAddress toInetSocketAddress() {
        return new InetSocketAddress(toInetAddress(), port);
    }

    /**
     * Express this location as a secure HTTP URL.
     *
     * @return URL
     */
    public URL toSecureHttpUrl() {
        return toUrl(HTTPS);
    }

    /**
     * Express this location as an HTTP URL.
     *
     * @return URL
     */
    public URL toHttpUrl() {
        return toUrl(HTTP);
    }

    /**
     * Express this location as a URL.
     *
     * @param protocol Protocol
     * @return URL
     */
    public URL toUrl(String protocol) {
        try {
            return new URL(protocol, getHost(), getPort(), ROOT);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Illegal protocol: " + protocol, e);
        }
    }

    public String toRmiUrl(String name) {
        StringBuilder header = body(new StringBuilder(ROOT_SEPARATOR));
        return header.append(ROOT).append(name).append(ROOT).toString();
    }

    public String toLocationString() {
        return body(null).toString();
    }

    private StringBuilder body(StringBuilder builder) {
        return (builder == null ? new StringBuilder() : builder).append(host).append(":").append(port);
    }

    public Location incrementPort() {
        return incrementPort(1);
    }

    public Location incrementPort(int delta) {
        return new Location(host, port + delta);
    }

    private static final long serialVersionUID = -2063806770384653512L;

    private static final String HTTP = "http";

    private static final String HTTPS = HTTP + "s";

    private static final String ROOT = "/";

    private static final String ROOT_SEPARATOR = "//";

    private static final String LOCALHOST = "localhost";

    private static final Pattern LOCATION_PATTERN = Pattern.compile(".*\\s*:\\s*\\d{1,5}\\s*\\z");

    private static Integer parsePortDelta(String spec) {
        try {
            return Integer.parseInt(spec);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port delta: " + spec, e);
        }
    }

    @Override
    public boolean equals(Object object) {
        Location location = EqHc.retyped(this, object);
        return location == this || location != null && EqHc.eq(host, location.host, port, location.port);
    }

    @Override
    public int hashCode() {
        return EqHc.hc(host, port);
    }

    @Override
    public String toString() {
        return ToString.of(this, body(null));
    }
}
