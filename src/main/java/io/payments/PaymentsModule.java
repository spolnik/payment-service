package io.payments;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import io.payments.account.AccountApiV1;
import io.payments.api.PaymentsRouter;
import io.payments.api.Router;
import io.payments.api.VersionedApi;
import io.payments.payment.ExecutePayment;
import io.payments.payment.ExecutePaymentInternally;
import io.payments.payment.PaymentApiV1;

public class PaymentsModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Router.class).to(PaymentsRouter.class).asEagerSingleton();
        bind(ExecutePayment.class).to(ExecutePaymentInternally.class).asEagerSingleton();

        Multibinder<VersionedApi> mb = Multibinder.newSetBinder(binder(), VersionedApi.class);
        mb.addBinding().to(PaymentApiV1.class);
        mb.addBinding().to(AccountApiV1.class);
    }
}
