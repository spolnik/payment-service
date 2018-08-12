package io.payments.account;

import io.payments.api.Function;
import io.payments.payment.Payment;
import io.payments.payment.PaymentStatus;
import jetbrains.exodus.entitystore.Entity;

import java.io.Closeable;
import java.util.Optional;

public interface AccountsRepository extends Closeable {
    AccountStatus save(Account account);
    Optional<Account> findById(String accountId);
    PaymentStatus executePayment(
            Payment payment,
            Function<Entity, Entity, PaymentStatus> isValid
    );
}
