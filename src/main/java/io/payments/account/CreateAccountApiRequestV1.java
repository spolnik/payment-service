package io.payments.account;

import io.payments.api.ApiRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.money.Money;

import static io.payments.account.Account.NOT_SAVED;

@Data
@AllArgsConstructor
@NoArgsConstructor
class CreateAccountApiRequestV1 implements ApiRequest {

    private String userId;
    private String accountId;
    private Money initialBalance;
    private String trackId;

    Account toAccount() {
        return new Account(
                NOT_SAVED, userId, accountId, initialBalance
        );
    }
}
