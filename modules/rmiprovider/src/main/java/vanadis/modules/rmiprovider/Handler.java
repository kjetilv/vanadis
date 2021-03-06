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
package vanadis.modules.rmiprovider;

import vanadis.core.lang.Not;
import vanadis.remoting.AbstractHandler;
import vanadis.remoting.MethodCall;
import vanadis.remoting.MethodCallResult;
import vanadis.services.remoting.RemotingException;
import vanadis.services.remoting.TargetHandle;

import java.rmi.RemoteException;

class Handler extends AbstractHandler {

    private final RemoteInvoker invoker;

    Handler(TargetHandle<?> handle, RemoteInvoker invoker) {
        super(handle);
        this.invoker = Not.nil(invoker, "invoker");
    }

    @Override
    protected MethodCallResult invoke(MethodCall methodCall) {
        try {
            return invoker.perform(methodCall);
        } catch (RemoteException e) {
            throw new RemotingException
                    (this + " failed to invoke " + methodCall, e);
        }
    }

}
