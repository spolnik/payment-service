package io.payments;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import io.payments.account.RestAccountApiV1;
import io.payments.api.PaymentsRouter;
import io.payments.api.Router;
import io.payments.api.VersionedApi;
import io.payments.payment.RestPaymentApiV1;
import io.payments.user.RestUserApiV1;

import java.util.Arrays;
import java.util.List;

public class PaymentsModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Router.class).to(PaymentsRouter.class).asEagerSingleton();
    }

    @Provides
    @Inject
    public List<VersionedApi> api() {
        return Arrays.asList(
                new RestAccountApiV1(),
                new RestPaymentApiV1(),
                new RestUserApiV1()
        );
    }
}
