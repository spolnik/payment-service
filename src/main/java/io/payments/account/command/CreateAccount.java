package io.payments.account.command;

import io.payments.account.api.CreateAccountApiRequestV1;
import io.payments.account.domain.AccountStatus;

public interface CreateAccount {
    AccountStatus run(CreateAccountApiRequestV1 request);
}
