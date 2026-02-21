package org.thesergey496.processors.impl;

import com.google.common.base.CaseFormat;
import lombok.RequiredArgsConstructor;
import org.thesergey496.processors.GeneratedTypeProcessor;
import org.thesergey496.processors.ImportProcessor;
import org.thesergey496.processors.TypeProcessor;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AliasTypeProcessor implements GeneratedTypeProcessor {
    protected final String alias;
    protected final TypeProcessor underlyingType;

    @Override
    public Set<ImportProcessor> getUsageImports() {
        return Collections.singleton(new ImportProcessorImpl(this.getDefinitionName(), CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, this.alias)));
    }

    @Override
    public String getDefinitionName() {
        return this.alias;
    }

    @Override
    public Set<ImportProcessor> getDefinitionImports() {
        return this.underlyingType.getUsageImports();
    }

    @Override
    public String toDefinition() {
        final String imports = getDefinitionImports().stream()
                .map(importProcessor -> importProcessor.convertToTs(null))
                .sorted()
                .collect(Collectors.joining("\n"));
        return (!imports.isEmpty() ? "%s\n\n".formatted(imports) : "")
                + """
                export type %s = %s;
                """
                .formatted(
                        alias,
                        underlyingType.getDefinitionName()
                );
    }
}
