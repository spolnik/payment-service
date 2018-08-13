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
    public PaymentStatus run(PaymentApiRequestV1 paymentsRequest) {
        Payment payment = store(paymentsRequest.toPayment());

        PaymentStatus paymentStatus = accountsRepository.executePayment(
                payment,
                isValid(payment)
        );

        paymentsRepository.updatePaymentStatus(payment.getId(), paymentStatus);

        return paymentStatus;
    }

    private Function<Entity, Entity, PaymentStatus> isValid(Payment payment) {
        return (Entity accountFromEntity, Entity accountToEntity) -> {
            if (accountFromEntity == null) {
                return PaymentStatus.ACCOUNT_FROM_NOT_FOUND;
            }

            if (accountToEntity == null) {
                return PaymentStatus.ACCOUNT_TO_NOT_FOUND;
            }

            Account accountFrom = Account.from(accountFromEntity);

            if (invalidUser(accountFrom.getUserId(), payment.getUserId())) {
                return PaymentStatus.USER_ID_AND_ACCOUNT_TO_DO_NOT_MATCH;
            }

            if (!hasMoneyInRequiredCurrency(payment, accountFrom)) {
                return PaymentStatus.INVALID_CURRENCY;
            }

            return hasEnoughMoney(payment, accountFrom);
        };
    }

    private PaymentStatus hasEnoughMoney(Payment payment, Account accountFrom) {
        return (accountFrom.getBalance().isGreaterThan(payment.getAmount()) ||
                accountFrom.getBalance().isEqual(payment.getAmount()))
                ? PaymentStatus.VALID
                : PaymentStatus.NOT_ENOUGH_MONEY;
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
