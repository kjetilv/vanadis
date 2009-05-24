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
package vanadis.modules.networker.actions;

import vanadis.core.time.TimeSpan;
import vanadis.services.networking.RemoteExposure;
import vanadis.services.networking.Router;
import vanadis.util.concurrent.AbstractRetriableAction;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public abstract class AbstractExposuresAction extends AbstractRetriableAction<Router> {

    private final List<RemoteExposure> remoteExposures;

    AbstractExposuresAction(TimeSpan retry,
                            ScheduledExecutorService service,
                            Router target,
                            List<RemoteExposure> remoteExposures) {
        super(retry, service, target);
        this.remoteExposures = remoteExposures;
    }

    List<RemoteExposure> getRemoteExposures() {
        return remoteExposures;
    }
}
