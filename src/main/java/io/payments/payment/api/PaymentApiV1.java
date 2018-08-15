package io.payments.payment.api;

import io.payments.api.VersionedApi;
import io.payments.payment.command.ExecutePayment;
import io.payments.payment.domain.Payment;
import io.payments.payment.domain.PaymentStatus;
import io.payments.payment.query.FindAllPayments;
import spark.Route;
import spark.RouteGroup;

import javax.inject.Inject;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static io.payments.api.Common.APPLICATION_JSON;
import static io.payments.api.Common.gson;
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
            get("", findAllPayments());
            post("", APPLICATION_JSON, executePayment());
        };
    }

    @Override
    public String path() {
        return "/payments";
    }

    private Route findAllPayments() {
        return (req, res) -> {
            res.type(APPLICATION_JSON);

            List<Payment> payments = findAllPayments.run();
            return gson().toJson(payments);
        };
    }

    private Route executePayment() {
        return (req, res) -> {
            res.type(APPLICATION_JSON);

            PaymentApiRequestV1 paymentsRequest =
                    gson().fromJson(req.body(), PaymentApiRequestV1.class);

            PaymentStatus status = validateAndExecute(paymentsRequest);

            return new PaymentApiResponseV1(
                    paymentsRequest.getUserId(),
                    status.toString(),
                    paymentsRequest.getTrackId()
            ).toJson();
        };
    }

    private PaymentStatus validateAndExecute(PaymentApiRequestV1 paymentsRequest) {
        if (isNullOrEmpty(paymentsRequest.getUserId())) {
            return PaymentStatus.USER_ID_MISSING;
        }

        if (isNullOrEmpty(paymentsRequest.getAccountFrom())) {
            return PaymentStatus.ACCOUNT_FROM_MISSING;
        }

        if (isNullOrEmpty(paymentsRequest.getAccountTo())) {
            return PaymentStatus.ACCOUNT_TO_MISSING;
        }

        if (paymentsRequest.getAmount() == null) {
            return PaymentStatus.AMOUNT_MISSING;
        }

        return executePayment.run(paymentsRequest);
    }
}
