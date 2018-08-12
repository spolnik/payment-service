package io.payments.payment;

import java.util.List;

public interface PaymentsRepository {
    List<Payment> findAll();
    Payment save(Payment payment);
}
