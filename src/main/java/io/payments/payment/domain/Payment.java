package io.payments.payment.domain;

import jetbrains.exodus.entitystore.Entity;
import lombok.*;
import org.joda.money.Money;

import java.time.LocalDateTime;

import static io.payments.api.Common.gson;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    public static final String NOT_SAVED = null;

    private String id;
    private String userId;
    private String accountFrom;
    private String accountTo;
    private Money amount;
    private String trackId;
    private LocalDateTime receivedAtUTC;
    private PaymentStatus paymentStatus;

    public static Payment from(Entity entity) {
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
                receivedAtUTC,
                PaymentStatus.valueOf((String) entity.getProperty(Constants.paymentsStatus))
        );
    }

    public static class Constants {
        private Constants() {
            // constants
        }

        public static final String ENTITY_TYPE = "Payments";
        public static final String userId = "userId";
        public static final String accountFrom = "accountFrom";
        public static final String accountTo = "accountTo";
        public static final String amount = "amount";
        public static final String trackId = "trackId";
        public static final String receivedAtUTC = "receivedAtUTC";
        public static final String paymentsStatus = "paymentsStatus";
    }
}
