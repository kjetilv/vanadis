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
package net.sf.vanadis.modules.networker.actions;

import net.sf.vanadis.core.time.TimeSpan;
import net.sf.vanadis.ext.RemoteConnection;
import net.sf.vanadis.services.networking.RemoteNode;
import net.sf.vanadis.util.concurrent.AbstractRetriableAction;

import java.util.concurrent.ScheduledExecutorService;

public class ConnectAction extends AbstractRetriableAction<RemoteNode> {

    private final RemoteConnection remoteConnection;

    public ConnectAction(TimeSpan retry, ScheduledExecutorService service,
                         RemoteNode target, RemoteConnection remoteConnection) {
        super(retry, service, target);
        this.remoteConnection = remoteConnection;
    }

    @Override
    protected void makeCall() {
        getTarget().connect(remoteConnection);
    }
}
