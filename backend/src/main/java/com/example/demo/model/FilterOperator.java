package com.example.demo.model;

import java.util.Arrays;

public enum FilterOperator {
    eq, ne, like, not_like, gt, lt, gte, lte, in, not_in, between, is_null, is_not_null;

    public static FilterOperator from(String op) {
        return Arrays.stream(values())
                .filter(o -> o.name().equalsIgnoreCase(op))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid operator: " + op));
    }
}
