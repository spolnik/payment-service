package io.payments.payment;

public interface ExecutePayment {
    PaymentStatus run(ExecutePaymentApiRequestV1 paymentsRequest);
}
