package io.payments.api;

import spark.RouteGroup;

public interface Router {
    RouteGroup routes();
    String path();
}

