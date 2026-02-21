package org.thesergey496.processors.impl;

import lombok.RequiredArgsConstructor;
import org.thesergey496.processors.ImportProcessor;
import org.thesergey496.processors.TypeProcessor;

import java.util.Collections;
import java.util.Set;

@RequiredArgsConstructor
public class CustomTypeProcessor implements TypeProcessor {
    protected final String name;
    protected final String source;

    @Override
    public Set<ImportProcessor> getUsageImports() {
        return Collections.singleton(new ImportProcessorImpl(this.getDefinitionName(), source));
    }

    @Override
    public String getDefinitionName() {
        return this.name;
    }

    @Override
    public Set<ImportProcessor> getDefinitionImports() {
        return Set.of();
    }

    @Override
    public String toDefinition() {
        return "";
    }
}
