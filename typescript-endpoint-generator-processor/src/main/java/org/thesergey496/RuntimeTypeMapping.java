package org.thesergey496;

import org.thesergey496.annotations.TypeMapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface RuntimeTypeMapping {
    TypeMapping value();
}
