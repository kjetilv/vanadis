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
package vanadis.modules.networker;

import vanadis.concurrent.AbstractRetrier;
import vanadis.common.time.TimeSpan;
import vanadis.modules.networker.actions.RegisterRemoteExposuresAction;
import vanadis.modules.networker.actions.RegisterRemoteInjectPointsAction;
import vanadis.modules.networker.actions.UnregisterRemoteExposuresAction;
import vanadis.modules.networker.actions.UnregisterRemoteInjectPointsAction;
import vanadis.services.networking.RemoteExposure;
import vanadis.services.networking.RemoteInjectPoint;
import vanadis.services.networking.Router;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

final class RouterRetrier extends AbstractRetrier<Router> implements Router {

    RouterRetrier(TimeSpan retry,
                  ScheduledExecutorService service,
                  Router target) {
        super(retry, service, target);
    }

    @Override
    public void registerRemoteInjectPoints(List<RemoteInjectPoint> remoteInjectPoints) {
        submit(new RegisterRemoteInjectPointsAction(getRetry(), getService(), getTarget(), remoteInjectPoints));
    }

    @Override
    public void unregisterRemoteInjectPoints(List<RemoteInjectPoint> remoteInjectPoints) {
        submit(new UnregisterRemoteInjectPointsAction(getRetry(), getService(), getTarget(), remoteInjectPoints));
    }

    @Override
    public void registerRemoteExposures(List<RemoteExposure> remoteExposures) {
        submit(new RegisterRemoteExposuresAction(getService(), getRetry(), getTarget(), remoteExposures));
    }

    @Override
    public void unregisterRemoteExposures(List<RemoteExposure> remoteExposures) {
        submit(new UnregisterRemoteExposuresAction(getService(), getRetry(), getTarget(), remoteExposures));
    }
}
