package io.payments.account;

import java.util.Optional;

public interface FindAccountById {
    Optional<Account> run(String accountId);
}
