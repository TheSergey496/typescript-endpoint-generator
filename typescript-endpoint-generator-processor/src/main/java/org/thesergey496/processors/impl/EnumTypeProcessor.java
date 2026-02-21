package org.thesergey496.processors.impl;

import com.google.common.base.CaseFormat;
import org.thesergey496.processors.GeneratedTypeProcessor;
import org.thesergey496.processors.ImportProcessor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumTypeProcessor implements GeneratedTypeProcessor {
    protected final String name;
    protected final Set<String> enumConstants;

    public EnumTypeProcessor(Element element) {
        this.name = element.getSimpleName().toString();
        // Cast the Element to TypeElement (an enum is a kind of class)
        TypeElement enumTypeElement = (TypeElement) element;

        enumConstants = enumTypeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.ENUM_CONSTANT)
                .map(e -> (VariableElement) e)
                .map(Element::toString)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<ImportProcessor> getUsageImports() {
        return Collections.singleton(new ImportProcessorImpl(this.name, CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, this.name)));
    }

    @Override
    public String getDefinitionName() {
        return this.name;
    }

    @Override
    public Set<ImportProcessor> getDefinitionImports() {
        return Collections.emptySet();
    }

    @Override
    public String toDefinition() {
        return """
                const <name> = {
                %s
                } as const;
                
                export type <name> = typeof <name>[keyof typeof <name>];
                """
                .formatted(
                        this.enumConstants.stream()
                                .map(s -> "\t%s: '%s'".formatted(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, s), s))
                                .collect(Collectors.joining(",\n"))
                )
                .replaceAll("<name>", this.getDefinitionName());
    }
}
