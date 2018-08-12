package io.payments.api;

import static io.payments.api.Common.gson;

public interface ApiResponse {
    default String toJson() {
        return gson().toJson(this);
    }
}
