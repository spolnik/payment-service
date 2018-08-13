package io.payments.payment;

import io.payments.payment.api.PaymentApiRequestV1;
import io.payments.payment.api.PaymentApiResponseV1;
import io.payments.payment.domain.Payment;
import io.payments.payment.domain.PaymentStatus;
import org.joda.money.Money;
import org.junit.Test;

import java.time.Clock;
import java.time.LocalDateTime;

import static io.payments.TestUtils.PLN;
import static io.payments.api.Common.gson;
import static org.assertj.core.api.Assertions.assertThat;

public class PaymentSerializationTest {

    private static final Money ACCOUNT_BALANCE = Money.of(PLN, 1000.0);
    private static final String ACCOUNT_TO = "ACCOUNT_TO";
    private static final String ACCOUNT_FROM = "ACCOUNT_FROM";
    private static final String USER_ID = "USER_ID";
    private static final String TRACK_ID = "TRACK_ID";

    private final Payment payment = new Payment(
            "ID", USER_ID, ACCOUNT_FROM,
            ACCOUNT_TO, ACCOUNT_BALANCE, TRACK_ID, LocalDateTime.now(Clock.systemUTC()),
            PaymentStatus.COMPLETED
    );

    private final PaymentApiRequestV1 request = new PaymentApiRequestV1(
            USER_ID, ACCOUNT_FROM, ACCOUNT_TO, ACCOUNT_BALANCE, TRACK_ID
    );

    private final PaymentApiResponseV1 response = new PaymentApiResponseV1(
            USER_ID, PaymentStatus.COMPLETED.toString(), TRACK_ID
    );

    @Test
    public void serializes_payment_to_json_and_back() {
        String paymentAsJson = gson().toJson(payment);
        Payment paymentFromJson = gson().fromJson(paymentAsJson, Payment.class);

        assertThat(paymentFromJson).isEqualTo(payment);
    }

    @Test
    public void serializes_payment_request_to_json_and_back() {
        String requestAsJson = gson().toJson(request);
        PaymentApiRequestV1 requestFromJson =
                gson().fromJson(requestAsJson, PaymentApiRequestV1.class);

        assertThat(requestFromJson).isEqualTo(request);
    }

    @Test
    public void serializes_payment_response_to_json_and_back() {
        String responseAsJson = gson().toJson(response);
        PaymentApiResponseV1 responseFromJson =
                gson().fromJson(responseAsJson, PaymentApiResponseV1.class);

        assertThat(responseFromJson).isEqualTo(response);
    }
}
