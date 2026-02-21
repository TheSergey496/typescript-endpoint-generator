package org.thesergey496.processors.impl;

import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.thesergey496.SpringUtils;
import org.thesergey496.TypescriptEndpointProcessor;
import org.thesergey496.processors.ArgumentProcessor;
import org.thesergey496.processors.EntityProcessor;
import org.thesergey496.processors.ImportProcessor;
import org.thesergey496.processors.TypeProcessor;
import org.thesergey496.processors.exceptions.TypescriptEndpointGeneratorException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MethodProcessorImpl implements EntityProcessor {
    @Getter
    protected final Set<ImportProcessor> definitionImports = new HashSet<>();

    protected final String name;
    protected final String returnType;
    protected List<? extends ArgumentProcessor> arguments;
    protected final List<String> generics;
    protected final String requestMethod;
    protected final String requestPath;

    protected final List<Map.Entry<String, String>> pathVariables = new ArrayList<>();
    protected final List<Map.Entry<String, String>> requestParams = new ArrayList<>();
    protected final List<String> body = new ArrayList<>();

    public MethodProcessorImpl(ExecutableElement element, SpringUtils.SpringMappingInfo springMappingInfo) {
        this.name = element.getSimpleName().toString();
        TypeMirror elementReturnType = element.getReturnType();
        if (elementReturnType instanceof DeclaredType declaredType
                && ((TypeElement) declaredType.asElement()).getQualifiedName().contentEquals(ResponseEntity.class.getCanonicalName())) {
            elementReturnType = declaredType.getTypeArguments().getFirst();
        }
        final TypeProcessor returnTypeProcessor = TypescriptEndpointProcessor.typeRegistry.resolve(elementReturnType);
        this.definitionImports.addAll(returnTypeProcessor.getUsageImports());
        this.returnType = returnTypeProcessor.toUsage(elementReturnType, this.definitionImports);
        this.arguments = element.getParameters().stream()
                .filter(argument -> {
                    boolean needsToBeGenerated = false;

                    final String argumentName = argument.getSimpleName().toString();

                    final Map.Entry<String, String> pathVariable = TypescriptEndpointProcessor.springUtils.extractPathVariable(argument);
                    if (pathVariable != null) {
                        needsToBeGenerated = true;
                        this.pathVariables.add(pathVariable);
                    }

                    final Map.Entry<String, String> requestParam = TypescriptEndpointProcessor.springUtils.extractRequestParam(argument);
                    if (requestParam != null) {
                        needsToBeGenerated = true;
                        this.requestParams.add(requestParam);
                    }
                    final RequestBody requestBody = argument.getAnnotation(RequestBody.class);
                    if (requestBody != null) {
                        if (!body.isEmpty()) {
                            throw new TypescriptEndpointGeneratorException("Method %s has more than one RequestBody".formatted(element));
                        }
                        needsToBeGenerated = true;
                        body.add(argumentName);
                    }

                    return needsToBeGenerated;
                })
                .map(ArgumentProcessorImpl::new)
                .peek(a -> this.definitionImports.addAll(a.getDefinitionImports()))
                .sorted(Comparator.comparing(ArgumentProcessor::isOptional))
                .toList();
        this.generics = element.getTypeParameters().stream()
                .map(TypeParameterElement::asType)
                .map(TypeMirror::toString)
                .toList();

        this.requestMethod = springMappingInfo.httpMethods().getFirst();
        this.requestPath = springMappingInfo.paths().getFirst();
    }

    @Override
    public String getDefinitionName() {
        return this.name + (this.generics.isEmpty() ? "" : "<%s>".formatted(String.join(", ", this.generics)));
    }

    @Override
    public String toDefinition() {
        String pathVariables = this.pathVariables.stream()
                .filter(v -> !Objects.equals(v.getKey(), v.getValue()))
                .map(v -> "const %s = '%s';".formatted(v.getKey(), v.getValue()))
                .collect(Collectors.joining(",\n"));

        String httpParams = "const httpParams: Record<string, any> = {\n" +
                this.requestParams.stream()
                        .map(requestParam -> "%s: %s".formatted(requestParam.getKey(), requestParam.getValue()))
                        .collect(Collectors.joining(",\n"))
                        .indent(2) +
                "};";

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("public async %s(%s): Promise<%s> {\n".formatted(
                this.getDefinitionName(),
                this.arguments.stream().map(EntityProcessor::toDefinition).collect(Collectors.joining(", ")),
                this.returnType
        ));
        stringBuilder.append(pathVariables.indent(2));
        if (!this.requestParams.isEmpty()) {
            stringBuilder.append(httpParams.indent(2));
        }

        stringBuilder.append(
                "return lastValueFrom(this.httpClient.request<%s>('%s', `${this.basePath}/%s`, {"
                        .formatted(
                                this.returnType,
                                this.requestMethod,
                                this.requestPath.replaceAll("\\{(.?)}", "\\${$1}")
                        ).indent(2)
        );

        stringBuilder.append(
                Stream.of(
                                this.requestParams.isEmpty() ? null : "params: httpParams",
                                this.body.isEmpty() ? null : "body: %s".formatted(this.body.getFirst())
                        )
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(",\n"))
                        .indent(4)
        );

        stringBuilder.append("}));".indent(2));
        stringBuilder.append("}");

        return stringBuilder.toString().indent(2);
    }

    protected String prepareRequestParams() {
        final StringBuilder stringBuilder = new StringBuilder();

        if (!this.requestParams.isEmpty()) {
            stringBuilder.append("const httpParams: Record<string, any> = {\n");
            stringBuilder.append(
                    this.requestParams.stream()
                            .map(requestParam -> "%s: %s".formatted(requestParam.getKey(), requestParam.getValue()))
                            .collect(Collectors.joining(",\n"))
                            .indent(4)
            );
            stringBuilder.append("};");
        }

        return stringBuilder.toString().indent(2);
    }
}
