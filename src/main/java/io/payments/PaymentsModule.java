package io.payments;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import io.payments.account.*;
import io.payments.api.PaymentsRouter;
import io.payments.api.Router;
import io.payments.api.VersionedApi;
import io.payments.payment.*;

public class PaymentsModule extends AbstractModule {
    private final String dbName;

    PaymentsModule(String dbName) {
        this.dbName = dbName;
    }

    @Override
    protected void configure() {
        bindConstant().annotatedWith(Names.named("dbName")).to(dbName);

        bind(PaymentsRepository.class).to(XodusPaymentsRepository.class).asEagerSingleton();
        bind(AccountsRepository.class).to(XodusAccountsRepository.class).asEagerSingleton();

        bind(ExecutePayment.class).to(ExecutePaymentInternally.class).asEagerSingleton();
        bind(FindAllPayments.class).to(FindAllPaymentsInternally.class).asEagerSingleton();

        bind(FindAccount.class).to(FindAccountInternally.class).asEagerSingleton();
        bind(CreateAccount.class).to(CreateAccountInternally.class).asEagerSingleton();

        bind(Router.class).to(PaymentsRouter.class).asEagerSingleton();

        Multibinder<VersionedApi> mb = Multibinder.newSetBinder(binder(), VersionedApi.class);
        mb.addBinding().to(PaymentApiV1.class);
        mb.addBinding().to(AccountApiV1.class);
    }
}
