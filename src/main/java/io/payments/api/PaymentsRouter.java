package io.payments.api;

import spark.Route;
import spark.RouteGroup;
import spark.Spark;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.payments.api.Common.json;
import static spark.Spark.get;

public class PaymentsRouter implements Router {

    private static final String API_PREFIX = "/api";
    private final List<VersionedApi> api;

    @Inject
    public PaymentsRouter(List<VersionedApi> api) {
        this.api = api;
    }

    @Override
    public RouteGroup routes() {
        return () -> {
            List<String> versions = api.stream()
                    .map(VersionedApi::version).distinct().collect(Collectors.toList());

            get("", json(), versionsPath(versions));
            versions.forEach(versionedPath());
        };
    }

    private Route versionsPath(List<String> versions) {
        return (req, res) -> {
            res.type(json());
            return String.format("{\"versions\":%s}", Arrays.toString(versions.toArray()));
        };
    }

    private Consumer<String> versionedPath() {
        return version ->
                Spark.path("/" + version, versionedApi(version));
    }

    private RouteGroup versionedApi(String version) {
        return () -> api.stream()
                .filter(versionedApi -> versionedApi.version().equals(version))
                .forEach(it -> Spark.path(it.path(), it.routes()));
    }

    @Override
    public String path() {
        return API_PREFIX;
    }
}
