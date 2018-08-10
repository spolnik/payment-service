package io.payments.payment;

import spark.RouteGroup;

import static spark.Spark.get;

public class RestPaymentApiV1 implements PaymentApiV1 {
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
        return "/payments";
    }
}
