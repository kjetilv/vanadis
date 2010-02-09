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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vanadis.annopro.AnnotationDatum;
import vanadis.annopro.AnnotationsDigest;
import vanadis.blueprints.ModuleSpecification;
import vanadis.concurrent.OperationQueuer;
import vanadis.core.collections.Generic;
import vanadis.core.io.Location;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;
import vanadis.core.properties.PropertySet;
import vanadis.core.properties.PropertySets;
import vanadis.ext.*;
import vanadis.jmx.ManagedDynamicMBean;
import vanadis.objectmanagers.*;
import vanadis.osgi.Context;
import vanadis.osgi.Reference;
import vanadis.osgi.Registration;
import vanadis.osgi.ServiceProperties;

import javax.management.DynamicMBean;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static vanadis.objectmanagers.ManagedState.*;

final class ObjectManagerImpl implements ObjectManager, InjectionListener {

    static <T> ObjectManager create(Context context, ModuleSpecification moduleSpecification,
                                    Class<T> managedClass, T managed,
                                    ObjectManagerObserver observer, OperationQueuer dispatch) {
        return new ObjectManagerImpl(context, moduleSpecification,
                                     managedClass, managed,
                                     observer, dispatch);
    }

    private final ObjectManagerState state;

    private final ModuleSpecification moduleSpecification;

    private final Class<?> managedClass;

    private final AtomicReference<Object> managed = new AtomicReference<Object>();

    private final ClassLoader managedClassLoader;

    private final Context context;

    private final AnnotationsDigest annotationsDigest;

    /**
     * Tracks injectors such as {@link vanadis.extrt.MethodInjector}
     * and {@link vanadis.extrt.TrackingInjector}.
     */
    private final DependencyTracker<Injector<?>> injectorDependencyTracker = new DependencyTracker<Injector<?>>();

    /**
     * Tracks {@link MethodExposer} instances.
     */
    private final DependencyTracker<Exposer<?>> exposerDependencyTracker = new DependencyTracker<Exposer<?>>();

    private final Set<Configurer> configurers;

    private final ObjectManagerFailureTracker failureTracker = new ObjectManagerFailureTracker();

    private final Registration<ObjectManager> registration;

    private final List<JmxRegistration<?>> jmxRegs = Generic.list();

    private final OperationQueuer queuer;

    private final InjectionListener asyncInjectionListener;

    private final ModuleSystemCallback asynchModuleSystemCallback;

    private ObjectManagerImpl(Context context, ModuleSpecification moduleSpecification,
                              Class<?> managedClass, Object managed,
                              ObjectManagerObserver observer,
                              OperationQueuer queuer) {
        this.context = Not.nil(context, "context");
        validate(this.context, managedClass, managed);
        if (managed != null) {
            this.managed.set(managed);
        }
        this.managedClass = managedClass == null ? this.managed.get().getClass() : managedClass;
        this.managedClassLoader = this.managedClass.getClassLoader();
        this.state = new ObjectManagerState(this, observer, failureTracker);
        this.annotationsDigest = ValidAnnotations.read(this.managed.get());
        this.moduleSpecification = moduleSpecification == null
                ? ModuleSpecification.createDefault(this.managed.get())
                : moduleSpecification;
        this.queuer = queuer;

        configurers = setupConfigurers(this.context.getLocation());
        setupManagedFeatures();

        this.asyncInjectionListener = asynchInjectionListener();
        this.asynchModuleSystemCallback = asynchCallback();

        this.registration = this.context.register(this, objectManagerServiceProperties());
        this.jmxRegs.add(objectManagerBean());

        addCustomJmx();

        try {
            boot();
        } catch (Throwable e) {
            log.error(ObjectManagerImpl.this + " failed initialization", e);
            state.transition(Transition.FAIL);
        }
    }

    @Override
    public Collection<ConfigureSummary> getConfigureSummaries() {
        Collection<ConfigureSummary> summaries = Generic.set();
        for (Configurer configurer : configurers) {
            summaries.add(new ConfigureSummaryImpl(configurer));
        }
        return summaries;
    }

    @Override
    public Collection<InjectedServiceSummary> getInjectedServices() {
        List<InjectedServiceSummary> serviceSummaries = Generic.list();
        for (Injector<?> injector : injectorDependencyTracker) {
            serviceSummaries.add(new InjectedServiceSummaryImpl(injector));
        }
        return serviceSummaries;
    }

    @Override
    public Collection<ExposedServiceSummary> getExposedServices() {
        List<ExposedServiceSummary> serviceSummaries = Generic.list();
        for (Exposer<?> exposer : exposerDependencyTracker) {
            serviceSummaries.add(new ExposedServiceSummaryImpl(exposer));
        }
        return serviceSummaries;
    }

    @Override
    public String getName() {
        return moduleSpecification.getName();
    }

    @Override
    public String getType() {
        return moduleSpecification.getType();
    }

    @Override
    public ModuleSpecification getModuleSpecification() {
        return moduleSpecification;
    }

    @Override
    public Object getManagedObject() {
        return managed.get();
    }

    @Override
    public Class<?> getManagedClass() {
        return managedClass;
    }

    @Override
    public ClassLoader getManagedObjectClassLoader() {
        return managedClassLoader;
    }

    @Override
    public boolean isLaunchable() {
        return !hasFailed() && state.is(SERVICES_EXPOSED);
    }

    @Override
    public ObjectManagerFailures getFailures() {
        return failureTracker;
    }

    @Override
    public boolean hasFailed() {
        return !failureTracker.hasFailed();
    }

    @Override
    public void launch() {
        state.transition(Transition.ACTIVATE);
    }

    @Override
    public ManagedState getManagedState() {
        return state.getManagedState();
    }

    @Override
    public void shutdown() {
        if (getManagedState() == DISPOSED) {
            log.info(this + " was asked to close again");
            return;
        }
        notifyManagedFeatures();
        for (JmxRegistration<?> jmxReg : jmxRegs) {
            jmxReg.unregister();
        }
        deactivate(injectorDependencyTracker);
        deactivate(exposerDependencyTracker);
        state.transition(Transition.DISPOSE);
        registration.unregister();
    }

    @Override
    public void wasInjected(ManagedFeature<?,?> injector) {
        asyncInjectionListener.wasInjected(injector);
    }

    @Override
    public void wasRetracted(ManagedFeature<?, ?> injector) {
        asyncInjectionListener.wasRetracted(injector);
    }

    private ModuleSystemCallback asynchCallback() {
        return asynch(new Callback(state, failureTracker), ModuleSystemCallback.class);
    }

    private InjectionListener asynchInjectionListener() {
        return asynch(new ObjectManagerInjectionListener(injectorDependencyTracker, this.moduleSpecification),
                      InjectionListener.class);
    }

    private <T> T asynch(T service, Class<T> type) {
        return queuer == null ? service : queuer.createAsynch(service, type);
    }

    private Set<Configurer> setupConfigurers(Location location) {
        Set<Configurer> configurers = Generic.set();
        PropertySet propertySet = moduleSpecification.getPropertySet();
        for (Class<? extends Annotation> type : Arrays.asList(Configure.class, Configuration.class)) {
            for (AnnotationDatum<?> datum : annotationsDigest.getAccessibleData(type)) {
                configurers.add(new Configurer(managed.get(), location, datum, propertySet));
            }
        }
        return configurers;
    }

    private void addCustomJmx() {
        DynamicMBean mbean = ManagedDynamicMBean.create(managed.get());
        if (mbean != null) {
            jmxRegs.add(JmxRegistration.create(context, mbean,
                                               managedClass.getName(),
                                               propertySet(getType())));
        }
    }

    private JmxRegistration<ObjectManagerMBean> objectManagerBean() {
        return JmxRegistration.create(this.context, ObjectManagerMBean.class, new ObjectManagerMBeanImpl(this),
                                      managedClass.getName(),
                                      propertySet(getType() + "-manager"));
    }

    private PropertySet propertySet(String type) {
        return PropertySets.create("type", type,
                                   "name", getName());
    }

    private void setupManagedFeatures() {
        Set<String> names = Generic.set();
        setupInjectors(names);
        setupTrackers(names);
        setupExposers(names);
        log.info(this + " setup managed features: " + names);
    }

    private void boot() {
        setContext();
        setCallback();
        configure();
        state.transition(Transition.CONFIGURE);
        activateInjectors();
        state.transition(Transition.INITIALIZE);
        resolveCurrentState();
    }

    private ServiceProperties<ObjectManager> objectManagerServiceProperties() {
        PropertySet propertySet = PropertySets.create();
        CoreProperty.OBJECTMANAGER_NAME.set(propertySet, getName());
        CoreProperty.OBJECTMANAGER_TYPE.set(propertySet, getType());
        return ServiceProperties.create(ObjectManager.class, propertySet);
    }

    private void setCallback() {
        if (managed.get() instanceof ModuleSystemAware) {
            ((ModuleSystemAware) managed.get()).setManagedObjectCallback(asynchModuleSystemCallback);
        }
    }

    private void activateInjectors() {
        for (Injector<?> injector : injectorDependencyTracker) {
            injector.activate();
        }
    }

    private void configure() {
        for (Configurer configurer : configurers) {
            configurer.set(context.getPropertySet());
        }
    }

    private void setContext() {
        if (managed.get() instanceof ContextAware) {
            ((ContextAware) managed.get()).setContext(context);
        }
    }

    private void setupInjectors(Set<String> names) {
        for (AnnotationDatum<Method> datum : annotationsDigest.getMethodData(Inject.class)) {
            setupMethodInjector(names, datum);
        }
        for (AnnotationDatum<Field> datum : annotationsDigest.getFieldData(Inject.class)) {
            setupFieldInjector(names, datum);
        }
    }

    private void setupMethodInjector(Set<String> names, AnnotationDatum<Method> datum) {
        Method injectMethod = datum.getElement();
        Class<?>[] parameterTypes = injectMethod.getParameterTypes();
        if (parameterTypes.length > 0) {
            Inject injectDirective = proxy(datum, Inject.class);
            Method retractMethod = retractMethod(injectMethod, injectDirective);
            Class<?> serviceInterface = resolveType(injectMethod, parameterTypes[0], injectDirective.injectType());
            String featureName = validateName(Names.nameOfMethod(datum), names);
            setupMethodInjector(injectMethod, retractMethod, serviceInterface, injectDirective, featureName);
        } else {
            // It's a scala var field, the real setter gets handled in a different
            // call to this method.
        }
    }

    private String validateName(String featureName, Set<String> names) {
        if (names.contains(featureName)) {
            throw new IllegalStateException(this + " found duplicate feature name: " + featureName);
        }
        names.add(featureName);
        return featureName;
    }

    private void setupFieldInjector(Set<String> names, AnnotationDatum<Field> datum) {
        Field field = datum.getElement();
        if (injectorDependencyTracker.isTracking(scalaSetterFeature(field))) {
            // Scala injector method is already being tracked as a method injector
        } else {
            Inject injectDirective = proxy(datum, Inject.class);
            Class<?> serviceInterface = resolveType(field, field.getType(), injectDirective.injectType());
            String featureName = validateName(Names.nameOfField(datum), names);
            setupFieldInjector(field, serviceInterface, injectDirective, featureName);
        }
    }

    private void setupTrackers(Set<String> names) {
        for (AnnotationDatum<Method> datum : annotationsDigest.getMethodData(Track.class)) {
            String name = validateName(Names.nameOfMethod(datum), names);
            setupTracker(datum, datum.getElement(), name);
        }
        for (AnnotationDatum<Field> datum : annotationsDigest.getFieldData(Track.class)) {
            String name = validateName(Names.nameOfField(datum), names);
            setupTracker(datum, datum.getElement(), name);
        }
    }

    private void setupTracker(AnnotationDatum<?> datum, AccessibleObject trackObject, String featureName) {
        Track trackDirective = proxy(datum, Track.class);
        setupTracker(trackObject, trackDirective.trackedType(), trackDirective, featureName);
    }

    private void setupExposers(Set<String> names) {
        AnnotationDatum<Class<?>> classDatum = annotationsDigest.getClassDatum(Expose.class);
        if (classDatum != null) {
            setupClassExposer(classDatum, names);
        }
        for (AnnotationDatum<Method> methodDatum : annotationsDigest.getMethodData(Expose.class)) {
            setupMethodExposer(methodDatum, names);
        }
        for (AnnotationDatum<Field> methodDatum : annotationsDigest.getFieldData(Expose.class)) {
            setupFieldExposer(methodDatum, names);
        }
    }

    private void setupClassExposer(AnnotationDatum<Class<?>> classDatum, Set<String> names) {
        Expose expose = proxy(classDatum, Expose.class);
        String featureName = validateName(Names.nameOfType(classDatum, getName()), names);
        Class<?> serviceInterface = resolveExposedType(expose);
        setupMethodExposer(null, serviceInterface, expose, featureName);
    }

    private void setupFieldExposer(AnnotationDatum<Field> fieldDatum, Set<String> names) {
        Expose expose = proxy(fieldDatum, Expose.class);
        Field field = fieldDatum.getElement();
        String featureName = validateName(Names.nameOfField(fieldDatum), names);
        Class<?> serviceInterface = resolveExposedType(field.getType(), expose);
        setupFieldExposer(field, serviceInterface, expose, featureName);
    }

    private void setupMethodExposer(AnnotationDatum<Method> methodDatum, Set<String> names) {
        Expose expose = proxy(methodDatum, Expose.class);
        Method method = methodDatum.getElement();
        String featureName = validateName(Names.nameOfMethod(methodDatum), names);
        Class<?> serviceInterface = resolveExposedType(method.getReturnType(), expose);
        setupMethodExposer(method, serviceInterface, expose, featureName);
    }

    private <T> TrackingInjector<T> setupTracker(AccessibleObject trackObject, Class<T> serviceInterface,
                                                 Track track, String featureName) {
        return track(new TrackingInjector<T>
                (anchor(serviceInterface, featureName), trackObject, track, this));
    }

    private <T> MethodInjector<T> setupMethodInjector(Method injectMethod, Method retractMethod,
                                                      Class<T> serviceInterface,
                                                      Inject inject, String featureName) {
        return track(new MethodInjector<T>
                (anchor(serviceInterface, featureName), injectMethod, retractMethod, inject, this));
    }

    private <T> FieldInjector<T> setupFieldInjector(Field injectField, Class<T> serviceInterface,
                                                    Inject inject, String featureName) {
        return track(new FieldInjector<T>(anchor(serviceInterface, featureName), injectField, inject, this));
    }

    private <T> Exposer<T> setupMethodExposer(Method method, Class<T> serviceInterface,
                                              Expose exposeDirective, String featureName) {
        return track(new MethodExposer<T>(anchor(serviceInterface, featureName), method, exposeDirective));
    }

    private <T> Exposer<T> setupFieldExposer(Field field, Class<T> serviceInterface,
                                             Expose exposeDirective, String featureName) {
        return track(new FieldExposer<T>(anchor(serviceInterface, featureName), field, exposeDirective));
    }

    private <T> FeatureAnchor<T> anchor(Class<T> serviceInterface, String featureName) {
        return FeatureAnchor.create(this, featureName, serviceInterface, context, queuer);
    }

    private Method retractMethod(Method injectMethod, Inject injectDirective) {
        List<AnnotationDatum<Method>> retractMetods = annotationsDigest.getMethodData(Retract.class);
        if (retractMetods == null) {
            return injectMethod;
        }
        String attributeName = injectDirective.attributeName().trim();
        if (attributeName.trim().length() > 0) {
            for (AnnotationDatum<Method> datum : retractMetods) {
                Retract retractionPoint = proxy(datum, Retract.class);
                if (retractionPoint.attributeName().trim().equals(attributeName)) {
                    return datum.getElement();
                }
            }
        }
        for (AnnotationDatum<Method> datum : retractMetods) {
            Method method = datum.getElement();
            if (nameTypeMatch(injectMethod, method)) {
                return method;
            }
        }
        return injectMethod;
    }

    private <T extends Annotation> T proxy(AnnotationDatum<?> datum, Class<T> type) {
        return datum.createProxy(getClass().getClassLoader(), type);
    }

    private <T, I extends Injector<T>> I track(I injector) {
        injectorDependencyTracker.track(injector);
        return injector;
    }

    private <T> Exposer<T> track(Exposer<T> exposer) {
        exposerDependencyTracker.track(exposer);
        return exposer;
    }

    private void expose(Exposer<?> exposer) {
        exposer.activate();
        exposerDependencyTracker.progress(exposer.getFeatureName());
    }

    private void handleRetractionEvent() {
        if (state.is(DEPENDENCIES_RESOLVED, SERVICES_EXPOSED, ACTIVE)) {
            if (!injectorDependencyTracker.isRequiredComplete()) {
                state.transition(Transition.BECOME_UNRESOLVED);
            }
            for (Exposer<?> exposer : exposerDependencyTracker.complete()) {
                if (!canActivate(exposer) && !exposer.isPersistent()) {
                    deactivate(exposer, exposerDependencyTracker);
                }
            }
        }
    }

    private void resolveCurrentState() {
        if (state.is(RESOLVING_DEPENDENCIES)) {
            if (injectionIncomplete()) {
                return;
            }
            checkResolvedStatus();
        }
        makeExposures();
        checkExposureStatus();
    }

    private boolean injectionIncomplete() {
        Iterable<Injector<?>> injectors = injectorDependencyTracker.requiredIncomplete();
        for (Injector<?> injector : injectors) {
            if (injector.isRequired()) {
                return true;
            }
        }
        return false;
    }

    private void checkResolvedStatus() {
        if (injectorDependencyTracker.isRequiredComplete()) {
            state.transition(Transition.BECOME_RESOLVED);
        }
    }

    private void makeExposures() {
        if (state.afterOrIn(DEPENDENCIES_RESOLVED)) {
            for (Exposer<?> exposer : exposerDependencyTracker.incomplete()) {
                if (canActivate(exposer)) {
                    expose(exposer);
                }
            }
        }
    }

    private void checkExposureStatus() {
        if (state.is(DEPENDENCIES_RESOLVED)) {
            if (exposerDependencyTracker.isRequiredComplete()) {
                state.transition(Transition.COMPLETE_EXPOSURE);
            }
        }
    }

    private boolean canActivate(Exposer<?> exposer) {
        return exposer.isReadyToGo(injectorDependencyTracker.completeNames());
    }

    private Class<?> resolveExposedType(Expose expose) {
        Class<?> annotated = expose.exposedType();
        return annotated == Derived.class ? managedClass : annotated;
    }

    private void notifyManagedFeatures() {
        for (ManagedFeature<?, ?> feature : exposerDependencyTracker) {
            feature.teardownMode();
        }
        for (ManagedFeature<?, ?> feature : injectorDependencyTracker) {
            feature.teardownMode();
        }
    }

    private static final Logger log = LoggerFactory.getLogger(ObjectManagerImpl.class);

    private static final String SCALASET_SUFFIX = "_$eq";

    private static String scalaSetterFeature(Field field) {
        return field.getName() + SCALASET_SUFFIX;
    }

    private static void validate(Context context, Class<?> managedClass, Object managed) {
        if (managedClass == null && managed == null) {
            throw new IllegalArgumentException
                    ("Neither managed class nor managed instance passed to constructor, context: " + context);
        }
        if (managedClass != null && managed != null && !managedClass.isInstance(managed)) {
            throw new IllegalArgumentException
                    ("Managed instance " + managed +
                            " is not an instance of managed " + managedClass +
                            ", context: " + context);
        }
    }

    private static Class<?> resolveType(Object injectPoint, Class<?> serviceInterface, Class<?> annotated) {
        boolean unannotated = annotated == Derived.class;
        if (serviceInterface.equals(Reference.class)) {
            if (unannotated) {
                throw new IllegalArgumentException
                        (injectPoint + " has annotation with argument of " +
                                Reference.class + ", annotation should specify service interface");
            }
            return annotated;
        }
        return unannotated ? serviceInterface : annotated;
    }

    private static Class<?> resolveExposedType(Class<?> accessibleType, Expose expose) {
        Class<?> annotated = expose.exposedType();
        if (annotated == Derived.class) {
            return accessibleType.isArray() ? accessibleType.getComponentType() : accessibleType;
        }
        return annotated;
    }

    private static boolean nameTypeMatch(Method injectMethod, Method retractMethod) {
        String addName = injectMethod.getName();
        String removeName = retractMethod.getName();
        Class<?>[] addParams = injectMethod.getParameterTypes();
        Class<?>[] removeParams = retractMethod.getParameterTypes();
        return addName.startsWith("add") && removeName.startsWith("remove") &&
                addName.substring(3).equals(removeName.substring(6)) &&
                addParams.length >= 1 && removeParams.length == 1 &&
                addParams[0] == removeParams[0];
    }

    private static <M extends ManagedFeature<?, ?>> void deactivate(M managedFeature,
                                                                    DependencyTracker<M> dependencyTracker) {
        try {
            managedFeature.deactivate();
        } finally {
            dependencyTracker.setback(managedFeature.getFeatureName());
        }
    }

    private static <M extends ManagedFeature<?, ?>> void deactivate(DependencyTracker<M> dependencyTracker) {
        for (M managedFeature : dependencyTracker) {
            deactivate(managedFeature, dependencyTracker);
        }
    }

    private class ObjectManagerInjectionListener implements InjectionListener {

        private final DependencyTracker<?> injectorDependencyTracker;

        private final ModuleSpecification moduleSpecification;

        private ObjectManagerInjectionListener(DependencyTracker<?> injectorDependencyTracker,
                                               ModuleSpecification moduleSpecification) {
            this.injectorDependencyTracker = injectorDependencyTracker;
            this.moduleSpecification = moduleSpecification;
        }

        @Override
        public void wasInjected(ManagedFeature<?,?> feature) {
            try {
                if (feature.isComplete()) {
                    injectorDependencyTracker.progress(feature.getFeatureName());
                }
                resolveCurrentState();
            } catch (Exception e) {
                state.updateState(e, "{0} failed to process injection from {1}", this, feature);
            }
        }

        @Override
        public void wasRetracted(ManagedFeature<?, ?> feature) {
            injectorDependencyTracker.setback(feature.getFeatureName());
            handleRetractionEvent();
        }

        @Override
        public String toString() {
            return ToString.of(this, moduleSpecification);
        }
    }

    @Override
    public String toString() {
        return ToString.of(this, managed.get(), "state", state, "context", context);
    }
}
