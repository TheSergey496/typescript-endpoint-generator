package org.thesergey496.processors.impl;

import lombok.Getter;
import org.thesergey496.TypescriptEndpointProcessor;
import org.thesergey496.processors.FieldProcessor;
import org.thesergey496.processors.ImportProcessor;
import org.thesergey496.processors.TypeProcessor;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class FieldProcessorImpl implements FieldProcessor {
    @Getter
    protected final String definitionName;
    @Getter
    protected final Set<ImportProcessor> definitionImports = new HashSet<>();
    @Getter
    protected final boolean optional;

    protected final String type;

    public FieldProcessorImpl(VariableElement variableElement) {
        this.definitionName = variableElement.getSimpleName().toString();

        TypeMirror typeMirror = variableElement.asType();
        if (typeMirror instanceof DeclaredType declaredType) {
            this.optional = isOptionalType(declaredType);
            if (this.optional) {
                typeMirror = declaredType.getTypeArguments().isEmpty() ? null : declaredType.getTypeArguments().getFirst();
            }
        } else {
            this.optional = false;
        }

        final TypeProcessor typeProcessor = typeMirror != null
                ? TypescriptEndpointProcessor.typeRegistry.resolve(typeMirror)
                : PrimitiveTypeProcessor.ANY;
        this.type = typeProcessor.toUsage(typeMirror, this.definitionImports);
    }

    @Override
    public String toDefinition() {
        return "%s%s: %s;".formatted(this.getDefinitionName(), this.isOptional() ? "?" : "", this.type);
    }

    protected boolean isOptionalType(DeclaredType declaredType) {
        return ((TypeElement) declaredType.asElement()).getQualifiedName().contentEquals(Optional.class.getCanonicalName());
    }
}
