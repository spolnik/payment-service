package io.payments.account;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.payments.api.Function;
import io.payments.payment.Payment;
import io.payments.payment.PaymentStatus;
import io.payments.payment.XodusPaymentsRepository;
import jetbrains.exodus.ExodusException;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.PersistentEntityStore;
import jetbrains.exodus.entitystore.PersistentEntityStores;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static io.payments.api.Common.gson;

public class XodusAccountsRepository implements AccountsRepository {

    private static final Logger LOG = LoggerFactory.getLogger(XodusPaymentsRepository.class);

    private final PersistentEntityStore store;

    @Inject
    public XodusAccountsRepository(@Named("dbName") String dbName) {
        store = PersistentEntityStores.newInstance("./" + dbName + "_Accounts");
    }

    @Override
    public Account save(Account account) {
        return store.computeInExclusiveTransaction(txn -> {
            Entity entity = txn.newEntity(Account.Constants.ENTITY_TYPE);
            entity.setProperty(Account.Constants.userId, account.getUserId());
            entity.setProperty(Account.Constants.accountId, account.getAccountId());
            setNewBalance(entity, account.getBalance());

            return Account.from(entity);
        });
    }

    @Override
    public Optional<Account> findById(String accountId) {
        return store.computeInReadonlyTransaction(txn -> {
            Entity accountEntity = txn.find(
                    Account.Constants.ENTITY_TYPE,
                    Account.Constants.accountId,
                    accountId
            ).getFirst();

            return accountEntity != null ? Optional.of(Account.from(accountEntity)) : Optional.empty();
        });
    }

    @Override
    public PaymentStatus executePayment(
            Payment payment,
            Function<Entity, Entity, Boolean> isValid
    ) {
        return store.computeInExclusiveTransaction(txn -> {
            Entity accountFromEntity = txn.find(
                    Account.Constants.ENTITY_TYPE,
                    Account.Constants.accountId,
                    payment.getAccountFrom()
            ).getFirst();

            Entity accountToEntity = txn.find(
                    Account.Constants.ENTITY_TYPE,
                    Account.Constants.accountId,
                    payment.getAccountTo()
            ).getFirst();

            if (!isValid.apply(accountFromEntity, accountToEntity)) {
                return PaymentStatus.REJECTED;
            }

            return transferMoney(accountFromEntity, accountToEntity, payment.getAmount());
        });
    }

    private PaymentStatus transferMoney(Entity accountFromEntity, Entity accountToEntity, Money amount) {
        Money balanceFrom = getBalance(accountFromEntity);
        Money balanceTo = getBalance(accountToEntity);

        setNewBalance(accountFromEntity, balanceFrom.minus(amount));
        setNewBalance(accountToEntity, balanceTo.plus(amount));

        return PaymentStatus.COMPLETED;
    }

    private void setNewBalance(Entity entity, Money newBalance) {
        entity.setProperty(Account.Constants.balance, gson().toJson(newBalance));
    }

    private Money getBalance(Entity accountEntity) {
        String balanceAsJson = (String) accountEntity.getProperty(Account.Constants.balance);
        return gson().fromJson(balanceAsJson, Money.class);
    }

    @Override
    public void close() {
        try {
            store.close();
        } catch (ExodusException e) {
            LOG.error("Could not close payment store: {}", e.getMessage(), e);
        }
    }
}
