package org.thesergey496.processors;

import javax.annotation.Nullable;

public interface ImportProcessor {
    String getEntityName();

    String convertToTs(@Nullable String pathPrefix);
}
