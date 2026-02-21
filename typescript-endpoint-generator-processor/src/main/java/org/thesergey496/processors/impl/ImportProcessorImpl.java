package org.thesergey496.processors.impl;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.thesergey496.processors.ImportProcessor;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("ClassCanBeRecord")
@RequiredArgsConstructor
@Data
@Setter(AccessLevel.NONE)
class ImportProcessorImpl implements ImportProcessor {
    protected final String entityName;
    protected final String source;

    @Override
    public String convertToTs(@Nullable String pathPrefix) {
        return "import {%s} from '%s';".formatted(getEntityName(), Stream.of(pathPrefix, getSource()).filter(Objects::nonNull).collect(Collectors.joining("/")));
    }
}
