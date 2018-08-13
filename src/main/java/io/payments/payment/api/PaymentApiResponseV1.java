package io.payments.payment.api;

import io.payments.api.ApiResponse;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentApiResponseV1 implements ApiResponse {
    private String userId;
    private String status;
    private String trackId;
}
