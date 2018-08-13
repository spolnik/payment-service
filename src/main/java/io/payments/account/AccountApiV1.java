package io.payments.account;

import io.payments.api.VersionedApi;
import spark.Route;
import spark.RouteGroup;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static io.payments.api.Common.gson;
import static io.payments.api.Common.json;
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
            post("", json(), createAccount());
            get("", findAllAccounts());
        };
    }

    private Route findAllAccounts() {
        return (req, res) -> {
            res.type(json());

            List<Account> accounts = findAllAccounts.run();
            return gson().toJson(accounts);
        };
    }

    private Route createAccount() {
        return (req, res) -> {
            res.type(json());

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
                res.type(json());
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
