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
package vanadis.modules.jmxsetup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vanadis.core.collections.Generic;
import vanadis.core.jmx.Jmx;
import vanadis.core.lang.ToString;
import vanadis.ext.*;
import static vanadis.ext.CoreProperty.OBJECTNAME;
import static vanadis.ext.CoreProperty.OBJECTNAME_NAME;

import vanadis.osgi.ServiceProperties;

import javax.management.DynamicMBean;
import javax.management.ObjectName;
import java.util.Map;

@Module(moduleType = "jmxsetup", launch = @AutoLaunch(name = "jmxsetup"))
public class JmxSetupModule extends AbstractModule {

    private static final Map<Object, ObjectName> objects = Generic.identityMap();

    @SuppressWarnings({"unchecked"})
    @Inject(properties = @Property(name = OBJECTNAME_NAME), required = false)
    public void addJmxService(Object service, ServiceProperties<?> serviceProperties) {
        ObjectName objectName = OBJECTNAME.lookupIn(serviceProperties.getPropertySet());
        register(service, objectName, (ServiceProperties<Object>) serviceProperties);
    }

    @Retract
    public void removeJmxService(Object service) {
        unregister(service);
    }

    @Override
    public void closed() {
        for (ObjectName objectName : objects.values()) {
            unregister(objectName);
        }
    }

    private void unregister(Object service) {
        ObjectName objectName = objects.remove(service);
        unregister(objectName);
    }

    private <T> void register(T service, ObjectName objectName, ServiceProperties<T> serviceProperties) {
        try {
            if (service instanceof DynamicMBean) {
                Jmx.registerJmx(objectName, (DynamicMBean) service);
            } else {
                Class<T> mbeanType = serviceProperties.getMainClass();
                Jmx.registerJmx(objectName, mbeanType.cast(service), mbeanType);
            }
        } catch (Exception e) {
            log.error(this + " failed to register " + service + " at " + objectName, e);
        } finally {
            objects.put(service, objectName);
        }
    }

    private void unregister(ObjectName objectName) {
        if (objectName != null) {
            try {
                Jmx.unregisterJmx(objectName);
            } catch (Exception e) {
                log.error(this + " failed to unregister " + objectName, e);
            }
        }
    }

    private static final Logger log = LoggerFactory.getLogger(JmxSetupModule.class);

    @Override
    public String toString() {
        return ToString.of(this, "registrations", objects.size());
    }
}
