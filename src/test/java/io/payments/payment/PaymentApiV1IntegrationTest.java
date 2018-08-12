package io.payments.payment;

import com.google.gson.reflect.TypeToken;
import io.payments.PaymentsServiceApp;
import io.payments.TestUtils;
import io.payments.account.Account;
import io.payments.account.AccountsRepository;
import io.payments.account.XodusAccountsRepository;
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

    private static final CurrencyUnit PLN = CurrencyUnit.getInstance("PLN");

    private static final Account ACCOUNT_A = createUser("A", 10000);
    private static final Account ACCOUNT_B = createUser("B", 0);
    private static final Account ACCOUNT_C = createUser("C", 10000);
    private static final Account ACCOUNT_D = createUser("D", 0);

    private static final String TEST_DB_NAME = "PaymentApiV1IntegrationTest";

    private static Account createUser(String name, int initialBalance) {
        String userId = String.format("user%s@mail.com", name);
        String accountId = String.format("ACCOUNT_%s", name);

        return new Account(
                null, userId, accountId, Money.of(PLN, initialBalance)
        );
    }

    @BeforeClass
    public static void setUpTest() throws IOException {
        setUpAccounts();

        PaymentsServiceApp.main(new String[] {"port=" + TEST_PORT, "dbName=" + TEST_DB_NAME});
        RestAssured.baseURI = BASE_HOST;
        RestAssured.port = TEST_PORT;
        Spark.awaitInitialization();
    }

    private static void setUpAccounts() throws IOException {
        try (AccountsRepository accountsRepository = new XodusAccountsRepository(TEST_DB_NAME)) {
            accountsRepository.save(ACCOUNT_A);
            accountsRepository.save(ACCOUNT_B);
            accountsRepository.save(ACCOUNT_C);
            accountsRepository.save(ACCOUNT_D);
        }
    }

    @AfterClass
    public static void tearDownTest() throws IOException {
        Spark.stop();
        TestUtils.deleteDirectoryStream(Paths.get("./" + TEST_DB_NAME + "_Payments"));
        TestUtils.deleteDirectoryStream(Paths.get("./" + TEST_DB_NAME + "_Accounts"));
    }

    @Test
    public void returns_status_COMPLETED_for_payment_of_1000_PLN_from_account_A_to_account_B() {
        String trackId = UUID.randomUUID().toString();
        ApiRequest paymentRequest = paymentRequestOf1000PLN(trackId, ACCOUNT_A, ACCOUNT_B);

        // @formatter:off
        ExecutePaymentApiResponseV1 paymentResponse =
                given().
                    accept(ContentType.JSON).
                    contentType(ContentType.JSON).
                    body(paymentRequest.toJson()).
                when().
                    post("/api/v1/payments").
                then().
                    statusCode(200).
                extract().body().as(ExecutePaymentApiResponseV1.class);
        // @formatter:on

        assertThat(paymentResponse.getStatus()).isEqualTo(PaymentStatus.COMPLETED.toString());
        assertThat(paymentResponse.getUserId()).isEqualTo(ACCOUNT_A.getUserId());
        assertThat(paymentResponse.getTrackId()).isEqualTo(trackId);
    }

    @Test
    public void registers_all_received_payments() {
        String trackId = UUID.randomUUID().toString();
        ExecutePaymentApiRequestV1 paymentRequest = paymentRequestOf1000PLN(trackId, ACCOUNT_A, ACCOUNT_B);

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

    @Test
    public void makes_money_transfer_for_payment_of_1000_PLN_from_account_C_with_initial_balance_10000_to_account_D_with_initial_balance_0() {
        String trackId = UUID.randomUUID().toString();
        ExecutePaymentApiRequestV1 paymentRequest = paymentRequestOf1000PLN(trackId, ACCOUNT_C, ACCOUNT_D);

        // @formatter:off
        given().
            accept(ContentType.JSON).
            contentType(ContentType.JSON).
            body(paymentRequest.toJson()).
        when().
            post("/api/v1/payments").
        then().
            statusCode(200);
        // @formatter:on

        checkAccount(ACCOUNT_C.getAccountId(), ACCOUNT_C.getUserId(), Money.of(PLN, 9000));
        checkAccount(ACCOUNT_D.getAccountId(), ACCOUNT_D.getUserId(), Money.of(PLN, 1000));
    }

    private void checkAccount(String accountId, String userId, Money finalBalance) {
        // @formatter:off
        String accountAsJson =
            given().
                accept(ContentType.JSON).
            when().
                get("/api/v1/accounts/" + accountId).
            then().
                statusCode(200).
            extract().body().asString();
        // @formatter:on

        Account account = gson().fromJson(accountAsJson, Account.class);

        assertThat(account.getUserId()).isEqualTo(userId);
        assertThat(account.getAccountId()).isEqualTo(accountId);
        assertThat(account.getBalance()).isEqualTo(finalBalance);
    }

    private ExecutePaymentApiRequestV1 paymentRequestOf1000PLN(String trackId, Account from, Account to) {
        return new ExecutePaymentApiRequestV1(
                from.getUserId(),
                from.getAccountId(),
                to.getAccountId(),
                Money.of(PLN, BigDecimal.valueOf(1000.0)),
                trackId
        );
    }
}
