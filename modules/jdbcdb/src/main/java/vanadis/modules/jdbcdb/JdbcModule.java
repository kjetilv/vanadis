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

import vanadis.core.lang.ToString;
import vanadis.ext.AbstractModule;
import vanadis.ext.Configure;
import vanadis.ext.Expose;
import vanadis.ext.Module;
import vanadis.services.db.Connections;
import vanadis.services.db.ConnectionsMBean;

@Module(moduleType = "jdbcdb")
public class JdbcModule extends AbstractModule {

    @Configure(def = "sa")
    private String user;

    @Configure(def = "sa")
    private String passwd;

    @Configure
    private String driverClassName;

    @Configure
    private String connectionUrl;

    private JdbcConnections jdbcConnections;

    private JdbcConnectionsMBean jdbcConnectionsMBean;

    public JdbcModule() {
        this(null, null, null, null);
    }

    public JdbcModule(String user, String passwd, String driverClassName, String connectionUrl) {
        this.user = user;
        this.passwd = passwd;
        this.driverClassName = driverClassName;
        this.connectionUrl = connectionUrl;
    }

    public String getUser() {
        return user;
    }

    public String getPasswd() {
        return passwd;
    }

    @Expose(managed = true)
    public ConnectionsMBean getConnectionsMBean() {
        return jdbcConnectionsMBean;
    }

    @Expose
    public Connections getConnections() {
        return jdbcConnections;
    }

    @Override
    public void dependenciesResolved() {
        jdbcConnections = new JdbcConnections(connectionUrl, driverClassName, user, passwd);
        jdbcConnectionsMBean = new JdbcConnectionsMBean(jdbcConnections);
    }

    @Override
    public String toString() {
        return ToString.of(this, connectionUrl,
                           "user", user,
                           "passwd", passwd,
                           "driver", driverClassName);
    }
}
