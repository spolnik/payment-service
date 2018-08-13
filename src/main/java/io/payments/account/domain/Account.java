package io.payments.account.domain;

import jetbrains.exodus.entitystore.Entity;
import lombok.*;
import org.joda.money.Money;

import static io.payments.api.Common.gson;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    public static final String NOT_SAVED = null;

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

    public static class Constants {
        private Constants() {
            // constants
        }

        public static final String ENTITY_TYPE = "Accounts";
        public static final String userId = "userId";
        public static final String accountId = "accountId";
        public static final String balance = "balance";
    }
}
