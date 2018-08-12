package io.payments.api;

@FunctionalInterface
public interface Function<One, Two, Response> {
    Response apply(One one, Two two);
}
