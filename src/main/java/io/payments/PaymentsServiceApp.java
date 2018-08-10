package io.payments;

import spark.Route;

import static spark.Spark.*;

public class PaymentsServiceApp {

    private static final String API_PREFIX = "/api/v1";

    public static void main(String[] args) {
        get("/", home());
        get(API_PREFIX + "/info", home());
    }

    private static Route home() {
        return (req, res) -> "Payments Service 1.0.0";
    }
}
