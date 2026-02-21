package org.thesergey496.processors.impl;

import com.google.common.base.CaseFormat;
import org.thesergey496.TypescriptEndpointProcessor;
import org.thesergey496.annotations.TypescriptIgnore;
import org.thesergey496.processors.EntityProcessor;
import org.thesergey496.processors.FieldProcessor;
import org.thesergey496.processors.GeneratedTypeProcessor;
import org.thesergey496.processors.ImportProcessor;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.*;
import java.util.stream.Collectors;

public class DtoTypeProcessor implements GeneratedTypeProcessor {
    protected final String name;
    protected final List<String> generics;

    protected List<? extends FieldProcessor> fields;

    public DtoTypeProcessor(Element element) {
        this.name = element.getSimpleName().toString();
        this.generics = ((TypeElement) element).getTypeParameters().stream()
                .map(TypeParameterElement::asType)
                .map(TypeMirror::toString)
                .toList();
    }

    public void init(Element element) {
        this.fields = ElementFilter.fieldsIn(element.getEnclosedElements()).stream()
                .filter(e -> !e.getModifiers().contains(Modifier.STATIC))
                .filter(e -> e.getAnnotation(TypescriptIgnore.class) == null)
                .map(FieldProcessorImpl::new)
                .collect(Collectors.toList());
    }

    @Override
    public Set<ImportProcessor> getUsageImports() {
        return Collections.singleton(new ImportProcessorImpl(this.getDefinitionName(), CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, this.getDefinitionName())));
    }

    @Override
    public String toUsage(TypeMirror typeMirror, Set<ImportProcessor> outUsageImports) {
        final String defaultUsage = GeneratedTypeProcessor.super.toUsage(typeMirror, outUsageImports);

        if (typeMirror instanceof DeclaredType declaredType) {
            String baseName = this.getDefinitionName();

            List<? extends TypeMirror> typeArgs = declaredType.getTypeArguments();

            if (typeArgs.isEmpty()) {
                return baseName;
            }

            StringBuilder sb = new StringBuilder(baseName);
            sb.append("<");
            for (int i = 0; i < typeArgs.size(); i++) {
                final TypeMirror type = typeArgs.get(i);
                sb.append(TypescriptEndpointProcessor.typeRegistry.resolve(type).toUsage(type, outUsageImports));
                if (i < typeArgs.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append(">");
            return sb.toString();
        }

        return defaultUsage;
    }

    @Override
    public String getDefinitionName() {
        return this.name;
    }

    @Override
    public Set<ImportProcessor> getDefinitionImports() {
        return this.fields.stream()
                .map(EntityProcessor::getDefinitionImports)
                .flatMap(Collection::stream)
                .filter(importProcessor -> !Objects.equals(importProcessor.getEntityName(), this.getDefinitionName()))
                .collect(Collectors.toSet());
    }

    @Override
    public String toDefinition() {
        final String imports = this.getDefinitionImports().stream()
                .map(importProcessor -> importProcessor.convertToTs(null))
                .sorted()
                .collect(Collectors.joining("\n"));

        return "// @ts-nocheck\n"
                + (!imports.isEmpty() ? "%s\n\n".formatted(imports) : "")
                + """
                export class %s%s {
                %s
                }
                """
                .formatted(
                        this.getDefinitionName(),
                        this.generics.isEmpty() ? "" : "<%s>".formatted(String.join(", ", this.generics)),
                        this.fields.stream().map(EntityProcessor::toDefinition).map(s -> "\t" + s).collect(Collectors.joining("\n"))
                );
    }
}
