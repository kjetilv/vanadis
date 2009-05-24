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

import vanadis.core.lang.ToString;
import vanadis.ext.AbstractModule;
import vanadis.ext.Configure;
import vanadis.ext.Expose;
import vanadis.ext.Module;
import vanadis.services.db.Connections;
import vanadis.services.db.ConnectionsMBean;

import java.io.File;

@Module(moduleType = "h2db")
public class H2Module extends AbstractModule {

    @Configure(def = "sa")
    private String user;

    @Configure(def = "sa")
    private String passwd;

    @Configure
    private File file;

    @Configure(def = "vanadis-mem")
    private String memoryDatabase;

    private H2Connections h2Connections;

    private H2ConnectionsMBean h2ConnectionsMBean;

    public H2Module() {
        this(null, null, null, null);
    }

    public H2Module(String user, String passwd, File file, String memoryDatabase) {
        this.user = user;
        this.passwd = passwd;
        this.file = file;
        this.memoryDatabase = memoryDatabase;
    }

    public String getUser() {
        return user;
    }

    public String getPasswd() {
        return passwd;
    }

    public File getFile() {
        return file;
    }

    public String getMemoryDatabase() {
        return memoryDatabase;
    }

    @Expose(managed = true)
    public ConnectionsMBean getConnectionsMBean() {
        return h2ConnectionsMBean;
    }

    @Expose
    public Connections getConnections() {
        return h2Connections;
    }

    @Override
    public void dependenciesResolved() {
        h2Connections = file == null
                ? new H2Connections(memoryDatabase, user, passwd)
                : new H2Connections(file, user, passwd);
        h2ConnectionsMBean = new H2ConnectionsMBean(h2Connections);
    }

    @Override
    public String toString() {
        return ToString.of(this, "memoryDatabase", memoryDatabase,
                           "file", file,
                           "user", user,
                           "passwd", passwd);
    }
}
