package io.payments.payment;

import com.google.gson.reflect.TypeToken;
import io.payments.PaymentsServiceApp;
import io.payments.TestUtils;
import io.payments.account.domain.Account;
import io.payments.account.data.AccountsRepository;
import io.payments.account.data.XodusAccountsRepository;
import io.payments.payment.data.PaymentsRepository;
import io.payments.payment.data.XodusPaymentsRepository;
import io.payments.payment.domain.Payment;
import io.payments.payment.domain.PaymentStatus;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import junitparams.JUnitParamsRunner;
import org.joda.money.Money;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import spark.Spark;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static io.payments.TestUtils.PLN;
import static io.payments.TestUtils.createAccount;
import static io.payments.api.Common.gson;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class PaymentApiV1RegisterPaymentsIntegrationTest {

    private static final int TEST_PORT =
            TestUtils.findRandomOpenPortOnAllLocalInterfaces();
    private static final String BASE_HOST = "http://localhost";

    private static final Account ACCOUNT_A = createAccount("A", 10000);
    private static final Account ACCOUNT_B = createAccount("B", 0);

    private static final Payment PAYMENT = new Payment(
            null,
            ACCOUNT_A.getUserId(),
            ACCOUNT_A.getAccountId(),
            ACCOUNT_B.getAccountId(),
            Money.of(PLN, 1000.0),
            "TRACK_ID",
            LocalDateTime.now(Clock.systemUTC()),
            PaymentStatus.COMPLETED
    );

    private static final String TEST_DB_NAME = "PaymentApiV1RegisterPaymentsIntegrationTest";

    @BeforeClass
    public static void setUpTest() throws IOException {
        setUpAccounts();
        setUpPayments();

        PaymentsServiceApp.main(new String[]{"port=" + TEST_PORT, "dbName=" + TEST_DB_NAME});
        RestAssured.baseURI = BASE_HOST;
        RestAssured.port = TEST_PORT;
        Spark.awaitInitialization();
    }

    private static void setUpPayments() throws IOException {
        try (PaymentsRepository paymentsRepository = new XodusPaymentsRepository(TEST_DB_NAME)) {
            paymentsRepository.save(PAYMENT);
        }
    }

    private static void setUpAccounts() throws IOException {
        try (AccountsRepository accountsRepository = new XodusAccountsRepository(TEST_DB_NAME)) {
            accountsRepository.save(ACCOUNT_A);
            accountsRepository.save(ACCOUNT_B);
        }
    }

    @AfterClass
    public static void tearDownTest() throws IOException {
        Spark.stop();
        TestUtils.deleteDirectoryStream(Paths.get("./" + TEST_DB_NAME + "_Payments"));
        TestUtils.deleteDirectoryStream(Paths.get("./" + TEST_DB_NAME + "_Accounts"));
    }

    @Test
    public void registers_all_received_payments() {
        // @formatter:off
        String paymentsAsJson =
            given().
                accept(ContentType.JSON).
            when().
                get("/api/v1/payments").
            then().
                statusCode(200).
            extract().body().asString();

        List<Payment> payments = gson().fromJson(
                paymentsAsJson, new TypeToken<List<Payment>>() {}.getType()
        );
        // @formatter:on

        assertThat(payments.size()).isEqualTo(1);

        Payment payment = payments.get(0);
        assertThat(payment).isEqualToIgnoringGivenFields(PAYMENT, "id");
    }
}
