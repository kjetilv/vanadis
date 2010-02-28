package vanadis.jmx;

import vanadis.annopro.AnnotationsDigest;

public class JmxMapping {

    static final JmxMapping DEFAULT = new JmxMapping(Managed.class, Operation.class, Attr.class, Param.class);

    private Class<?> classType;

    private Class<?> operationType;

    private Class<?> fieldType;

    private Class<?> parameterType;

    public JmxMapping(Class<?> classType,
                             Class<?> operationType,
                             Class<?> fieldType,
                             Class<?> parameterType) {
        this.classType = classType;
        this.operationType = operationType;
        this.fieldType = fieldType;
        this.parameterType = parameterType;
    }

    Class<?> getClassType() {
        return classType;
    }

    Class<?> getOperationType() {
        return operationType;
    }

    Class<?> getFieldType() {
        return fieldType;
    }

    Class<?> getParameterType() {
        return parameterType;
    }

    boolean isManaged(AnnotationsDigest digest) {
        return digest.hasClassData(classType) ||
            digest.hasMethodData(fieldType, operationType) ||
            digest.hasFieldData(fieldType) ||
            digest.hasParamData(parameterType);
    }

    public AnnotationsDigest managed(AnnotationsDigest digest) {
        return isManaged(digest) ? digest : null;
    }
}
