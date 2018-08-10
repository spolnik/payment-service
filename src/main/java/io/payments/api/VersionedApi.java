package io.payments.api;

import spark.Route;

import static io.payments.api.Common.json;

public interface VersionedApi extends Router {
    String version();

    default Route info() {
        return (req, res) -> {
            res.type(json());
            return String.format("{\"api\":\"/api/%s/%s\"}", version(), path());
        };
    }
}
