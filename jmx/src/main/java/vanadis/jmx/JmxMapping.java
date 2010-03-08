package vanadis.jmx;

import vanadis.annopro.AnnotationMapper;
import vanadis.annopro.AnnotationsDigest;
import vanadis.annopro.MappingAnnotationMapper;
import vanadis.core.collections.Generic;

import java.util.Map;

public class JmxMapping {

    static final AnnotationMapper DEFAULT =
            new MappingAnnotationMapper(Managed.class, Operation.class, Attr.class, Param.class);

    static final JmxMapping DEFAULT_JMX_MAPPING =
            new JmxMapping(Managed.class, Operation.class, Attr.class, Param.class);

    private Class<?> classType;

    private Class<?> operationType;

    private Class<?> attributeType;

    private Class<?> parameterType;

    private final MappingAnnotationMapper mappingAnnotationMapper;

    public JmxMapping(Class<?> classType,
                      Class<?> operationType,
                      Class<?> attributeType,
                      Class<?> parameterType) {
        this(classType, null, null,
             operationType, null, null, null,
             attributeType, null, null, null, null,
             parameterType, null, null);
    }

    public JmxMapping(Class<?> classType, String objectName, String desc,
                      Class<?> operationType, String opAsString, String opDesc, String impact,
                      Class<?> attributeType, String atAsString, String atDesc, String readable, String writable,
                      Class<?> parameterType, String parName, String parDesc) {
        this.classType = classType;
        this.operationType = operationType;
        this.attributeType = attributeType;
        this.parameterType = parameterType;
        this.mappingAnnotationMapper = new MappingAnnotationMapper
                (Generic.<Class<?>, Class<?>>map
                        (Managed.class, this.classType,
                         Operation.class, this.operationType,
                         Attr.class, this.attributeType,
                         Param.class, this.parameterType),
                 Generic.<Class<?>, Map<String,String>>map
                         (Managed.class, Generic.map("objectName", objectName,
                                                     "desc", desc),
                          Operation.class, Generic.map("asString", opAsString,
                                                       "desc", opDesc,
                                                       "impact", impact),
                          Attr.class, Generic.map("asString", atAsString,
                                                  "desc", atDesc,
                                                  "readable", readable,
                                                  "writable", writable),
                          Param.class, Generic.map("name", parName,
                                                   "desc", parDesc)));
    }

    AnnotationMapper getAnnotationMapper() {
        return mappingAnnotationMapper;
    }

    boolean isManaged(AnnotationsDigest digest) {
        return digest.hasClassData(classType) ||
                digest.hasMethodData(attributeType, operationType) ||
                digest.hasFieldData(attributeType) ||
                digest.hasParamData(parameterType);
    }

    public AnnotationsDigest managed(AnnotationsDigest digest) {
        return isManaged(digest) ? digest : null;
    }
}
