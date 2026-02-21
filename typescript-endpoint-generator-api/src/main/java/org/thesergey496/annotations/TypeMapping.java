package org.thesergey496.annotations;

import org.thesergey496.enums.TsMappingMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // We put this on dummy methods
@Retention(RetentionPolicy.SOURCE)
public @interface TypeMapping {
    Class<?> source();

    String target() default "";

    TsMappingMode mode();

    boolean includeSubclasses() default false;
}
