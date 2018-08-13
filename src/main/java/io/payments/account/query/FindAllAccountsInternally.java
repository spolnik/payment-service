package io.payments.account.query;

import io.payments.account.data.AccountsRepository;
import io.payments.account.domain.Account;

import javax.inject.Inject;
import java.util.List;

public class FindAllAccountsInternally implements FindAllAccounts {

    private final AccountsRepository accountsRepository;

    @Inject
    public FindAllAccountsInternally(AccountsRepository accountsRepository) {
        this.accountsRepository = accountsRepository;
    }

    @Override
    public List<Account> run() {
        return accountsRepository.findAll();
    }
}
