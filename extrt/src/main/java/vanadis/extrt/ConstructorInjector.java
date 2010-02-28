package vanadis.extrt;

import vanadis.core.collections.Generic;
import vanadis.core.collections.Pair;
import vanadis.ext.Inject;
import vanadis.osgi.Reference;

import java.lang.reflect.Constructor;
import java.util.List;

public class ConstructorInjector<T> extends AccessibleInjector<T> {

    private Object[] parameters;

    private int parameterIndex;

    private List<Pair<T, Reference<T>>> matches = Generic.list();

    public ConstructorInjector(FeatureAnchor<T> featureAnchor,
                               Constructor injectPoint,
                               Object[] parameters, int parameterIndex,
                               Inject inject,
                               InjectionListener injectionListener) {
        super(featureAnchor,
              inject,
              false,
              injectPoint.getParameterTypes()[0].equals(Reference.class),
              injectionListener);
        this.parameters = parameters;
        this.parameterIndex = parameterIndex;
    }

    @Override
    protected void performInject(Reference<T> reference, T service) {
        matches.add(Pair.of(service, reference));
        if (parameters[parameterIndex] == null) {
            parameters[parameterIndex] = service;
        }
    }

    @Override
    protected void performUninject(Reference<T> reference, T service) {
        matches.remove(Pair.of(service, reference));
        if (parameters[parameterIndex] == service && !matches.isEmpty()) {
            parameters[parameterIndex] = matches.iterator().next();
        }
    }
}
