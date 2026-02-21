package org.thesergey496.processors.impl;

import lombok.Getter;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;
import org.thesergey496.processors.ArgumentProcessor;

import javax.lang.model.element.VariableElement;
import java.util.Optional;


public class ArgumentProcessorImpl extends FieldProcessorImpl implements ArgumentProcessor {
    protected final boolean required;
    @Getter
    protected final String defaultValue;

    public ArgumentProcessorImpl(VariableElement variableElement) {
        super(variableElement);
        final RequestParam requestParam = variableElement.getAnnotation(RequestParam.class);
        this.required = Optional.ofNullable(requestParam).map(RequestParam::required).orElse(false);
        this.defaultValue = Optional.ofNullable(requestParam).map(RequestParam::defaultValue)
                .map(s -> {
                    if (ValueConstants.DEFAULT_NONE.equals(s)) return null;
                    else return s;
                })
                .orElse(null);
    }

    @Override
    public String toDefinition() {
        return "%s%s: %s%s".formatted(
                this.getDefinitionName(),
                this.isOptional() ? "?" : "",
                this.type,
                Optional.ofNullable(this.getDefaultValue()).map(defaultValue -> " = " + defaultValue).orElse("")
        );
    }

    @Override
    public boolean isOptional() {
        return super.isOptional() || !this.required || this.defaultValue != null;
    }
}
