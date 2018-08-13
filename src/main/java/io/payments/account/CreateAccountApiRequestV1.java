package io.payments.account;

import io.payments.api.ApiRequest;
import lombok.*;
import org.joda.money.Money;

import static io.payments.account.Account.NOT_SAVED;

@Getter
@Setter
@EqualsAndHashCode
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
