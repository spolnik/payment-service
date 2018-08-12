package io.payments.account;

public interface CreateAccount {
    AccountStatus run(CreateAccountApiRequestV1 request);
}
