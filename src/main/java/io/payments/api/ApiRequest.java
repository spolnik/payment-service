package io.payments.api;

import static io.payments.api.Common.gson;

public interface ApiRequest {
    default String toJson() {
        return gson().toJson(this);
    }
}
