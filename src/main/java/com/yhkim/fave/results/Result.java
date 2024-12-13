package com.yhkim.fave.results;

public interface Result {
    String NAME = "result";

    String name();

    default String nameToLower() {
        return name().toLowerCase();
    }
}
