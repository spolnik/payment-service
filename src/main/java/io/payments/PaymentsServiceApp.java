package io.payments;

import com.google.inject.Guice;
import io.payments.api.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Route;

import javax.inject.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

import static io.payments.api.Common.json;
import static spark.Spark.*;

public class PaymentsServiceApp {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentsServiceApp.class);
    private static final int DEFAULT_PORT = 4000;

    private final Router router;
    private final String version;

    @Inject
    public PaymentsServiceApp(Router router) {
        this.router = router;
        this.version = readVersion();
    }

    private void run(int port) {
        port(port);
        staticFiles.location("/public");

        before("/*",
                (req, res) -> LOG.info("{} {}", req.requestMethod(), req.pathInfo())
        );

        get("/", json(), home());
        path(router.path(), router.routes());

        after("/*", (req, res) -> LOG.info(res.body()));

        awaitInitialization();
        LOG.info("Running at: http://localhost:{}", port);
    }

    private Route home() {
        return (req, res) -> {
            res.type(json());
            return String.format("{\"info\": \"Payments Service %s\"}", version);
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

    private static String readVersion() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("version.properties");
        Properties properties = new Properties();
        try {
            properties.load(is);
            return properties.getProperty("version");
        } catch (IOException e) {
            LOG.warn("Cannot read version: {}", e.getMessage());
            return "version unknown";
        }
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
