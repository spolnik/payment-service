package io.payments;

import com.google.inject.Guice;
import io.payments.api.Common;
import io.payments.api.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Route;

import javax.inject.Inject;

import static io.payments.api.Common.json;
import static spark.Spark.*;

public class PaymentsServiceApp {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentsServiceApp.class);


    private static final int DEFAULT_PORT = 4000;

    private final Router router;

    @Inject
    public PaymentsServiceApp(Router router) {
        this.router = router;
    }

    private void run(int port) {
        port(port);

        before("/*", (req, res) -> {
            LOG.info("{} {}", req.requestMethod(), req.pathInfo());
        });

        get("/", json(), home());
        path(router.path(), router.routes());

        after("/*", (req, res) -> LOG.info(res.body()));

        LOG.info("Running at: http://localhost:{}", port);
    }

    private static Route home() {
        return (req, res) -> {
            res.type(json());
            return "{\"info\": \"Payments Service 1.0.0\"}";
        };
    }

    public static void main(String[] args) {

        Guice.createInjector(new PaymentsModule())
                .getInstance(PaymentsServiceApp.class)
                .run(clarifyPort());
    }

    private static int clarifyPort() {
        String envPort = System.getenv("PORT");
        return envPort != null ? Integer.parseInt(envPort) : DEFAULT_PORT;
    }
}
