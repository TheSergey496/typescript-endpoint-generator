package org.thesergey496.processors;

import java.util.Set;

public interface EntityProcessor {
    String getDefinitionName();

    Set<ImportProcessor> getDefinitionImports();

    String toDefinition();
}
