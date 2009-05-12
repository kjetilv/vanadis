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
package net.sf.vanadis.modules.remoting;

import net.sf.vanadis.core.io.Closeables;
import net.sf.vanadis.core.io.Location;
import net.sf.vanadis.core.lang.ToString;
import net.sf.vanadis.ext.AbstractContextAware;
import net.sf.vanadis.ext.Expose;
import net.sf.vanadis.ext.Inject;
import net.sf.vanadis.ext.Module;
import net.sf.vanadis.remoting.RemotingImpl;
import net.sf.vanadis.services.remoting.RemoteClientFactory;
import net.sf.vanadis.services.remoting.Remoting;
import net.sf.vanadis.services.remoting.RemotingInfrastructure;

@Module(moduleType = "remoting")
public class RemotingModule extends AbstractContextAware {

    private RemoteClientFactory clientFactory;

    private RemotingInfrastructure infrastructure;

    private Remoting remoting;

    @Inject
    public void setClientFactory(RemoteClientFactory clientFactory) {
        if (clientFactory == null) {
            try {
                stop();
            } finally {
                this.clientFactory = null;
            }
        } else {
            this.clientFactory = clientFactory;
        }
    }

    @Inject
    public void setInfrastructure(RemotingInfrastructure infrastructure) {
        if (infrastructure == null) {
            try {
                stop();
            } finally {
                this.infrastructure = null;
            }
        } else {
            this.infrastructure = infrastructure;
        }
    }

    private void stop() {
        if (remoting != null) {
            try {
                Closeables.close(remoting);
            } finally {
                remoting = null;
            }
        }
    }

    @Override
    public void closed() {
        stop();
    }

    @Override
    public void dependenciesResolved() {
        remoting = new RemotingImpl
                (infrastructure, clientFactory, getClass().getClassLoader());
        remoting.addLocator(context());
    }

    @Expose
    public Remoting getRemoting() {
        if (remoting == null) {
            throw new IllegalStateException(this + " has no remoting!");
        }
        return remoting;
    }

    @Expose(managed = true)
    public RemotingMBean getRemotingMBean() {
        return new RemotingMBeanImpl(this);
    }

    public Location getLocation() {
        return infrastructure.getLocation();
    }

    @Override
    public String toString() {
        return ToString.of(this, remoting, "infrastructure", clientFactory);
    }
}
