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

package vanadis.modules.commands;

import vanadis.blueprints.ModuleSpecification;
import vanadis.core.collections.Generic;
import vanadis.core.properties.PropertySet;
import vanadis.ext.*;
import vanadis.osgi.Context;
import vanadis.osgi.Filter;
import vanadis.osgi.Reference;
import vanadis.osgi.ServiceProperties;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

final class ListExecution extends AbstractCommandExecution {

    @Override
    public void exec(String command, String[] args, StringBuilder sb, Context context) {
        boolean verbose = verbose(args);
        String type = type(args);
        CoreProperty<String> property = property(args);
        if (property == CoreProperty.OBJECTMANAGER_TYPE) {
            ln(sb.append("Managed object factories:"));
            writeObjectManagerFactoryReferences(sb, context, property, type, verbose);
        }
        ln(sb.append("Managed objects:"));
        writeObjectManagerReferences(sb, context, property, type, verbose);
    }

    private CoreProperty<String> property(String[] args) {
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

    private static <T> void writeObjectManagerFactoryReferences(StringBuilder sb, Context context,
                                                                CoreProperty<T> property, T type, boolean verbose) {
        Collection<Reference<ObjectManagerFactory>> omfRefs =
                context.getReferences(ObjectManagerFactory.class, filter(property, type));
        for (Reference<ObjectManagerFactory> ref : omfRefs) {
            writeObjectManagerFactoryReference(sb, ref, verbose);
        }
    }

    private static <T> Filter filter(CoreProperty<T> property, T value) {
        return value != null ? property.filter(value) : null;
    }

    private static void writeObjectManagerFactoryReference(StringBuilder sb, Reference<ObjectManagerFactory> ref,
                                                           boolean verbose) {
        try {
            writeObjectManagerFactory(sb, ref.getService(), verbose);
        } finally {
            ref.unget();
        }
    }

    private static String writeObjectManagerReference(StringBuilder sb, Reference<ObjectManager> ref,
                                                      boolean verbose) {
        try {
            return writeObjectManager(sb, ref.getService(), verbose);
        } finally {
            ref.unget();
        }
    }

    private static void writeObjectManagerFactory(StringBuilder sb, ObjectManagerFactory fac,
                                                  boolean verbose) {
        if (fac != null) {
            ind(1, sb).append(fac.getType()).append(" [").append(fac.getContextName()).append("]");
            if (verbose) {
                sb.append(" (").append(fac.getModuleClass().getName()).append(")");
            }
            ln(sb);
            if (verbose) {
                ln(ind(2, sb).append("Hash:").append(System.identityHashCode(fac)));
                ln(ind(2, sb).append("Launched:"));
                if (verbose && fac.getLaunchCount() > 0) {
                    for (ModuleSpecification spec : fac) {
                        ln(ind(3, sb).append(spec.getName()));
                    }
                } else {
                    ln(ind(3, sb).append("<none>"));
                }
            }
        }
    }

    private static <T> List<String> writeObjectManagerReferences(StringBuilder sb, Context context,
                                                                 CoreProperty<T> property, T value, boolean verbose) {
        Collection<Reference<ObjectManager>> omRefs =
                context.getReferences(ObjectManager.class, filter(property, value));
        return writeObjectManagerReferences(sb, verbose, omRefs);
    }

    private static List<String> writeObjectManagerReferences(StringBuilder sb,
                                                             boolean verbose,
                                                             Collection<Reference<ObjectManager>> omRefs) {
        List<String> types = Generic.list();
        for (Reference<ObjectManager> ref : omRefs) {
            String type = writeObjectManagerReference(sb, ref, verbose);
            if (type != null) {
                types.add(type);
            }
        }
        return types;
    }

    private static String writeObjectManager(StringBuilder sb, ObjectManager mgr, boolean verbose) {
        if (mgr != null) {
            ind(1, sb).append(mgr.getManagedState()).append
                    (" : ").append(mgr.getName()).append
                    (":").append(mgr.getType());
            if (verbose) {
                sb.append(" (").append(mgr.getManagedObject()).append(")");
            }
            ln(sb);
            if (verbose) {
                ln(ind(2, sb).append("Hash:").append(System.identityHashCode(mgr)));
                writeConfiguration(sb, mgr.getConfigureSummaries());
                writeFeatures("Exposed:", sb, mgr.getExposedServices());
                writeFeatures("Injected:", sb, mgr.getInjectedServices());
            }
            return mgr.getType();
        }
        return null;
    }

    private static void writeConfiguration(StringBuilder sb, Collection<ConfigureSummary> configureSummaries) {
        if (configureSummaries == null || configureSummaries.isEmpty()) {
            return;
        }
        ln(ind(2, sb).append("Configuration:"));
        for (ConfigureSummary summary : configureSummaries) {
            ln(ind(3, sb).append(summary.getName()).append("=").append(summary.getValue()).append
                    (" (").append(summary.getType()).append(")"));
        }
    }

    private static void writeFeatures(String header, StringBuilder sb,
                                      Collection<? extends ManagedFeatureSummary> featureSummaries) {
        if (!featureSummaries.isEmpty()) {
            ln(ind(2, sb).append(header));
            for (ManagedFeatureSummary summary : featureSummaries) {
                ind(3, sb).append(summary.getName()).append(":").append(summary.getFeatureClass());
                if (summary instanceof InjectedServiceSummary) {
                    InjectedServiceSummary inj = (InjectedServiceSummary) summary;
                    sb.append("/").append(inj.getInjectionType());
                    Filter filter = inj.getFilter();
                    if (filter != null && !filter.isNull()) {
                        ind(3, ln(sb)).append(" filter: ").append(filter);
                    }
                }
                ln(sb);
                if (summary.isActive()) {
                    for (InstanceSummary instanceSummary : summary) {
                        writeInstanceSummary(ind(4, sb), 4, instanceSummary);
                    }
                } else {
                    ln(ind(4, sb).append("<no matches>"));
                }
            }
        }
    }

    private static StringBuilder writeInstanceSummary(StringBuilder sb, int indent, InstanceSummary summary) {
        ServiceProperties<?> properties = summary.getServiceProperties();
        Long serviceId = properties.getServiceId();
        sb.append("[");
        if (serviceId != null) {
            sb.append(serviceId);
            String pid = properties.getServicePid();
            if (pid != null) {
                sb.append("/").append(pid);
            }
        } else {
            sb.append("n/a");
        }
        sb.append("] ");
        ln(sb.append(summary.getInstanceToString()));
        PropertySet ps = properties.getPropertySet();
        int size = ps.size();
        String[] objectClasses = CoreProperty.OBJECTCLASSES.lookupIn(ps);
        if (objectClasses == null || objectClasses.length == 0) {
            ln(sb.append(properties.getMainClassName()));
        } else if (objectClasses.length == 1) {
            size--;
            ln(ind(indent + 1, sb).append("objectClass=").append(objectClasses[0]));
        } else {
            size--;
            ln(ind(indent + 1, sb).append("objectClasses=").append(Arrays.toString(objectClasses)));
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
                    ln(ind(indent + 1, sb).append(property).append("=").append(ps.get(property)));
                }
            }
        }
        return sb;
    }
}