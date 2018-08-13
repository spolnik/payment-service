package io.payments.account.command;

import io.payments.account.api.CreateAccountApiRequestV1;
import io.payments.account.data.AccountsRepository;
import io.payments.account.domain.AccountStatus;

import javax.inject.Inject;

public class CreateAccountInternally implements CreateAccount {

    private final AccountsRepository repository;

    @Inject
    public CreateAccountInternally(AccountsRepository repository) {
        this.repository = repository;
    }

    @Override
    public AccountStatus run(CreateAccountApiRequestV1 request) {
        return repository.save(request.toAccount());
    }
}
