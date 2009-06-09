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

package vanadis.extrt;

import vanadis.blueprints.ModuleSpecification;
import vanadis.core.collections.Generic;
import vanadis.core.properties.PropertySet;
import vanadis.core.text.Printer;
import vanadis.ext.CommandExecution;
import vanadis.ext.CoreProperty;
import vanadis.objectmanagers.*;
import vanadis.osgi.Context;
import vanadis.osgi.Filter;
import vanadis.osgi.Reference;
import vanadis.osgi.ServiceProperties;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

final class ListExecution implements CommandExecution {

    @Override
    public void exec(String command, String[] args, Printer p, Context context) {
        boolean verbose = verbose(args);
        String type = type(args);
        CoreProperty<String> property = property(args);
        Set<String> moduleNames = moduleNames(context);
        if (property == CoreProperty.OBJECTMANAGER_TYPE) {
            p.p("Managed object factories:").cr();
            writeObjectManagerFactoryReferences(p, context, property, type, verbose);
        }
        p.p("Managed objects:").cr();
        writeObjectManagerReferences(p, context, property, type, verbose, moduleNames);
        if (!moduleNames.isEmpty()) {
            p.p("Orphan modules: ").cr();
            p.p("  ").p(moduleNames).cr();
        }
    }

    private static Set<String> moduleNames(Context context) {
        Collection<Reference<ModuleSpecification>> modRefs = context.getReferences(ModuleSpecification.class);
        Set<String> moduleNames = Generic.set();
        for (Reference<ModuleSpecification> modRef : modRefs) {
            ModuleSpecification service = modRef.getService();
            try {
                moduleNames.add(service.getName() + ":" + service.getType());
            } finally {
                modRef.unget();
            }
        }
        return moduleNames;
    }

    private static CoreProperty<String> property(String[] args) {
        for (String arg : args) {
            if (args != null && !arg.equalsIgnoreCase("-v")) {
                return arg.startsWith("name=") ? CoreProperty.OBJECTMANAGER_NAME : CoreProperty.OBJECTMANAGER_TYPE;
            }
        }
        return null;
    }

    private static String type(String[] args) {
        for (String arg : args) {
            if (args != null && !arg.equalsIgnoreCase("-v")) {
                return arg.startsWith("type=") || arg.startsWith("name=") ? arg.substring(5) : arg;
            }
        }
        return null;
    }

    private static boolean verbose(String[] args) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase("-v")) {
                return true;
            }
        }
        return false;
    }

    private static <T> void writeObjectManagerFactoryReferences(Printer p, Context context,
                                                                CoreProperty<T> property, T type, boolean verbose) {
        Collection<Reference<ObjectManagerFactory>> omfRefs =
                context.getReferences(ObjectManagerFactory.class, filter(property, type));
        for (Reference<ObjectManagerFactory> ref : omfRefs) {
            writeObjectManagerFactoryReference(p, ref, verbose);
        }
    }

    private static <T> Filter filter(CoreProperty<T> property, T value) {
        return value != null ? property.filter(value) : null;
    }

    private static void writeObjectManagerFactoryReference(Printer p, Reference<ObjectManagerFactory> ref,
                                                           boolean verbose) {
        try {
            writeObjectManagerFactory(p, ref.getService(), verbose);
        } finally {
            ref.unget();
        }
    }

    private static String writeObjectManagerReference(Printer p, Reference<ObjectManager> ref,
                                                      boolean verbose, Set<String> moduleNames) {
        try {
            return writeObjectManager(p, ref.getService(), verbose, moduleNames);
        } finally {
            ref.unget();
        }
    }

    private static void writeObjectManagerFactory(Printer p, ObjectManagerFactory fac, boolean verbose) {
        if (fac != null) {
            p.ind();
            p.p(fac.getType()).p(" [").p(fac.getContextName()).p("]");
            if (verbose) {
                p.p(" (").p(fac.getModuleClass().getName()).p(")");
            }
            p.cr();
            if (verbose) {
                p.ind();
                p.p("Hash:").p(System.identityHashCode(fac)).cr();
                p.p("Launched:").cr();
                p.ind();
                if (verbose && fac.getLaunchCount() > 0) {
                    for (ModuleSpecification spec : fac) {
                        p.p(spec.getName()).cr();
                    }
                } else {
                    p.p("<none>").cr();
                }
                p.indOut(2);
            }
            p.outd();
        }
    }

    private static List<String> writeObjectManagerReferences(Printer p, Context context,
                                                             CoreProperty<String> property, String value,
                                                             boolean verbose,
                                                             Set<String> moduleNames) {
        Collection<Reference<ObjectManager>> omRefs =
                context.getReferences(ObjectManager.class, filter(property, value));
        return writeObjectManagerReferences(p, verbose, omRefs, moduleNames);
    }

    private static List<String> writeObjectManagerReferences(Printer p,
                                                             boolean verbose,
                                                             Collection<Reference<ObjectManager>> omRefs,
                                                             Set<String> moduleNames) {
        List<String> types = Generic.list();
        for (Reference<ObjectManager> ref : omRefs) {
            String type = writeObjectManagerReference(p, ref, verbose, moduleNames);
            if (type != null) {
                types.add(type);
            }
        }
        return types;
    }

    private static String writeObjectManager(Printer p, ObjectManager mgr, boolean verbose, Set<String> moduleNames) {
        if (mgr != null) {
            try {
                p.ind().p(mgr.getManagedState()).p
                        (" : ").p(mgr.getName()).p
                        (":").p(mgr.getType());
                if (verbose) {
                    p.p(" (").p(mgr.getManagedObject()).p(")");
                }
                p.cr();
                if (verbose) {
                    p.ind().p("Hash:").p(System.identityHashCode(mgr)).cr();
                    writeConfiguration(p, mgr.getConfigureSummaries());
                    writeFeatures("Exposed:", p, mgr.getExposedServices());
                    writeFeatures("Injected:", p, mgr.getInjectedServices());
                    p.outd();
                }
                p.outd();
                return mgr.getType();
            } finally {
                moduleNames.remove(mgr.getName() + ":" + mgr.getType());
            }
        }
        return null;
    }

    private static void writeConfiguration(Printer p, Collection<ConfigureSummary> configureSummaries) {
        if (configureSummaries == null || configureSummaries.isEmpty()) {
            return;
        }
        p.ind();
        p.p("Configuration:").cr();
        for (ConfigureSummary summary : configureSummaries) {
            p.ind();
            p.p(summary.getName()).p("=").p(summary.getValue()).p(" (").p(summary.getType()).p(")").cr();
            p.outd();
        }
        p.outd();
    }

    private static void writeFeatures(String header, Printer p,
                                      Collection<? extends ManagedFeatureSummary> featureSummaries) {
        if (!featureSummaries.isEmpty()) {
            p.ind();
            p.p(header).cr();
            for (ManagedFeatureSummary summary : featureSummaries) {
                p.ind();
                p.p(summary.getName()).p(":").p(summary.getFeatureClass());
                if (summary instanceof InjectedServiceSummary) {
                    InjectedServiceSummary inj = (InjectedServiceSummary) summary;
                    p.p("/").p(inj.getInjectionType());
                    Filter filter = inj.getFilter();
                    if (filter != null && !filter.isNull()) {
                        p.cr().p(" filter: ").p(filter);
                    }
                }
                p.cr();
                p.ind();
                if (summary.isActive()) {
                    for (InstanceSummary instanceSummary : summary) {
                        writeInstanceSummary(p, instanceSummary);
                    }
                } else {
                    p.p("<no matches>").cr();
                }
                p.outd();
            }
        }
    }

    private static Printer writeInstanceSummary(Printer p, InstanceSummary summary) {
        ServiceProperties<?> properties = summary.getServiceProperties();
        Long serviceId = properties.getServiceId();
        p.p("[");
        if (serviceId != null) {
            p.p(serviceId);
            String pid = properties.getServicePid();
            if (pid != null) {
                p.p("/").p(pid);
            }
        } else {
            p.p("n/a");
        }
        p.p("] ");
        p.p(summary.getInstanceToString()).cr();
        PropertySet ps = properties.getPropertySet();
        int size = ps.size();
        String[] objectClasses = CoreProperty.OBJECTCLASSES.lookupIn(ps);
        if (objectClasses == null || objectClasses.length == 0) {
            p.p(properties.getMainClassName()).cr();
        } else if (objectClasses.length == 1) {
            size--;
            p.ind().p("objectClass=").p(objectClasses[0]).outd().cr();
        } else {
            size--;
            p.ind().p("objectClasses=").p(Arrays.toString(objectClasses)).outd().cr();
        }
        if (CoreProperty.SERVICE_ID.isSetIn(ps)) {
            size--;
        }
        if (CoreProperty.SERVICE_PID.isSetIn(ps)) {
            size--;
        }
        if (size > 0) {
            for (String property : ps) {
                if (!(property.equals(CoreProperty.OBJECTCLASSES_NAME) ||
                        property.equals(CoreProperty.SERVICE_ID_NAME) ||
                        property.equals(CoreProperty.SERVICE_PID_NAME))) {
                    p.ind().p(property).p("=").p(ps.get(property)).outd().cr();
                }
            }
        }
        return p;
    }
}