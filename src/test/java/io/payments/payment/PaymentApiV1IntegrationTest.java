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
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
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

@RunWith(JUnitParamsRunner.class)
public class PaymentApiV1IntegrationTest {

    private static final int TEST_PORT =
            TestUtils.findRandomOpenPortOnAllLocalInterfaces();
    private static final String BASE_HOST = "http://localhost";

    private static final Account ACCOUNT_A = createAccount("A", 10000);
    private static final Account ACCOUNT_B = createAccount("B", 0);
    private static final Account ACCOUNT_C = createAccount("C", 10000);
    private static final Account ACCOUNT_D = createAccount("D", 0);

    private static final String TEST_DB_NAME = "PaymentApiV1IntegrationTest";

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
        executePayment(trackId, paymentRequest, PaymentStatus.COMPLETED, ACCOUNT_A);
    }

    @Test
    public void registers_all_received_payments() {
        String trackId = UUID.randomUUID().toString();
        PaymentApiRequestV1 paymentRequest = paymentRequestOf1000PLN(trackId, ACCOUNT_A, ACCOUNT_B);

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

        List<Payment> payments = gson().fromJson(
                paymentsAsJson, new TypeToken<List<Payment>>() {}.getType()
        );
        // @formatter:on

        assertThat(payments.size()).isGreaterThanOrEqualTo(1);

        Payment payment = payments.get(0);
        assertThat(payment.getId()).isEqualTo("0-0");
    }

    @Test
    public void makes_money_transfer_for_payment_of_1000_PLN_from_account_C_with_initial_balance_10000_to_account_D_with_initial_balance_0() {
        String trackId = UUID.randomUUID().toString();
        PaymentApiRequestV1 paymentRequest = paymentRequestOf1000PLN(trackId, ACCOUNT_C, ACCOUNT_D);

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

    @Test
    public void rejects_money_transfer_when_not_enough_money_on_payer_account() {
        String trackId = UUID.randomUUID().toString();
        ApiRequest paymentRequest = paymentRequestPLN(trackId, ACCOUNT_A, ACCOUNT_B, 1000000);
        executePayment(trackId, paymentRequest, PaymentStatus.NOT_ENOUGH_MONEY, ACCOUNT_A);
    }

    @Test
    public void rejects_money_transfer_when_currency_not_aligned_with_account() {
        String trackId = UUID.randomUUID().toString();
        ApiRequest paymentRequest = paymentRequest(trackId, ACCOUNT_A, ACCOUNT_B, 100, CurrencyUnit.EUR);
        executePayment(trackId, paymentRequest, PaymentStatus.INVALID_CURRENCY, ACCOUNT_A);
    }

    @Test
    public void rejects_money_transfer_when_amount_is_missing() {
        String trackId = UUID.randomUUID().toString();

        ApiRequest paymentRequestWithMissingAmount = new PaymentApiRequestV1(
                ACCOUNT_A.getUserId(),
                ACCOUNT_A.getAccountId(),
                ACCOUNT_B.getAccountId(),
                null,
                trackId
        );

        executePayment(trackId, paymentRequestWithMissingAmount, PaymentStatus.AMOUNT_MISSING, ACCOUNT_A);
    }

    @Test
    public void rejects_money_transfer_when_account_to_is_empty() {
        String trackId = UUID.randomUUID().toString();
        ApiRequest paymentRequest = paymentRequestOf1000PLN(trackId, ACCOUNT_A, new Account());
        executePayment(trackId, paymentRequest, PaymentStatus.ACCOUNT_TO_MISSING, ACCOUNT_A);
    }

    @Test
    public void rejects_money_transfer_when_account_to_does_not_exist() {
        String trackId = UUID.randomUUID().toString();
        Account accountTo = new Account();
        accountTo.setAccountId("does_not_exist");
        ApiRequest paymentRequest = paymentRequestOf1000PLN(trackId, ACCOUNT_A, accountTo);
        executePayment(trackId, paymentRequest, PaymentStatus.ACCOUNT_TO_NOT_FOUND, ACCOUNT_A);
    }

    @Test
    @Parameters(method = "invalidPaymentRequests")
    public void rejects_money_transfer_when_invalid_payment_request(
            String userId, String accountFrom, Money balance, PaymentStatus expectedStatus
    ) {
        String trackId = UUID.randomUUID().toString();
        Account account = new Account(
                null, userId, accountFrom, balance
        );

        ApiRequest paymentRequest = paymentRequestOf1000PLN(
                trackId, account, ACCOUNT_B
        );

        executePayment(trackId, paymentRequest, expectedStatus, account);
    }

    @SuppressWarnings("unused")
    private Object[] invalidPaymentRequests() {
        return new Object[] {
                new Object[] {ACCOUNT_A.getUserId(), null, ACCOUNT_A.getBalance(), PaymentStatus.ACCOUNT_FROM_MISSING},
                new Object[] {null, ACCOUNT_A.getAccountId(), ACCOUNT_A.getBalance(), PaymentStatus.USER_ID_MISSING},
                new Object[] {"CHANGED", ACCOUNT_A.getAccountId(), ACCOUNT_A.getBalance(), PaymentStatus.USER_ID_AND_ACCOUNT_TO_DO_NOT_MATCH},
                new Object[] {ACCOUNT_A.getUserId(), "do_not_exist", ACCOUNT_A.getBalance(), PaymentStatus.ACCOUNT_FROM_NOT_FOUND},
        };
    }

    private void executePayment(
            String trackId, ApiRequest paymentRequest, PaymentStatus status, Account payerAccount
    ) {
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

        assertThat(paymentResponse.getStatus()).isEqualTo(status.toString());
        assertThat(paymentResponse.getUserId()).isEqualTo(payerAccount.getUserId());
        assertThat(paymentResponse.getTrackId()).isEqualTo(trackId);
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

    private static PaymentApiRequestV1 paymentRequestOf1000PLN(
            String trackId, Account from, Account to
    ) {
        return paymentRequestPLN(trackId, from, to, 1000.0);
    }

    private static PaymentApiRequestV1 paymentRequestPLN(
            String trackId, Account from, Account to, double amount
    ) {
        return paymentRequest(trackId, from, to, amount, PLN);
    }

    private static PaymentApiRequestV1 paymentRequest(
            String trackId, Account from, Account to, double amount, CurrencyUnit currency
    ) {
        return new PaymentApiRequestV1(
                from.getUserId(),
                from.getAccountId(),
                to.getAccountId(),
                Money.of(currency, BigDecimal.valueOf(amount)),
                trackId
        );
    }
}
