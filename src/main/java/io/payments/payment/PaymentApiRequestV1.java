package io.payments.payment;

import io.payments.api.ApiRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.money.Money;

import java.time.Clock;
import java.time.LocalDateTime;

import static io.payments.payment.Payment.NOT_SAVED;

@Data
@AllArgsConstructor
@NoArgsConstructor
class PaymentApiRequestV1 implements ApiRequest {

    private String userId;
    private String accountFrom;
    private String accountTo;
    private Money amount;
    private String trackId;

    Payment toPayment() {
        return new Payment(
                NOT_SAVED, userId, accountFrom, accountTo, amount, trackId, LocalDateTime.now(Clock.systemUTC())
        );
    }
}

