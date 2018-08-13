package io.payments.account.query;

import io.payments.account.domain.Account;

import java.util.Optional;

public interface FindAccountById {
    Optional<Account> run(String accountId);
}
