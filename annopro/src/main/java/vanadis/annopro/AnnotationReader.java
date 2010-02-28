package vanadis.annopro;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

interface AnnotationReader {

    Map<String, AnnotationDatum<Class<?>>> annotations();

    Map<Method, List<AnnotationDatum<Method>>> readAllMethods();

    Map<Method, List<List<AnnotationDatum<Integer>>>> readAllParameters();

    Map<Field, List<AnnotationDatum<Field>>> readAllFields();
}
