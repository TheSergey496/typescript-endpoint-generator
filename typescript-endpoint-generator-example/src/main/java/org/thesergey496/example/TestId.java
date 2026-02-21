package org.thesergey496.example;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestId extends Number {
    private final Long value;

    @Override
    public int intValue() {
        return value.intValue();
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return value.floatValue();
    }

    @Override
    public double doubleValue() {
        return value.doubleValue();
    }
}
