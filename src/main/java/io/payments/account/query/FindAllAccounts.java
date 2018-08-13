package io.payments.account.query;

import io.payments.account.domain.Account;

import java.util.List;

public interface FindAllAccounts {
    List<Account> run();
}
