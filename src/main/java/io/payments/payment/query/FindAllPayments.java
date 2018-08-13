package io.payments.payment.query;

import io.payments.payment.domain.Payment;

import java.util.List;

public interface FindAllPayments {
    List<Payment> run();
}
