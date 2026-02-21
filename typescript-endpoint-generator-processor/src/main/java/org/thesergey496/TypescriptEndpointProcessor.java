package org.thesergey496;

import com.google.auto.service.AutoService;
import com.google.common.base.CaseFormat;
import org.thesergey496.annotations.TypescriptService;
import org.thesergey496.processors.EntityProcessor;
import org.thesergey496.processors.ServiceProcessor;
import org.thesergey496.processors.impl.ServiceProcessorImpl;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@SupportedAnnotationTypes("org.thesergey496.annotations.TypescriptService")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class TypescriptEndpointProcessor extends AbstractProcessor {
    public static TypeRegistry typeRegistry;
    public static SpringUtils springUtils;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        typeRegistry = ConfigLoader.loadTypeRegistry(processingEnv, roundEnv);
        springUtils = new SpringUtils(processingEnv);

        String outputPath = processingEnv.getOptions().get("ts.output.path");

        if (outputPath == null || outputPath.isEmpty()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                    "No 'ts.output.path' option provided. Skipping TypeScript generation.");
            return false;
        }

        File servicesPath = new File(Path.of(outputPath, "services").toUri());
        if (!servicesPath.exists()) {
            var _mkdirs = servicesPath.mkdirs();
        }
        File dtoPath = new File(Path.of(outputPath, "dto").toUri());
        if (!dtoPath.exists()) {
            var _mkdirs = dtoPath.mkdirs();
        }

        final Set<EntityProcessor> entitiesToGenerate = new HashSet<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(TypescriptService.class)) {
            final ServiceProcessor serviceProcessor = new ServiceProcessorImpl((TypeElement) element);
            final File file = new File(servicesPath, CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, serviceProcessor.getDefinitionName()) + ".ts");
            try (FileWriter writer = new FileWriter(file)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                        "Generating TypeScript file: " + file.getAbsolutePath());
                writer.write(serviceProcessor.toDefinition());

                entitiesToGenerate.addAll(typeRegistry.getTypeProcessors());
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Failed to write TypeScript file: " + e.getMessage());
            }
        }

        for (EntityProcessor entity : entitiesToGenerate) {
            final File file = new File(dtoPath, CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, entity.getDefinitionName()) + ".ts");
            final String entityBody = entity.toDefinition();
            if (entityBody.isEmpty()) {
                continue;
            }
            try (FileWriter writer = new FileWriter(file)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                        "Generating TypeScript file: " + file.getAbsolutePath());
                writer.write(entityBody);
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Failed to write TypeScript file: " + e.getMessage());
            }
        }

        return true;
    }
}

