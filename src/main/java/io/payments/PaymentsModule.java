package io.payments;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import io.payments.account.api.AccountApiV1;
import io.payments.account.command.CreateAccount;
import io.payments.account.command.CreateAccountInternally;
import io.payments.account.data.AccountsRepository;
import io.payments.account.data.XodusAccountsRepository;
import io.payments.account.query.FindAccountById;
import io.payments.account.query.FindAccountByIdInternally;
import io.payments.account.query.FindAllAccounts;
import io.payments.account.query.FindAllAccountsInternally;
import io.payments.api.PaymentsRouter;
import io.payments.api.Router;
import io.payments.api.VersionedApi;
import io.payments.payment.api.PaymentApiV1;
import io.payments.payment.command.ExecutePayment;
import io.payments.payment.command.ExecutePaymentInternally;
import io.payments.payment.data.PaymentsRepository;
import io.payments.payment.data.XodusPaymentsRepository;
import io.payments.payment.query.FindAllPayments;
import io.payments.payment.query.FindAllPaymentsInternally;

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

        bind(FindAccountById.class).to(FindAccountByIdInternally.class).asEagerSingleton();
        bind(CreateAccount.class).to(CreateAccountInternally.class).asEagerSingleton();
        bind(FindAllAccounts.class).to(FindAllAccountsInternally.class).asEagerSingleton();

        bind(Router.class).to(PaymentsRouter.class).asEagerSingleton();

        Multibinder<VersionedApi> mb = Multibinder.newSetBinder(binder(), VersionedApi.class);
        mb.addBinding().to(PaymentApiV1.class);
        mb.addBinding().to(AccountApiV1.class);
    }
}
