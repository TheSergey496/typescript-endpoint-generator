package org.thesergey496;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpringUtils {
    protected final Elements elementUtils;
    protected final Types typeUtils;

    public SpringUtils(ProcessingEnvironment processingEnv) {
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
    }

    public String extractBasePath(TypeElement classElement) {
        TypeElement requestMappingType = elementUtils.getTypeElement(RequestMapping.class.getCanonicalName());

        for (AnnotationMirror classAnnotation : classElement.getAnnotationMirrors()) {
            AnnotationMirror requestMappingMirror = findRequestMappingMirror(classAnnotation, requestMappingType);
            if (requestMappingMirror != null) {
                List<String> paths = extractPaths(requestMappingMirror);
                return paths.isEmpty() ? "" : trimSlashes(paths.getFirst());
            }
        }
        return "";
    }

    public SpringMappingInfo extractSpringMappingInfo(ExecutableElement methodElement) {
        TypeElement requestMappingType = elementUtils.getTypeElement(RequestMapping.class.getCanonicalName());

        for (AnnotationMirror methodAnnotation : methodElement.getAnnotationMirrors()) {
            AnnotationMirror requestMappingMirror = findRequestMappingMirror(methodAnnotation, requestMappingType);

            if (requestMappingMirror != null) {
                List<String> paths = extractPaths(methodAnnotation);
                List<String> methods = extractHttpMethods(requestMappingMirror);
                return new SpringMappingInfo(methods, paths);
            }
        }
        return null;
    }

    public Map.Entry<String, String> extractPathVariable(VariableElement argument) {
        final String argumentName = argument.getSimpleName().toString();

        final PathVariable pathVariable = argument.getAnnotation(PathVariable.class);
        if (pathVariable == null) {
            return null;
        }
        if (!pathVariable.value().isBlank()) {
            return Map.entry(pathVariable.value(), argumentName);
        }
        if (!pathVariable.name().isBlank()) {
            return Map.entry(pathVariable.name(), argumentName);
        }
        return Map.entry(argumentName, argumentName);
    }

    public Map.Entry<String, String> extractRequestParam(VariableElement argument) {
        final String argumentName = argument.getSimpleName().toString();

        final RequestParam requestParam = argument.getAnnotation(RequestParam.class);
        if (requestParam == null) {
            return null;
        }
        if (!requestParam.value().isBlank()) {
            return Map.entry(requestParam.value(), argumentName);
        }
        if (!requestParam.name().isBlank()) {
            return Map.entry(requestParam.name(), argumentName);
        }
        return Map.entry(argumentName, argumentName);
    }

    protected AnnotationMirror findRequestMappingMirror(AnnotationMirror annotationMirror, TypeElement requestMappingType) {
        TypeMirror annotationType = annotationMirror.getAnnotationType();

        if (typeUtils.isSameType(annotationType, requestMappingType.asType())) {
            return annotationMirror;
        }

        Element annotationElement = typeUtils.asElement(annotationType);
        for (AnnotationMirror metaAnnotation : annotationElement.getAnnotationMirrors()) {
            if (typeUtils.isSameType(metaAnnotation.getAnnotationType(), requestMappingType.asType())) {
                return metaAnnotation;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected List<String> extractPaths(AnnotationMirror annotationMirror) {
        List<String> paths = new ArrayList<>();
        Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotationMirror.getElementValues();

        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
            String key = entry.getKey().getSimpleName().toString();
            if ("path".equals(key) || "value".equals(key)) {
                Object val = entry.getValue().getValue();
                if (val instanceof List) {
                    for (AnnotationValue av : (List<? extends AnnotationValue>) val) {
                        paths.add(trimSlashes(av.getValue().toString()));
                    }
                } else if (val instanceof String) {
                    paths.add(trimSlashes(val.toString()));
                }
            }
        }
        if (paths.isEmpty()) {
            paths.add("");
        }
        return paths;
    }

    @SuppressWarnings("unchecked")
    protected List<String> extractHttpMethods(AnnotationMirror requestMappingMirror) {
        List<String> methods = new ArrayList<>();
        Map<? extends ExecutableElement, ? extends AnnotationValue> values = requestMappingMirror.getElementValues();

        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
            if ("method".equals(entry.getKey().getSimpleName().toString())) {
                Object val = entry.getValue().getValue();
                if (val instanceof List) {
                    for (AnnotationValue av : (List<? extends AnnotationValue>) val) {
                        if (av.getValue() instanceof VariableElement) {
                            methods.add(((VariableElement) av.getValue()).getSimpleName().toString());
                        }
                    }
                }
            }
        }

        if (methods.isEmpty()) {
            methods.add("ANY");
        }
        return methods;
    }

    protected static @NonNull String trimSlashes(String path) {
        return StringUtils.trimTrailingCharacter(StringUtils.trimLeadingCharacter(path, '/'), '/');
    }

    public record SpringMappingInfo(
            List<String> httpMethods,
            List<String> paths) {
    }
}
