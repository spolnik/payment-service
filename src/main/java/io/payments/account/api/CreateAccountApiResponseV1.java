package io.payments.account.api;

import io.payments.api.ApiResponse;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountApiResponseV1 implements ApiResponse {

    private String accountId;
    private String status;
    private String trackId;
}
