package io.payments.payment;

import io.payments.PaymentsServiceApp;
import io.payments.TestUtils;
import io.payments.api.ApiRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import spark.Spark;

import java.math.BigDecimal;
import java.util.UUID;

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

    @BeforeClass
    public static void setUp() {
        PaymentsServiceApp.main(new String[] {"port=" + TEST_PORT});
        RestAssured.baseURI = BASE_HOST;
        RestAssured.port = TEST_PORT;
        Spark.awaitInitialization();
    }

    @AfterClass
    public static void tearDown() {
        Spark.stop();
    }

    @Test
    public void send_1000_PLN_from_account_A_to_account_B() {
        String trackId = UUID.randomUUID().toString();

        ApiRequest apiRequest = new PaymentRequestV1(
                USER_ID,
                ACCOUNT_FROM,
                ACCOUNT_TO,
                Money.of(PLN, BigDecimal.valueOf(1000.0)),
                trackId
        );

        ExtractableResponse<Response> response = given().
                accept(ContentType.JSON).
                contentType(ContentType.JSON).
                body(apiRequest.toJson()).
                when().
                post("/api/v1/payments").
                then().
                statusCode(200).
                extract();

        PaymentResponseV1 paymentResponse = response.body().as(PaymentResponseV1.class);

        assertThat(paymentResponse.getStatus()).isEqualTo(PaymentStatus.COMPLETED.toString());
        assertThat(paymentResponse.getUserId()).isEqualTo(USER_ID);
        assertThat(paymentResponse.getTrackId()).isEqualTo(trackId);
    }
}
