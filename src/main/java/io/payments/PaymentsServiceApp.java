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

        before("/*",
                (req, res) -> LOG.info("{} {}", req.requestMethod(), req.pathInfo())
        );

        get("/", json(), home());
        path(router.path(), router.routes());

        after("/*", (req, res) -> LOG.info(res.body()));

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
            port = overwritePortWithCommandLine(args, port);
        }

        Guice.createInjector(new PaymentsModule())
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

    private static int overwritePortWithCommandLine(String[] args, int port) {
        Optional<String> portArg = Arrays.stream(args)
                .filter(it -> it.startsWith("port=")).findFirst();

        return portArg.map(s -> Integer.parseInt(s.split("=")[1])).orElse(port);
    }

    private static int clarifyPort() {
        String envPort = System.getenv("PORT");
        return envPort != null ? Integer.parseInt(envPort) : DEFAULT_PORT;
    }
}
