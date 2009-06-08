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

package vanadis.modules.jdbcdb;

import vanadis.core.lang.Not;
import vanadis.core.lang.Strings;
import vanadis.core.lang.ToString;
import vanadis.services.db.Connections;
import vanadis.services.db.DbException;
import vanadis.util.log.Log;
import vanadis.util.log.Logs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcConnections implements Connections {

    private static final Log log = Logs.get(JdbcConnections.class);

    private final String connectionUrl;

    private final String driverClassName;

    private final String user;

    private final String passwd;

    private final boolean readOnly;

    public JdbcConnections(String connectionUrl, String driverClassName, String user, String passwd, boolean readOnly) {
        this.user = user;
        this.passwd = passwd;
        this.readOnly = readOnly;
        this.connectionUrl = Not.nil(connectionUrl, "connection url");
        this.driverClassName = Not.nil(driverClassName, "driver class name");
        try {
            Class.forName(this.driverClassName);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid driver: " + this.driverClassName, e);
        }
        log.info(this + " created");
    }

    @Override
    public Connection get() {
        return get(user, passwd);
    }

    @Override
    public Connection get(String user, String passwd) {
        Connection connection = newConnection(user, passwd);
        setReadOnly(connection);
        return connection;
    }

    @SuppressWarnings({"IfMayBeConditional"}) // ButItWouldn'tBeSoEasyToReadTheStacktrace...
    private Connection newConnection(String user, String passwd) {
        try {
            if (Strings.isEmpty(user)) {
                return DriverManager.getConnection(connectionUrl);
            } else {
                return DriverManager.getConnection(connectionUrl, user, passwd);
            }
        } catch (Exception e) {
            throw new DbException(this + " failed to open url", e);
        }
    }

    private void setReadOnly(Connection connection) {
        if (readOnly) {
            try {
                connection.setReadOnly(readOnly);
            } catch (SQLException e) {
                throw new DbException(this + " failed to set readOnly on connection " + connection, e);
            }
        }
    }

    @Override
    public void drop(Connection connection) {
        try {
            connection.close();
        } catch (Exception e) {
            log.warn(this + " failed to close " + connection, e);
        }
    }

    @Override
    public String toString() {
        return ToString.of(this, connectionUrl, "user", user, "passwd", passwd, "class", driverClassName);
    }
}
