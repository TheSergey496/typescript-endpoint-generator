package org.thesergey496;

import com.google.common.collect.Streams;
import org.thesergey496.annotations.TypeMapping;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class ConfigLoader {
    public static TypeRegistry loadTypeRegistry(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv) {
        final Elements elementUtils = processingEnv.getElementUtils();
        final Types typeUtils = processingEnv.getTypeUtils();

        final TypeRegistry registry = new TypeRegistry(elementUtils, typeUtils);

        final Stream<TypeMapping> defaultMappings = Arrays.stream(DefaultConfig.class.getMethods())
                .map(m -> m.getAnnotation(RuntimeTypeMapping.class))
                .filter(Objects::nonNull)
                .map(RuntimeTypeMapping::value);

        final Stream<TypeMapping> userDefinedMappings = roundEnv.getElementsAnnotatedWith(TypeMapping.class).stream()
                .map(element -> element.getAnnotation(TypeMapping.class));

        Streams.concat(defaultMappings, userDefinedMappings).forEach(mapping -> {
            try {
                final TypeMirror typeMirror = TypeMirrorHelper.getTypeMirrorFromTypeMapping(mapping, elementUtils, typeUtils);
                registry.addRule(typeMirror, mapping);
            } catch (Exception e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getLocalizedMessage());
            }
        });

        return registry;
    }
}