package io.payments.payment;

public interface ExecutePayment {
    PaymentStatus run(PaymentApiRequestV1 paymentsRequest);
}
