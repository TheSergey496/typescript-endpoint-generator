package org.thesergey496;

import org.springframework.core.io.Resource;
import org.thesergey496.annotations.TypeMapping;
import org.thesergey496.enums.TsMappingMode;

import java.util.Collection;
import java.util.Map;

public abstract class DefaultConfig {
    @RuntimeTypeMapping(@TypeMapping(source = String.class, target = "string", mode = TsMappingMode.PRIMITIVE))
    public abstract void string();

    @RuntimeTypeMapping(@TypeMapping(source = Number.class, includeSubclasses = true, target = "number", mode = TsMappingMode.PRIMITIVE))
    public abstract void number();

    @RuntimeTypeMapping(@TypeMapping(source = Map.class, includeSubclasses = true, mode = TsMappingMode.MAP))
    public abstract void map();

    @RuntimeTypeMapping(@TypeMapping(source = Collection.class, includeSubclasses = true, mode = TsMappingMode.COLLECTION))
    public abstract void collection();

    @RuntimeTypeMapping(@TypeMapping(source = Resource.class, includeSubclasses = true, mode = TsMappingMode.PRIMITIVE, target = "Blob"))
    public abstract void resource();
}
