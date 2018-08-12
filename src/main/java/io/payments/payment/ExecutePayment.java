package io.payments.payment;

public interface ExecutePayment {
    PaymentStatus run(PaymentRequestV1 paymentsRequest);
}
