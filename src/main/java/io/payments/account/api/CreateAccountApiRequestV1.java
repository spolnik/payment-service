package io.payments.account.api;

import io.payments.account.domain.Account;
import io.payments.api.ApiRequest;
import lombok.*;
import org.joda.money.Money;

import static io.payments.account.domain.Account.NOT_SAVED;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountApiRequestV1 implements ApiRequest {

    private String userId;
    private String accountId;
    private Money initialBalance;
    private String trackId;

    public Account toAccount() {
        return new Account(
                NOT_SAVED, userId, accountId, initialBalance
        );
    }
}
