package io.payments.account;

import io.payments.api.VersionedApi;
import spark.Route;
import spark.RouteGroup;

import javax.inject.Inject;
import java.util.Optional;

import static io.payments.api.Common.gson;
import static io.payments.api.Common.json;
import static spark.Spark.get;
import static spark.Spark.post;

public class AccountApiV1 implements VersionedApi {

    private final FindAccount findAccount;
    private final CreateAccount createAccount;

    @Inject
    public AccountApiV1(
            FindAccount findAccount,
            CreateAccount createAccount
    ) {
        this.findAccount = findAccount;
        this.createAccount = createAccount;
    }

    @Override
    public String version() {
        return "v1";
    }

    @Override
    public RouteGroup routes() {
        return () -> {
            get("/info", info());
            get("/:accountId", findAccount());
            post("", json(), createAccount());
        };
    }

    private Route createAccount() {
        return (req, res) -> {
            res.type(json());

            CreateAccountApiRequestV1 createAccountRequest =
                    gson().fromJson(req.body(), CreateAccountApiRequestV1.class);

            AccountStatus status = createAccount.run(createAccountRequest);

            return new CreateAccountApiResponseV1(
                    createAccountRequest.getAccountId(),
                    status.toString(),
                    createAccountRequest.getTrackId()
            ).toJson();
        };
    }

    private Route findAccount() {
        return (req, res) -> {
            Optional<Account> account = findAccount.run(req.params(":accountId"));

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
