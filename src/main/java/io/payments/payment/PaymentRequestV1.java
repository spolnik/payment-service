package io.payments.payment;

import io.payments.api.ApiRequest;
import io.payments.api.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.money.Money;

@Data
@AllArgsConstructor
@NoArgsConstructor
class PaymentRequestV1 implements ApiRequest {
    private String userId;
    private String accountFrom;
    private String accountTo;
    private Money amount;
    private String trackId;
}

