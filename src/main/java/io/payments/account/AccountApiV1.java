package io.payments.account;

import io.payments.api.VersionedApi;
import spark.RouteGroup;

import static spark.Spark.get;

public class AccountApiV1 implements VersionedApi {
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
