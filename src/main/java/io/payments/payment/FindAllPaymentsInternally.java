package io.payments.payment;

import javax.inject.Inject;
import java.util.List;

public class FindAllPaymentsInternally implements FindAllPayments {

    private final PaymentsRepository paymentsRepository;

    @Inject
    public FindAllPaymentsInternally(PaymentsRepository paymentsRepository) {
        this.paymentsRepository = paymentsRepository;
    }

    @Override
    public List<Payment> run() {
        return paymentsRepository.findAll();
    }
}
