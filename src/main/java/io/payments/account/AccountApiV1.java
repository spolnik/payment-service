package io.payments.account;

import io.payments.api.VersionedApi;
import io.payments.payment.ExecutePayment;
import spark.Route;
import spark.RouteGroup;

import javax.inject.Inject;

import java.util.Optional;

import static io.payments.api.Common.gson;
import static io.payments.api.Common.json;
import static spark.Spark.get;

public class AccountApiV1 implements VersionedApi {
    private final FindAccount findAccount;

    @Inject
    public AccountApiV1(FindAccount findAccount) {
        this.findAccount = findAccount;
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
