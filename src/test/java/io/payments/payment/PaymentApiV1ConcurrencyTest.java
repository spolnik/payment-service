package io.payments.payment;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;
import com.google.gson.reflect.TypeToken;
import io.payments.PaymentsServiceApp;
import io.payments.TestUtils;
import io.payments.account.Account;
import io.payments.account.AccountsRepository;
import io.payments.account.XodusAccountsRepository;
import io.payments.api.ApiRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.joda.money.Money;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import spark.Spark;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static io.payments.TestUtils.PLN;
import static io.payments.TestUtils.createAccount;
import static io.payments.api.Common.gson;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ConcurrentTestRunner.class)
public class PaymentApiV1ConcurrencyTest {

    private static final int TEST_PORT =
            TestUtils.findRandomOpenPortOnAllLocalInterfaces();
    private static final String BASE_HOST = "http://localhost";

    private static final Account ACCOUNT_A = createAccount("A", 10000);
    private static final Account ACCOUNT_B = createAccount("B", 0);

    private static final String TEST_DB_NAME = "PaymentApiV1ConcurrencyTest";

    private static final int THREAD_COUNT = 10;
    private static final double ALL_PAYMENTS = THREAD_COUNT * (100.0 + 55.5 + 101.12);

    @BeforeClass
    public static void setUpTest() throws IOException {
        setUpAccounts();

        PaymentsServiceApp.main(new String[]{"port=" + TEST_PORT, "dbName=" + TEST_DB_NAME});
        RestAssured.baseURI = BASE_HOST;
        RestAssured.port = TEST_PORT;
        Spark.awaitInitialization();
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
    @ThreadCount(THREAD_COUNT)
    public void execute_payments_from_account_A_to_account_B_in_four_threads() {
        String trackId = UUID.randomUUID().toString();
        ApiRequest paymentRequest = paymentRequest(trackId, 100.0);

        executePayment(trackId, paymentRequest);

        trackId = UUID.randomUUID().toString();
        paymentRequest = paymentRequest(trackId, 55.5);

        executePayment(trackId, paymentRequest);

        trackId = UUID.randomUUID().toString();
        paymentRequest = paymentRequest(trackId, 101.12);

        executePayment(trackId, paymentRequest);
    }

    @After
    public void verify_state() {
        checkAccount(ACCOUNT_A, ACCOUNT_A.getBalance().minus(ALL_PAYMENTS));
        checkAccount(ACCOUNT_B, ACCOUNT_B.getBalance().plus(ALL_PAYMENTS));
        checkPayments();
    }

    private void checkPayments() {
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

        assertThat(payments.size()).isEqualTo(THREAD_COUNT * 3);
        payments.forEach(
                payment -> assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED)
        );
    }

    private void checkAccount(Account accountToCheck, Money finalBalance) {
        // @formatter:off
        String accountAsJson =
            given().
                accept(ContentType.JSON).
            when().
                get("/api/v1/accounts/" + accountToCheck.getAccountId()).
            then().
                statusCode(200).
            extract().body().asString();
        // @formatter:on

        Account account = gson().fromJson(accountAsJson, Account.class);

        assertThat(account.getUserId()).isEqualTo(accountToCheck.getUserId());
        assertThat(account.getAccountId()).isEqualTo(accountToCheck.getAccountId());
        assertThat(account.getBalance()).isEqualTo(finalBalance);
    }

    private void executePayment(String trackId, ApiRequest paymentRequest) {
        // @formatter:off
        PaymentApiResponseV1 paymentResponse =
            given().
                accept(ContentType.JSON).
                contentType(ContentType.JSON).
                body(paymentRequest.toJson()).
            when().
                post("/api/v1/payments").
            then().
                statusCode(200).
            extract().body().as(PaymentApiResponseV1.class);
        // @formatter:on

        assertThat(paymentResponse.getStatus()).isEqualTo(PaymentStatus.COMPLETED.toString());
        assertThat(paymentResponse.getUserId()).isEqualTo(ACCOUNT_A.getUserId());
        assertThat(paymentResponse.getTrackId()).isEqualTo(trackId);
    }

    private static PaymentApiRequestV1 paymentRequest(String trackId, double amount) {
        return new PaymentApiRequestV1(
                ACCOUNT_A.getUserId(),
                ACCOUNT_A.getAccountId(),
                ACCOUNT_B.getAccountId(),
                Money.of(PLN, BigDecimal.valueOf(amount)),
                trackId
        );
    }
}
