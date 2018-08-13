package io.payments.account.api;

import io.payments.account.domain.AccountStatus;
import io.payments.account.command.CreateAccount;
import io.payments.account.query.FindAccountById;
import io.payments.account.query.FindAllAccounts;
import io.payments.account.domain.Account;
import io.payments.api.Common;
import io.payments.api.VersionedApi;
import spark.Route;
import spark.RouteGroup;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static io.payments.api.Common.gson;
import static spark.Spark.get;
import static spark.Spark.post;

public class AccountApiV1 implements VersionedApi {

    private final FindAccountById findAccountById;
    private final CreateAccount createAccount;
    private final FindAllAccounts findAllAccounts;

    @Inject
    public AccountApiV1(
            FindAccountById findAccountById,
            CreateAccount createAccount,
            FindAllAccounts findAllAccounts
    ) {
        this.findAccountById = findAccountById;
        this.createAccount = createAccount;
        this.findAllAccounts = findAllAccounts;
    }

    @Override
    public String version() {
        return "v1";
    }

    @Override
    public RouteGroup routes() {
        return () -> {
            get("/:accountId", findAccount());
            post("", Common.APPLICATION_JSON, createAccount());
            get("", findAllAccounts());
        };
    }

    private Route findAllAccounts() {
        return (req, res) -> {
            res.type(Common.APPLICATION_JSON);

            List<Account> accounts = findAllAccounts.run();
            return gson().toJson(accounts);
        };
    }

    private Route createAccount() {
        return (req, res) -> {
            res.type(Common.APPLICATION_JSON);

            CreateAccountApiRequestV1 createAccountRequest =
                    gson().fromJson(req.body(), CreateAccountApiRequestV1.class);

            AccountStatus status = createAccount.run(createAccountRequest);

            if (AccountStatus.ALREADY_EXIST.equals(status)) {
                res.status(409);
            }

            return new CreateAccountApiResponseV1(
                    createAccountRequest.getAccountId(),
                    status.toString(),
                    createAccountRequest.getTrackId()
            ).toJson();
        };
    }

    private Route findAccount() {
        return (req, res) -> {
            Optional<Account> account = findAccountById.run(req.params(":accountId"));

            if (account.isPresent()) {
                res.type(Common.APPLICATION_JSON);
                return gson().toJson(account.get());
            } else {
                res.status(404);
                return null;
            }
        };
    }

    @Override
    public String path() {
        return "/accounts";
    }
}
