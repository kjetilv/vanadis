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
package net.sf.vanadis.modules.httpprovider;

import net.sf.vanadis.core.lang.Proxies;
import net.sf.vanadis.remoting.AbstractRemoteClientFactory;
import net.sf.vanadis.services.remoting.TargetHandle;

public class HttpRemoteClientFactory extends AbstractRemoteClientFactory {

    @Override
    public <T> T createClient(ClassLoader classLoader, TargetHandle<T> targetHandle) {
        return Proxies.genericProxy(classLoader,
                                    targetHandle.getReference().getTargetInterface(),
                                    new Handler<T>(targetHandle,
                                                   classLoader));
    }
}