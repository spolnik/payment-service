package io.payments.user;

import spark.RouteGroup;

import static spark.Spark.*;

public class RestUserApiV1 implements UserApiV1 {
    @Override
    public String version() {
        return "v1";
    }

    @Override
    public RouteGroup routes() {
        return () -> {
            get("info", info());
        };
    }

    @Override
    public String path() {
        return "/users";
    }
}
