package vanadis.annopro;

public interface AnnotationMapper {

    Class<?> getClientCodeType(String processingType);

    Class<?> getClientCodeType(Class<?> processingType);

    String getClientCodeAttribute(String processingType, String processingAttribute);

    String getClientCodeAttribute(Class<?> processingType, String processingAttribute);
}
