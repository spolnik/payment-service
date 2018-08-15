package io.payments.api;

import spark.RouteGroup;
import spark.Spark;

import javax.inject.Inject;
import java.util.Set;

public class PaymentsRouter implements Router {

    private static final String API_PREFIX = "/api";

    private final Set<VersionedApi> api;

    @Inject
    public PaymentsRouter(Set<VersionedApi> api) {
        this.api = api;
    }

    @Override
    public RouteGroup routes() {
        return () -> api.stream()
                .map(VersionedApi::version)
                .distinct()
                .forEach(version ->
                        Spark.path("/" + version, versionedApi(version)));
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
