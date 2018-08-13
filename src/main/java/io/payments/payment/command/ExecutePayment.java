package io.payments.payment.command;

import io.payments.payment.api.PaymentApiRequestV1;
import io.payments.payment.domain.PaymentStatus;

public interface ExecutePayment {
    PaymentStatus run(PaymentApiRequestV1 paymentsRequest);
}
