package io.payments.payment;

import io.payments.api.VersionedApi;
import spark.Route;
import spark.RouteGroup;

import javax.inject.Inject;
import java.util.List;

import static io.payments.api.Common.gson;
import static io.payments.api.Common.json;
import static spark.Spark.get;
import static spark.Spark.post;

public class PaymentApiV1 implements VersionedApi {

    private final ExecutePayment executePayment;
    private final FindAllPayments findAllPayments;

    @Inject
    public PaymentApiV1(
            ExecutePayment executePayment,
            FindAllPayments findAllPayments
    ) {
        this.executePayment = executePayment;
        this.findAllPayments = findAllPayments;
    }

    @Override
    public String version() {
        return "v1";
    }

    @Override
    public RouteGroup routes() {
        return () -> {
            post("", json(), executePayment());
            get("", findAllPayments());
        };
    }

    @Override
    public String path() {
        return "/payments";
    }

    private Route findAllPayments() {
        return (req, res) -> {
            res.type(json());

            List<Payment> payments = findAllPayments.run();
            return gson().toJson(payments);
        };
    }

    private Route executePayment() {
        return (req, res) -> {
            res.type(json());
            ExecutePaymentApiRequestV1 paymentsRequest =
                    gson().fromJson(req.body(), ExecutePaymentApiRequestV1.class);

            PaymentStatus status = executePayment.run(paymentsRequest);

            return new ExecutePaymentApiResponseV1(
                    paymentsRequest.getUserId(),
                    status.toString(),
                    paymentsRequest.getTrackId()
            ).toJson();
        };
    }
}
