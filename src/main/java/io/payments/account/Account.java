package io.payments.account;

import jetbrains.exodus.entitystore.Entity;
import lombok.*;
import org.joda.money.Money;

import static io.payments.api.Common.gson;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    static final String NOT_SAVED = null;

    private String id;
    private String userId;
    private String accountId;
    private Money balance;

    public static Account from(Entity entity) {
        String balanceAsJson = (String) entity.getProperty(Constants.balance);
        Money balance = gson().fromJson(balanceAsJson, Money.class);

        return new Account(
                entity.getId().toString(),
                (String) entity.getProperty(Account.Constants.userId),
                (String) entity.getProperty(Constants.accountId),
                balance
        );
    }

    static class Constants {
        private Constants() {
            // constants
        }

        static final String ENTITY_TYPE = "Accounts";
        static final String userId = "userId";
        static final String accountId = "accountId";
        static final String balance = "balance";
    }
}
