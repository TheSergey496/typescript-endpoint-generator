package org.thesergey496.example;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Enumeration {
    VARIANT1("1"),
    VARIANT2("jopa");

    private final String foo;
}
