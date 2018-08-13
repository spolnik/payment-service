package io.payments.account.query;

import io.payments.account.data.AccountsRepository;
import io.payments.account.domain.Account;

import javax.inject.Inject;
import java.util.Optional;

public class FindAccountByIdInternally implements FindAccountById {

    private final AccountsRepository accountsRepository;

    @Inject
    public FindAccountByIdInternally(AccountsRepository accountsRepository) {
        this.accountsRepository = accountsRepository;
    }

    @Override
    public Optional<Account> run(String accountId) {
        return accountsRepository.findById(accountId);
    }
}
