package io.payments.payment;

import com.google.gson.reflect.TypeToken;
import io.payments.PaymentsServiceApp;
import io.payments.TestUtils;
import io.payments.api.ApiRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import spark.Spark;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static io.payments.api.Common.gson;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class PaymentApiV1IntegrationTest {

    private static final int TEST_PORT =
            TestUtils.findRandomOpenPortOnAllLocalInterfaces();
    private static final String BASE_HOST = "http://localhost";

    private static final String ACCOUNT_FROM = "A";
    private static final String ACCOUNT_TO = "B";

    private static final String USER_ID = "user1k@mail.com";
    private static final CurrencyUnit PLN = CurrencyUnit.getInstance("PLN");

    private static final String TEST_DB_NAME = "PaymentApiV1IntegrationTest";

    @BeforeClass
    public static void setUp() {
        PaymentsServiceApp.main(new String[] {"port=" + TEST_PORT, "dbName=" + TEST_DB_NAME});
        RestAssured.baseURI = BASE_HOST;
        RestAssured.port = TEST_PORT;
        Spark.awaitInitialization();
    }

    @AfterClass
    public static void tearDown() throws IOException {
        Spark.stop();
        TestUtils.deleteDirectoryStream(Paths.get("./" + TEST_DB_NAME));
    }

    @Test
    public void returns_status_COMPLETED_for_payment_of_1000_PLN_from_account_A_to_account_B() {
        String trackId = UUID.randomUUID().toString();
        ApiRequest paymentRequest = paymentRequest(trackId);

        // @formatter:off
        PaymentResponseV1 paymentResponse =
                given().
                    accept(ContentType.JSON).
                    contentType(ContentType.JSON).
                    body(paymentRequest.toJson()).
                when().
                    post("/api/v1/payments").
                then().
                    statusCode(200).
                extract().body().as(PaymentResponseV1.class);
        // @formatter:on

        assertThat(paymentResponse.getStatus()).isEqualTo(PaymentStatus.COMPLETED.toString());
        assertThat(paymentResponse.getUserId()).isEqualTo(USER_ID);
        assertThat(paymentResponse.getTrackId()).isEqualTo(trackId);
    }

    @Test
    public void registers_all_received_payments() {
        String trackId = UUID.randomUUID().toString();
        PaymentRequestV1 paymentRequest = paymentRequest(trackId);

        // @formatter:off
        given().
            accept(ContentType.JSON).
            contentType(ContentType.JSON).
            body(paymentRequest.toJson()).
        when().
            post("/api/v1/payments").
        then().
            statusCode(200);

        String paymentsAsJson =
                given().
                    accept(ContentType.JSON).
                when().
                    get("/api/v1/payments").
                then().
                    statusCode(200).
                extract().body().asString();
        // @formatter:on

        List<Payment> payments = gson().fromJson(paymentsAsJson, new TypeToken<List<Payment>>() {}.getType());

        assertThat(payments).hasSize(1);

        Payment payment = payments.get(0);
        Payment expected = paymentRequest.toPayment();
        expected.setId("0-0");
        assertThat(payment).isEqualToIgnoringGivenFields(expected, "receivedAtUTC");
    }

    private PaymentRequestV1 paymentRequest(String trackId) {
        return new PaymentRequestV1(
                USER_ID,
                ACCOUNT_FROM,
                ACCOUNT_TO,
                Money.of(PLN, BigDecimal.valueOf(1000.0)),
                trackId
        );
    }
}
