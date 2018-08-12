package io.payments.payment;

import java.io.Closeable;
import java.util.List;

public interface PaymentsRepository extends Closeable {
    List<Payment> findAll();
    Payment save(Payment payment);
}
