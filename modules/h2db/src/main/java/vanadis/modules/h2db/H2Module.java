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
package net.sf.vanadis.modules.h2db;

import net.sf.vanadis.ext.AbstractModule;
import net.sf.vanadis.ext.Configure;
import net.sf.vanadis.ext.Expose;
import net.sf.vanadis.ext.Module;
import net.sf.vanadis.services.db.Connections;
import net.sf.vanadis.services.db.ConnectionsMBean;
import net.sf.vanadis.util.lang.ToString;

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
