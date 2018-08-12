package io.payments.payment;

import jetbrains.exodus.entitystore.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.money.Money;

import java.time.LocalDateTime;

import static io.payments.api.Common.gson;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    public static String NOT_SAVED = null;

    private String id;
    private String userId;
    private String accountFrom;
    private String accountTo;
    private Money amount;
    private String trackId;
    private LocalDateTime receivedAtUTC;

    static Payment from(Entity entity) {
        String amountAsJson = (String) entity.getProperty(Constants.amount);
        Money amount = gson().fromJson(amountAsJson, Money.class);

        String receivedAtUTCAsJson = (String) entity.getProperty(Constants.receivedAtUTC);
        LocalDateTime receivedAtUTC = gson().fromJson(receivedAtUTCAsJson, LocalDateTime.class);

        return new Payment(
                entity.getId().toString(),
                (String) entity.getProperty(Constants.userId),
                (String) entity.getProperty(Constants.accountFrom),
                (String) entity.getProperty(Constants.accountTo),
                amount,
                (String) entity.getProperty(Constants.trackId),
                receivedAtUTC
        );
    }

    static class Constants {
        static final String ENTITY_TYPE = "Payments";
        static final String id = "id";
        static final String userId = "userId";
        static final String accountFrom = "accountFrom";
        static final String accountTo = "accountTo";
        static final String amount = "amount";
        static final String trackId = "trackId";
        static final String receivedAtUTC = "receivedAtUTC";
    }
}