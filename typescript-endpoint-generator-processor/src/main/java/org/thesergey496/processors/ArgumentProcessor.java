package org.thesergey496.processors;

import javax.annotation.Nullable;

public interface ArgumentProcessor extends FieldProcessor {
    @Nullable
    String getDefaultValue();
}
