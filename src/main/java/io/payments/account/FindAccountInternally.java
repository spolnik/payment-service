package io.payments.account;

import javax.inject.Inject;
import java.util.Optional;

public class FindAccountInternally implements FindAccount {

    private final AccountsRepository accountsRepository;

    @Inject
    public FindAccountInternally(AccountsRepository accountsRepository) {
        this.accountsRepository = accountsRepository;
    }

    @Override
    public Optional<Account> run(String accountId) {
        return accountsRepository.findById(accountId);
    }
}
