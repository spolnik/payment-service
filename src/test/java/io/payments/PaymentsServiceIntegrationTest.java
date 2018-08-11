package io.payments;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import spark.Spark;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

public class PaymentsServiceIntegrationTest {

    private static final int TEST_PORT = 8080;
    private static final String BASE_HOST = "http://localhost";

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
    public void accepts_home_request() {
        given().
            accept(ContentType.JSON).
        when().
            get("/").
        then().
            statusCode(200).
            body(
                "info", is("Payments Service 1.0.0")
            );
    }
}
