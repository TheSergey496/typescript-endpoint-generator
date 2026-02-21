package org.thesergey496.processors.impl;

import lombok.RequiredArgsConstructor;
import org.thesergey496.processors.ImportProcessor;
import org.thesergey496.processors.TypeProcessor;

import java.util.Collections;
import java.util.Set;

@SuppressWarnings("ClassCanBeRecord")
@RequiredArgsConstructor
public class PrimitiveTypeProcessor implements TypeProcessor {
    public static TypeProcessor ANY = new PrimitiveTypeProcessor("any");
    public static TypeProcessor BOOLEAN = new PrimitiveTypeProcessor("boolean");
    public static TypeProcessor NUMBER = new PrimitiveTypeProcessor("number");

    protected final String name;


    @Override
    public Set<ImportProcessor> getUsageImports() {
        return Collections.emptySet();
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
        return "";
    }
}
