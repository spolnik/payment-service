package io.payments.account;

import io.payments.PaymentsServiceApp;
import io.payments.TestUtils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import spark.Spark;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

import static io.payments.TestUtils.createAccount;
import static io.payments.api.Common.gson;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountApiV1IntegrationTest {
    private static final int TEST_PORT =
            TestUtils.findRandomOpenPortOnAllLocalInterfaces();

    private static final String BASE_HOST = "http://localhost";
    private static final String TEST_DB_NAME = "AccountApiV1IntegrationTest";

    @BeforeClass
    public static void setUpTest() {
        PaymentsServiceApp.main(new String[] {"port=" + TEST_PORT, "dbName=" + TEST_DB_NAME});
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
    public void returns_successful_response_after_creating_new_account() {
        String trackId = UUID.randomUUID().toString();
        Account newAccount = createAccount("TestAccount1", 3333);

        CreateAccountApiRequestV1 createAccountRequest =
                createNewAccountRequest(trackId, newAccount);

        CreateAccountApiResponseV1 response = createNewAccount(createAccountRequest);

        assertThat(response.getAccountId()).isEqualTo(createAccountRequest.getAccountId());
        assertThat(response.getStatus()).isEqualTo(AccountStatus.CREATED.toString());
        assertThat(response.getTrackId()).isEqualTo(trackId);
    }

    @Test
    public void rejects_account_creation_when_account_id_is_already_used() {
        String trackId = UUID.randomUUID().toString();
        Account newAccount = createAccount("TestAccount2", 3333);

        CreateAccountApiRequestV1 createAccountRequest =
                createNewAccountRequest(trackId, newAccount);

        // @formatter:off
        given().
            accept(ContentType.JSON).
            contentType(ContentType.JSON).
            body(createAccountRequest.toJson()).
        when().
            post("/api/v1/accounts").
        then().
            statusCode(200);

        CreateAccountApiResponseV1 response =
                given().
                    accept(ContentType.JSON).
                    contentType(ContentType.JSON).
                    body(createAccountRequest.toJson()).
                when().
                    post("/api/v1/accounts").
                then().
                    statusCode(409).
                extract().body().as(CreateAccountApiResponseV1.class);
        // @formatter:on

        assertThat(response.getAccountId()).isEqualTo(createAccountRequest.getAccountId());
        assertThat(response.getStatus()).isEqualTo(AccountStatus.ALREADY_EXIST.toString());
        assertThat(response.getTrackId()).isEqualTo(trackId);
    }

    @Test
    public void returns_created_account_when_queried() {
        String trackId = UUID.randomUUID().toString();
        Account newAccount = createAccount("TestAccount3", 5555);

        CreateAccountApiRequestV1 createAccountRequest =
                createNewAccountRequest(trackId, newAccount);

        // @formatter:off
        given().
            accept(ContentType.JSON).
            contentType(ContentType.JSON).
            body(createAccountRequest.toJson()).
        when().
            post("/api/v1/accounts").
        then().
            statusCode(200);

        String accountAsJson =
            given().
                accept(ContentType.JSON).
            when().
                get("/api/v1/accounts/" + newAccount.getAccountId()).
            then().
                statusCode(200).
            extract().body().asString();
        // @formatter:on

        Account account = gson().fromJson(accountAsJson, Account.class);

        assertThat(account.getUserId()).isEqualTo(newAccount.getUserId());
        assertThat(account.getAccountId()).isEqualTo(newAccount.getAccountId());
        assertThat(account.getBalance()).isEqualTo(newAccount.getBalance());
    }

    private CreateAccountApiResponseV1 createNewAccount(CreateAccountApiRequestV1 createAccountRequest) {
        // @formatter:off
        return given().
            accept(ContentType.JSON).
            contentType(ContentType.JSON).
            body(createAccountRequest.toJson()).
        when().
            post("/api/v1/accounts").
        then().
            statusCode(200).
        extract()
            .body()
            .as(CreateAccountApiResponseV1.class);
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
