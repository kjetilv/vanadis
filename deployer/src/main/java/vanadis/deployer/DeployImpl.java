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
package vanadis.deployer;

import vanadis.blueprints.BundleSpecification;
import vanadis.blueprints.ModuleSpecification;
import vanadis.osgi.Context;
import vanadis.osgi.Registration;
import vanadis.util.log.Log;
import vanadis.util.log.Logs;

import java.net.URI;
import java.util.Collection;

final class DeployImpl implements Deploy {

    private final Context context;

    DeployImpl(Context context) {
        this.context = context;
    }

    @Override
    public Registration<BundleSpecification> deployBundle(URI uri) {
        return register(BundleSpecification.create(uri, null, null));
    }

    @Override
    public Registration<ModuleSpecification> deployModule(URI uri) {
        return register(ModuleSpecification.createFrom(uri));
    }

    private <T> Registration<T> register(T object) {
        return register(object, null);
    }

    private <T> Registration<T> register(T object, Collection<Throwable> exceptions) {
        try {
            Registration<T> registration = context.register(object);
            log.info("Registered " + object);
            return registration;
        } catch (RuntimeException e) {
            if (exceptions == null) {
                throw new IllegalArgumentException("Unable to deploy " + object, e);
            }
            exceptions.add(e);
            log.error(this + " failed to register URI " + object, e);
            return null;
        }
    }

    private static final Log log = Logs.get(DeployImpl.class);
}
