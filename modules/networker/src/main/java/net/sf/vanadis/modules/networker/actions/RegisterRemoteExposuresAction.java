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
import net.sf.vanadis.ext.RemoteExposure;
import net.sf.vanadis.services.networking.Router;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class RegisterRemoteExposuresAction extends AbstractExposuresAction {

    public RegisterRemoteExposuresAction(ScheduledExecutorService service, TimeSpan retry,
                                         Router target, List<RemoteExposure> remoteExposures) {
        super(retry, service, target, remoteExposures);
    }

    @Override
    protected void makeCall() {
        getTarget().registerRemoteExposures(getRemoteExposures());
    }
}
