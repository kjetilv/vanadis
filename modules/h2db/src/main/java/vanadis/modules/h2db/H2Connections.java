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

package vanadis.modules.h2db;

import org.h2.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vanadis.core.lang.ToString;
import vanadis.services.db.Connections;
import vanadis.services.db.DbException;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

public class H2Connections implements Connections {

    static {
        Driver.load();
    }

    private static final Logger log = LoggerFactory.getLogger(H2Connections.class);

    private final String user;

    private final String passwd;

    private final String url;

    public H2Connections(File file) {
        this(file, null, null, null);
    }

    public H2Connections(String memoryDatabase) {
        this(null, memoryDatabase, null, null);
    }

    public H2Connections(File file, String user, String passwd) {
        this(file, null, user, passwd);
    }

    public H2Connections(String memoryDatabase, String user, String passwd) {
        this(null, memoryDatabase, user, passwd);
    }

    private H2Connections(File file, String memoryDatabase,
                          String user, String passwd) {
        this.user = user;
        this.passwd = passwd;
        this.url = url(file, memoryDatabase);
        log.info(this + " created");
    }

    @Override
    public Connection get() {
        if (user == null || passwd == null) {
            throw new IllegalArgumentException("No user or password set, use other get method");
        }
        return get(user, passwd);
    }

    @Override
    public Connection get(String user, String passwd) {
        try {
            return DriverManager.getConnection(url, user, passwd);
        } catch (Exception e) {
            throw new DbException(this + " failed to open url " + url, e);
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

    private static String url(File file, String memoryDatabase) {
        return "jdbc:h2:" + (file != null
                ? file.getAbsolutePath()
                : "mem:" + (memoryDatabase == null ? "" : memoryDatabase));
    }

    @Override
    public String toString() {
        return ToString.of(this, url, "user", user, "passwd", passwd);
    }
}
