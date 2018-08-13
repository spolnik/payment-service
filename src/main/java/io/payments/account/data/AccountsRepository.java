package io.payments.account.data;

import io.payments.account.domain.Account;
import io.payments.account.domain.AccountStatus;
import io.payments.api.Function;
import io.payments.payment.domain.Payment;
import io.payments.payment.domain.PaymentStatus;
import jetbrains.exodus.entitystore.Entity;

import java.io.Closeable;
import java.util.List;
import java.util.Optional;

public interface AccountsRepository extends Closeable {
    AccountStatus save(Account account);
    Optional<Account> findById(String accountId);

    PaymentStatus executePayment(
            Payment payment,
            Function<Entity, Entity, PaymentStatus> isValid
    );

    List<Account> findAll();
}
