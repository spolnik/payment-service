package io.payments.payment;

import com.google.common.base.Strings;
import io.payments.api.VersionedApi;
import spark.Route;
import spark.RouteGroup;

import javax.inject.Inject;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
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
            PaymentApiRequestV1 paymentsRequest =
                    gson().fromJson(req.body(), PaymentApiRequestV1.class);

            PaymentStatus status = isInvalid(paymentsRequest)
                    ? PaymentStatus.REJECTED
                    : executePayment.run(paymentsRequest);

            return new PaymentApiResponseV1(
                    paymentsRequest.getUserId(),
                    status.toString(),
                    paymentsRequest.getTrackId()
            ).toJson();
        };
    }

    private boolean isInvalid(PaymentApiRequestV1 paymentsRequest) {
        return isNullOrEmpty(paymentsRequest.getUserId()) ||
                isNullOrEmpty(paymentsRequest.getAccountFrom()) ||
                isNullOrEmpty(paymentsRequest.getAccountTo()) ||
                paymentsRequest.getAmount() == null;
    }
}
