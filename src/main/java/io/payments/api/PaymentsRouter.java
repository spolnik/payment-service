package io.payments.api;

import spark.RouteGroup;
import spark.Spark;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PaymentsRouter implements Router {

    private static final String API_PREFIX = "/api";
    private final Set<VersionedApi> api;

    @Inject
    public PaymentsRouter(Set<VersionedApi> api) {
        this.api = api;
    }

    @Override
    public RouteGroup routes() {
        return () -> {
            List<String> versions = api.stream()
                    .map(VersionedApi::version).distinct().collect(Collectors.toList());

            versions.forEach(versionedPath());
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
