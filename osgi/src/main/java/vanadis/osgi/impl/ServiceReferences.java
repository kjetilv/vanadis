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
package vanadis.osgi.impl;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

final class ServiceReferences {

    static String getServicePid(ServiceRegistration reg) {
        return getServicePid(reg.getReference());
    }

    static String getServicePid(ServiceReference ref) {
        Object object = ref.getProperty(Constants.SERVICE_PID);
        return object == null ? null : String.valueOf(object);
    }

    static long getServiceId(ServiceRegistration reg) {
        return getServiceId(reg.getReference());
    }

    static long getServiceId(ServiceReference ref) {
        Object object = ref.getProperty(Constants.SERVICE_ID);
        return object == null ? -1
                : object instanceof Long
                        ? (Long)object
                        : (object instanceof Integer
                                ? ((Integer)object).longValue()
                                : Long.parseLong(String.valueOf(object)));
    }

    private ServiceReferences() {
        // Don't make me
    }
}
