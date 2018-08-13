package io.payments.account;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.payments.api.Function;
import io.payments.payment.Payment;
import io.payments.payment.PaymentStatus;
import jetbrains.exodus.ExodusException;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.PersistentEntityStore;
import jetbrains.exodus.entitystore.PersistentEntityStores;
import lombok.extern.slf4j.Slf4j;
import org.joda.money.Money;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.payments.api.Common.gson;
import static java.util.stream.StreamSupport.stream;

@Slf4j
public class XodusAccountsRepository implements AccountsRepository {

    private final PersistentEntityStore store;

    @Inject
    public XodusAccountsRepository(@Named("dbName") String dbName) {
        store = PersistentEntityStores.newInstance("./" + dbName + "_Accounts");
    }

    @Override
    public AccountStatus save(Account account) {
        return store.computeInTransaction(txn -> {
            Entity alreadyExist = txn.find(
                    Account.Constants.ENTITY_TYPE,
                    Account.Constants.accountId,
                    account.getAccountId()
            ).getFirst();

            if (alreadyExist != null) {
                return AccountStatus.ALREADY_EXIST;
            }

            Entity entity = txn.newEntity(Account.Constants.ENTITY_TYPE);
            entity.setProperty(Account.Constants.userId, account.getUserId());
            entity.setProperty(Account.Constants.accountId, account.getAccountId());
            setNewBalance(entity, account.getBalance());

            return AccountStatus.CREATED;
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
            Function<Entity, Entity, PaymentStatus> isValid
    ) {
        return store.computeInTransaction(txn -> {
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

            PaymentStatus validationStatus = isValid.apply(accountFromEntity, accountToEntity);

            if (validationStatus != PaymentStatus.VALID) {
                return validationStatus;
            }

            return transferMoney(accountFromEntity, accountToEntity, payment.getAmount());
        });
    }

    @Override
    public List<Account> findAll() {
        return store.computeInReadonlyTransaction(txn ->
                stream(txn.getAll(Account.Constants.ENTITY_TYPE).spliterator(), false)
                        .map(Account::from)
                        .collect(Collectors.toList())
        );
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
            log.error("Could not close payment store: {}", e.getMessage(), e);
        }
    }
}
