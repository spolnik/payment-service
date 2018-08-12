package io.payments.payment;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.ExodusException;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.PersistentEntityStore;
import jetbrains.exodus.entitystore.PersistentEntityStores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static io.payments.api.Common.gson;
import static java.util.stream.StreamSupport.stream;

public class XodusPaymentsRepository implements PaymentsRepository {

    private static final Logger LOG = LoggerFactory.getLogger(XodusPaymentsRepository.class);

    private final PersistentEntityStore store;

    @Inject
    public XodusPaymentsRepository(@Named("dbName") String dbName) {
        store = PersistentEntityStores.newInstance("./" + dbName + "_Payments");
    }

    @Override
    public List<Payment> findAll() {
        return store.computeInReadonlyTransaction(txn ->
                stream(txn.getAll(Payment.Constants.ENTITY_TYPE).spliterator(), false)
                        .map(Payment::from)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Payment save(Payment payment) {
        return store.computeInExclusiveTransaction(txn -> {
            Entity entity = txn.newEntity(Payment.Constants.ENTITY_TYPE);
            entity.setProperty(Payment.Constants.userId, payment.getUserId());
            entity.setProperty(Payment.Constants.accountFrom, payment.getAccountFrom());
            entity.setProperty(Payment.Constants.accountTo, payment.getAccountTo());
            entity.setProperty(Payment.Constants.amount, gson().toJson(payment.getAmount()));
            entity.setProperty(Payment.Constants.trackId, payment.getTrackId());
            entity.setProperty(Payment.Constants.receivedAtUTC, gson().toJson(payment.getReceivedAtUTC()));

            return Payment.from(entity);
        });
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
