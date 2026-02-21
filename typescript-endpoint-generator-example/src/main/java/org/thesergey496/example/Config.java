package org.thesergey496.example;

import org.thesergey496.annotations.TypeMapping;
import org.thesergey496.enums.TsMappingMode;

public abstract class Config {
    @TypeMapping(source = TestId.class, target = "number", mode = TsMappingMode.ALIAS, includeSubclasses = true)
    public abstract void testId();

    @TypeMapping(source = Void.class, target = "../../dto/custom-void", mode = TsMappingMode.CUSTOM)
    public abstract void voidCustomType();
}
