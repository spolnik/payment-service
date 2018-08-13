package io.payments.payment;

import io.payments.api.ApiResponse;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
class PaymentApiResponseV1 implements ApiResponse {
    private String userId;
    private String status;
    private String trackId;
}
