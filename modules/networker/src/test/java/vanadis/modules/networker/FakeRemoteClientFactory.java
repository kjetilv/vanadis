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
package net.sf.vanadis.modules.networker;

import net.sf.vanadis.osgi.impl.BareBonesContext;
import net.sf.vanadis.remoting.AbstractRemoteClientFactory;
import net.sf.vanadis.services.remoting.TargetHandle;

public class FakeRemoteClientFactory extends AbstractRemoteClientFactory {

    private final BareBonesContext context;

    public FakeRemoteClientFactory(BareBonesContext context) {
        this.context = context;
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public <T> T createClient(ClassLoader classLoader,
                              TargetHandle<T> targetHandle) {
        return (T) context.getReference(targetHandle.getClass());
    }

}