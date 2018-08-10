package io.payments.account;

import spark.RouteGroup;

import static spark.Spark.get;

public class RestAccountApiV1 implements AccountApiV1 {
    @Override
    public String version() {
        return "v1";
    }

    @Override
    public RouteGroup routes() {
        return () -> {
            get("/info", info());
        };
    }

    @Override
    public String path() {
        return "/accounts";
    }
}
