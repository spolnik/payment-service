package io.payments.account;

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
