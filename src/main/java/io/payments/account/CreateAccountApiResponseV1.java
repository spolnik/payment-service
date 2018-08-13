package io.payments.account;

import io.payments.api.ApiResponse;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
class CreateAccountApiResponseV1 implements ApiResponse {

    private String accountId;
    private String status;
    private String trackId;
}
