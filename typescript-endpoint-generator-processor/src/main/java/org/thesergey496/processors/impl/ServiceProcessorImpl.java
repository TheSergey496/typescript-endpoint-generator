package org.thesergey496.processors.impl;

import lombok.Getter;
import org.thesergey496.SpringUtils;
import org.thesergey496.TypescriptEndpointProcessor;
import org.thesergey496.annotations.TypescriptIgnore;
import org.thesergey496.annotations.TypescriptService;
import org.thesergey496.processors.EntityProcessor;
import org.thesergey496.processors.ImportProcessor;
import org.thesergey496.processors.ServiceProcessor;
import org.thesergey496.processors.exceptions.TypescriptEndpointGeneratorException;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ServiceProcessorImpl implements ServiceProcessor {
    @Getter
    protected final String definitionName;
    @Getter
    protected final Set<EntityProcessor> methods;

    protected final List<String> generics;
    protected final String basePath;

    public ServiceProcessorImpl(TypeElement element) {
        final TypescriptService annotation = element.getAnnotation(TypescriptService.class);
        if (annotation == null) {
            throw new TypescriptEndpointGeneratorException("Controller must be annotated with @" + TypescriptService.class.getSimpleName());
        }

        this.definitionName = extractName(element, annotation);
        this.methods = ElementFilter.methodsIn(element.getEnclosedElements()).stream()
                .filter(e -> e.getModifiers().contains(Modifier.PUBLIC) && !e.getModifiers().contains(Modifier.STATIC))
                .filter(e -> e.getAnnotation(TypescriptIgnore.class) == null)
                .flatMap(e -> {
                    final SpringUtils.SpringMappingInfo springMappingInfo = TypescriptEndpointProcessor.springUtils.extractSpringMappingInfo(e);
                    if (springMappingInfo == null) {
                        return Stream.empty();
                    }
                    return Stream.of(new MethodProcessorImpl(e, springMappingInfo));
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
        this.generics = element.getTypeParameters().stream()
                .map(TypeParameterElement::asType)
                .map(TypeMirror::toString)
                .toList();
        this.basePath = TypescriptEndpointProcessor.springUtils.extractBasePath(element);
    }

    @Override
    public Set<ImportProcessor> getDefinitionImports() {
        return this.getMethods().stream()
                .map(EntityProcessor::getDefinitionImports)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public String toDefinition() {
        final String imports = this.getDefinitionImports().stream()
                .map(importProcessor -> importProcessor.convertToTs("../dto"))
                .sorted()
                .collect(Collectors.joining("\n"));
        return """
                import {inject, Injectable} from '@angular/core';
                import {HttpClient} from '@angular/common/http';
                import {lastValueFrom} from 'rxjs';
                %s
                @Injectable({
                  providedIn: 'root',
                })
                export class %s%s {
                  private readonly httpClient = inject(HttpClient);
                  private readonly basePath = '%s';
                
                %s
                }
                """.formatted(
                (!imports.isEmpty() ? "\n%s\n".formatted(imports) : ""),
                this.getDefinitionName(),
                this.generics.isEmpty() ? "" : "<%s>".formatted(String.join(", ", this.generics)),
                this.basePath,
                this.getMethods().stream().map(EntityProcessor::toDefinition).collect(Collectors.joining("\n"))
        );
    }

    protected String extractName(Element element, TypescriptService annotation) {
        String className = element.getSimpleName().toString();
        final String annotationName = annotation.name();
        if (annotationName.isEmpty()) {
            if (className.endsWith("Controller")) {
                className = className.substring(0, className.length() - "Controller".length());
            }
            className += "Service";
        } else {
            className = annotationName;
        }

        return className;
    }
}
