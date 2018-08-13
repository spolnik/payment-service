package io.payments.payment.data;

import io.payments.payment.domain.Payment;
import io.payments.payment.domain.PaymentStatus;

import java.io.Closeable;
import java.util.List;

public interface PaymentsRepository extends Closeable {
    List<Payment> findAll();
    Payment save(Payment payment);
    void updatePaymentStatus(String id, PaymentStatus paymentStatus);
}
