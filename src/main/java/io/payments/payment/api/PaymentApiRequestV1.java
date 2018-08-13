package io.payments.payment.api;

import io.payments.api.ApiRequest;
import io.payments.payment.domain.Payment;
import io.payments.payment.domain.PaymentStatus;
import lombok.*;
import org.joda.money.Money;

import java.time.Clock;
import java.time.LocalDateTime;

import static io.payments.payment.domain.Payment.NOT_SAVED;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentApiRequestV1 implements ApiRequest {

    private String userId;
    private String accountFrom;
    private String accountTo;
    private Money amount;
    private String trackId;

    public Payment toPayment() {
        return new Payment(
                NOT_SAVED,
                userId,
                accountFrom,
                accountTo,
                amount,
                trackId,
                LocalDateTime.now(Clock.systemUTC()),
                PaymentStatus.IN_PROGRESS
        );
    }
}

