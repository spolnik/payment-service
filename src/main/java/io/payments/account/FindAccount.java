package io.payments.account;

import java.util.Optional;

public interface FindAccount {
    Optional<Account> run(String accountId);
}
