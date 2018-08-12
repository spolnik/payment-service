package io.payments.payment;

public class ExecutePaymentInternally implements ExecutePayment {

    public ExecutePaymentInternally() {
    }

    @Override
    public PaymentStatus run(PaymentRequestV1 paymentsRequest) {
        return PaymentStatus.COMPLETED;
    }
}
