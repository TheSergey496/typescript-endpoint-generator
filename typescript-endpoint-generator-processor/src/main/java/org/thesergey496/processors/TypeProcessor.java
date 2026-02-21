package org.thesergey496.processors;

import javax.lang.model.type.TypeMirror;
import java.util.Set;

public interface TypeProcessor extends EntityProcessor {
    Set<ImportProcessor> getUsageImports();

    default String toUsage(TypeMirror typeMirror, Set<ImportProcessor> outUsageImports) {
        outUsageImports.addAll(getUsageImports());
        return getDefinitionName();
    }
}
