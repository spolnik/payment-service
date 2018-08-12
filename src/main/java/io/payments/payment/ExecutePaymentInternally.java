package io.payments.payment;

import javax.inject.Inject;

public class ExecutePaymentInternally implements ExecutePayment {

    private final PaymentsRepository paymentsRepository;

    @Inject
    public ExecutePaymentInternally(PaymentsRepository paymentsRepository) {
        this.paymentsRepository = paymentsRepository;
    }

    @Override
    public PaymentStatus run(PaymentRequestV1 paymentsRequest) {
        store(paymentsRequest.toPayment());

        return PaymentStatus.COMPLETED;

    }

    private Payment store(Payment payment) {
        return paymentsRepository.save(payment);
    }
}
