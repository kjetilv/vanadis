package vanadis.extrt;

import vanadis.annopro.AnnotationDatum;
import vanadis.concurrent.OperationQueuer;
import vanadis.core.collections.Generic;
import vanadis.core.lang.ToString;
import vanadis.ext.Inject;
import vanadis.objectmanagers.ObjectManager;
import vanadis.osgi.Context;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

class ConstructorGatherer implements InjectionListener {

    private final DependencyTracker<ConstructorInjector<?>> tracker;

    private final List<ConstructorInjector<?>> injectors = Generic.list();

    private final ConstructionListener constructionListener;

    private Constructor constructor;

    private final Object[] parameters;

    private final List<Object> currentParameters = Generic.copyOnWriteArrayList();

    ConstructorGatherer(ObjectManager objectManager,
                         ConstructionListener constructionListener,
                         Constructor constructor, int constructorNo,
                         ClassLoader classLoader, List<AnnotationDatum<Integer>> injectData,
                         Context context,
                         OperationQueuer operationQueuer) {
        this.constructionListener = constructionListener;
        this.constructor = constructor;
        tracker = new DependencyTracker<ConstructorInjector<?>>();
        Class[] parameterTypes = constructor.getParameterTypes();
        int parameterCount = parameterTypes.length;
        if (parameterCount != injectData.size()) {
            throw new IllegalArgumentException("Expected " + parameterCount + " injectors, got " +
                    injectData.size() + ": " + injectData);
        }
        this.parameters = new Object[parameterCount];
        for (int i = 0; i < parameterCount; i++) {
            Class<?> parameterType = parameterTypes[i];
            AnnotationDatum<Integer> datum = injectData.get(i);
            ConstructorInjector<?> injector =
                    injector(objectManager, constructor, constructorNo + "-" + i, i, parameterType,
                             classLoader, datum,
                             context, operationQueuer);
            injectors.add(injector);
            tracker.track(injector);
        }
    }

    @Override
    public void wasInjected(ManagedFeature<?, ?> feature) {
        tracker.progress(feature.getFeatureName());
        if (tracker.isRequiredComplete()) {
            constructionListener.constructionTimeAgain(this);
        }
    }

    @Override
    public void wasRetracted(ManagedFeature<?, ?> feature) {
        tracker.setback(feature.getFeatureName());
        if (!tracker.isRequiredComplete()) {
            currentParameters.clear();
            currentParameters.addAll(Arrays.asList(this.parameters));
        }
    }

    public void activate() {
        for (ConstructorInjector<?> injector : injectors) {
            injector.activate();
        }
    }

    private <T> ConstructorInjector<T> injector(ObjectManager objectManager, Constructor constructor,
                                                String name, int parameterIndex, Class<T> parameterType,
                                                ClassLoader classLoader, AnnotationDatum<Integer> datum,
                                                Context context,
                                                OperationQueuer operationQueuer) {
        FeatureAnchor<T> featureAnchor =
                FeatureAnchor.create(objectManager, name, parameterType, context, operationQueuer);
        Inject inject = datum.createProxy(classLoader, Inject.class);
        return new ConstructorInjector<T>(featureAnchor, constructor, parameters, parameterIndex, inject, this);
    }

    public Object create() {
        try {
            return constructor.newInstance(parameters);
        } catch (Exception e) {
            throw new IllegalStateException(this + " unable to create instance");
        }
    }

    @Override
    public String toString() {
        return ToString.of(this, constructor, "parameters", parameters);
    }
}
