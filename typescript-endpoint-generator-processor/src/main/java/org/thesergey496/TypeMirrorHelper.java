package org.thesergey496;

import org.thesergey496.annotations.TypeMapping;

import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class TypeMirrorHelper {
    public static TypeMirror getTypeMirrorFromTypeMapping(TypeMapping mapping, Elements elementUtils, Types typeUtils) {
        TypeMirror result;
        try {
            final Class<?> source = mapping.source();
            if (source.isPrimitive()) {
                if (mapping.source() == boolean.class) {
                    result = typeUtils.getPrimitiveType(TypeKind.BOOLEAN);
                } else {
                    result = typeUtils.getPrimitiveType(TypeKind.INT);
                }
            } else {
                result = elementUtils.getTypeElement(source.getCanonicalName()).asType();
            }
        } catch (MirroredTypeException mte) {
            result = mte.getTypeMirror();
        }
        return result;
    }
}
