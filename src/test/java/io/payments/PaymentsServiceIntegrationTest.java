package io.payments;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import spark.Spark;

import java.io.IOException;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

public class PaymentsServiceIntegrationTest {

    private static final int TEST_PORT =
            TestUtils.findRandomOpenPortOnAllLocalInterfaces();

    private static final String BASE_HOST = "http://localhost";

    private static final String TEST_DB_NAME = "PaymentsServiceIntegrationTest";

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
    public void accepts_home_request() {
        given().
            accept(ContentType.JSON).
        when().
            get("/version").
        then().
            statusCode(200).
            body(
                "name", is("Payments Service"),
                    "version", is("1.0.0")
            );
    }
}
