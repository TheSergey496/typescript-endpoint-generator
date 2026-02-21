package org.thesergey496.processors.impl;

import org.thesergey496.TypescriptEndpointProcessor;
import org.thesergey496.processors.ImportProcessor;
import org.thesergey496.processors.TypeProcessor;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Set;

public class CollectionTypeProcessor implements TypeProcessor {
    @Override
    public String getDefinitionName() {
        return "";
    }

    @Override
    public Set<ImportProcessor> getDefinitionImports() {
        return Set.of();
    }

    @Override
    public String toDefinition() {
        return "";
    }

    @Override
    public Set<ImportProcessor> getUsageImports() {
        return Set.of();
    }

    @Override
    public String toUsage(TypeMirror typeMirror, Set<ImportProcessor> outUsageImports) {
        final String defaultUsage = TypeProcessor.super.toUsage(typeMirror, outUsageImports);

        if (typeMirror.getKind() == TypeKind.ARRAY) {
            final TypeMirror componentType = ((ArrayType) typeMirror).getComponentType();
            return TypescriptEndpointProcessor.typeRegistry.resolve(componentType).toUsage(componentType, outUsageImports) + "[]";
        }

        if (typeMirror instanceof DeclaredType declaredType) {
            if (declaredType.getTypeArguments().isEmpty()) {
                return "any[]";
            }
            final TypeMirror type = declaredType.getTypeArguments().getFirst();
            return TypescriptEndpointProcessor.typeRegistry.resolve(type).toUsage(type, outUsageImports) + "[]";
        }
        return defaultUsage;
    }
}
