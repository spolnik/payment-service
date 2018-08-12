package io.payments.payment;

import io.payments.account.Account;
import io.payments.account.AccountsRepository;
import io.payments.api.Function;
import jetbrains.exodus.entitystore.Entity;

import javax.inject.Inject;
import java.util.Objects;

public class ExecutePaymentInternally implements ExecutePayment {

    private final PaymentsRepository paymentsRepository;
    private final AccountsRepository accountsRepository;

    @Inject
    public ExecutePaymentInternally(
            PaymentsRepository paymentsRepository,
            AccountsRepository accountsRepository
    ) {
        this.paymentsRepository = paymentsRepository;
        this.accountsRepository = accountsRepository;
    }

    @Override
    public PaymentStatus run(ExecutePaymentApiRequestV1 paymentsRequest) {
        Payment payment = store(paymentsRequest.toPayment());

        return accountsRepository.executePayment(
                payment,
                isValid(payment)
        );
    }

    private Function<Entity, Entity, Boolean> isValid(Payment payment) {
        return (Entity accountFromEntity, Entity accountToEntity) -> {
            if (accountFromEntity == null || accountToEntity == null) {
                return false;
            }

            Account accountFrom = Account.from(accountFromEntity);

            if (invalidUser(accountFrom.getUserId(), payment.getUserId())) {
                return false;
            }

            if (!hasMoneyInRequiredCurrency(payment, accountFrom)) {
                return false;
            }

            return hasEnoughMoney(payment, accountFrom);
        };
    }

    private boolean hasEnoughMoney(Payment payment, Account accountFrom) {
        return accountFrom.getBalance().getAmount().compareTo(
                payment.getAmount().getAmount()
        ) >= 0;
    }

    private boolean hasMoneyInRequiredCurrency(Payment payment, Account accountFrom) {
        return accountFrom.getBalance().getCurrencyUnit().equals(
                payment.getAmount().getCurrencyUnit()
        );
    }

    private boolean invalidUser(String userId, String paymentUserId) {
        return !Objects.equals(userId, paymentUserId);
    }

    private Payment store(Payment payment) {
        return paymentsRepository.save(payment);
    }
}
