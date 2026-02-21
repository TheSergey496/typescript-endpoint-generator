package org.thesergey496.processors.impl;

import lombok.Getter;
import org.thesergey496.TypescriptEndpointProcessor;
import org.thesergey496.processors.ImportProcessor;
import org.thesergey496.processors.TypeProcessor;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class MapTypeProcessor implements TypeProcessor {
    protected final String usageName = "Record";
    protected final Set<ImportProcessor> usageImports = new HashSet<>();

    public MapTypeProcessor() {
    }

    @Override
    public String getDefinitionName() {
        return "";
    }

    @Override
    public Set<ImportProcessor> getDefinitionImports() {
        return Set.of();
    }

    @Override
    public String toDefinition() {
        return "";
    }

    @Override
    public String toUsage(TypeMirror typeMirror, Set<ImportProcessor> outUsageImports) {
        final String defaultUsage = TypeProcessor.super.toUsage(typeMirror, outUsageImports);

        if (typeMirror instanceof DeclaredType declaredType) {
            List<? extends TypeMirror> args = declaredType.getTypeArguments();
            String keyType = args.isEmpty() ? "string" : TypescriptEndpointProcessor.typeRegistry.resolve(args.get(0)).toUsage(args.get(0), outUsageImports);
            String valType = args.size() < 2 ? "any" : TypescriptEndpointProcessor.typeRegistry.resolve(args.get(1)).toUsage(args.get(1), outUsageImports);
            return "Record<" + keyType + ", " + valType + ">";
        }

        return defaultUsage;
    }
}
