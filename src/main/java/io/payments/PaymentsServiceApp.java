package io.payments;

import com.google.inject.Guice;
import io.payments.api.Router;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Filter;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Optional;

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
        staticFiles.location("/public");

        before("/*", logIncomingRequest());

        path(router.path(), router.routes());

        after("/*", (req, res) -> LOG.info("\n\t<<<< {}", res.body()));

        awaitInitialization();
        LOG.info("Running at: http://localhost:{}", port);
    }

    @NotNull
    private Filter logIncomingRequest() {
        return (req, res) -> {
            LOG.info("{} {}", req.requestMethod(), req.pathInfo());
            if ("POST".equals(req.requestMethod())) {
                LOG.info("\n\t>>>> {}", req.body());
            }
        };
    }

    public static void main(String[] args) {
        int port = clarifyPort();
        if (args != null && args.length > 0) {
            port = Integer.parseInt(overwriteWithCommandLine(args, Integer.toString(port), "port"));
        }

        String dbName = overwriteWithCommandLine(args, "PaymentStore", "dbName");

        Guice.createInjector(new PaymentsModule(dbName))
                .getInstance(PaymentsServiceApp.class)
                .run(port);
    }

    private static String overwriteWithCommandLine(String[] args, String defaultValue, String argName) {
        Optional<String> arg = Arrays.stream(args)
                .filter(it -> it.startsWith(argName + "=")).findFirst();

        return arg.map(s -> s.split("=")[1]).orElse(defaultValue);
    }

    private static int clarifyPort() {
        String envPort = System.getenv("PORT");
        return envPort != null ? Integer.parseInt(envPort) : DEFAULT_PORT;
    }
}
