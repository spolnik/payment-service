package io.payments.account;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;
import com.google.gson.reflect.TypeToken;
import io.payments.PaymentsServiceApp;
import io.payments.TestUtils;
import io.payments.account.domain.Account;
import io.payments.account.api.CreateAccountApiRequestV1;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import spark.Spark;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static io.payments.TestUtils.createAccount;
import static io.payments.api.Common.gson;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.anyOf;

@RunWith(ConcurrentTestRunner.class)
public class AccountApiV1ConcurrencyTest {

    private static final int TEST_PORT =
            TestUtils.findRandomOpenPortOnAllLocalInterfaces();
    private static final String BASE_HOST = "http://localhost";

    private static final Account ACCOUNT_A = createAccount("A", 10000);
    private static final Account ACCOUNT_B = createAccount("B", 9000);
    private static final Account ACCOUNT_C = createAccount("C", 8000);
    private static final Account ACCOUNT_D = createAccount("D", 7000);
    private static final Account ACCOUNT_E = createAccount("E", 600);

    private static final String TEST_DB_NAME = "AccountApiV1ConcurrencyTest";

    private static final int THREAD_COUNT = 10;
    private static final int ALL_ACCOUNTS = 5;

    @BeforeClass
    public static void setUpTest() {
        PaymentsServiceApp.main(new String[]{"port=" + TEST_PORT, "dbName=" + TEST_DB_NAME});
        RestAssured.baseURI = BASE_HOST;
        RestAssured.port = TEST_PORT;
        Spark.awaitInitialization();
    }

    @AfterClass
    public static void tearDownTest() throws IOException {
        Spark.stop();
        TestUtils.deleteDirectoryStream(Paths.get("./" + TEST_DB_NAME + "_Payments"));
        TestUtils.deleteDirectoryStream(Paths.get("./" + TEST_DB_NAME + "_Accounts"));
    }

    @Test
    @ThreadCount(THREAD_COUNT)
    public void create_accounts() {
        String trackId = UUID.randomUUID().toString();
        createNewAccount(trackId, ACCOUNT_A);

        trackId = UUID.randomUUID().toString();
        createNewAccount(trackId, ACCOUNT_B);

        trackId = UUID.randomUUID().toString();
        createNewAccount(trackId, ACCOUNT_C);

        trackId = UUID.randomUUID().toString();
        createNewAccount(trackId, ACCOUNT_D);

        trackId = UUID.randomUUID().toString();
        createNewAccount(trackId, ACCOUNT_E);
    }

    @After
    public void verify_state() {
        checkAccounts();
    }

    private void checkAccounts() {
        // @formatter:off
        String accountsAsJson =
            given().
                accept(ContentType.JSON).
            when().
                get("/api/v1/accounts").
            then().
                statusCode(200).
            extract().body().asString();

        List<Account> accounts = gson().fromJson(
                accountsAsJson, new TypeToken<List<Account>>() {}.getType()
        );
        // @formatter:on

        assertThat(accounts.size()).isEqualTo(ALL_ACCOUNTS);

        accounts.forEach(account -> {
            assertThat(account.getBalance().isPositive()).isTrue();
            assertThat(account.getAccountId()).isIn(
                    ACCOUNT_A.getAccountId(),
                    ACCOUNT_B.getAccountId(),
                    ACCOUNT_C.getAccountId(),
                    ACCOUNT_D.getAccountId(),
                    ACCOUNT_E.getAccountId()
            );
        });
    }

    private void createNewAccount(String trackId, Account account) {
        CreateAccountApiRequestV1 createAccountRequest =
                createNewAccountRequest(trackId, account);

        // @formatter:off
        given().
            accept(ContentType.JSON).
            contentType(ContentType.JSON).
            body(createAccountRequest.toJson()).
        when().
            post("/api/v1/accounts").
        then().
            statusCode(anyOf(is(200), is(409)));
        // @formatter:on
    }

    private CreateAccountApiRequestV1 createNewAccountRequest(String trackId, Account account) {
        return new CreateAccountApiRequestV1(
                account.getUserId(),
                account.getAccountId(),
                account.getBalance(),
                trackId
        );
    }
}
