package io.payments.payment;

import io.payments.api.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
class PaymentResponseV1 implements ApiResponse {
    private String userId;
    private String status;
    private String trackId;
}
