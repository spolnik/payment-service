package io.payments.payment;

import io.payments.api.Common;
import io.payments.api.VersionedApi;
import spark.Route;
import spark.RouteGroup;

import javax.inject.Inject;

import static io.payments.api.Common.json;
import static spark.Spark.get;
import static spark.Spark.post;

public class PaymentApiV1 implements VersionedApi {

    private final ExecutePayment executePayment;

    @Inject
    public PaymentApiV1(ExecutePayment executePayment) {
        this.executePayment = executePayment;
    }

    @Override
    public String version() {
        return "v1";
    }

    @Override
    public RouteGroup routes() {
        return () -> {
            get("info", info());
            post("", json(), processPayment());
        };
    }

    @Override
    public String path() {
        return "/payments";
    }

    private Route processPayment() {
        return (req, res) -> {
            res.type(json());
            PaymentRequestV1 paymentsRequest = Common.gson().fromJson(req.body(), PaymentRequestV1.class);

            PaymentStatus status = executePayment.run(paymentsRequest);

            return new PaymentResponseV1(
                    paymentsRequest.getUserId(),
                    status.toString(),
                    paymentsRequest.getTrackId()
            ).toJson();
        };
    }
}
