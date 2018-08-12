package io.payments.api;

import com.google.gson.Gson;

public class Common {
    private static final Gson gson = new Gson();

    public static Gson gson() { return gson; }
    public static String json() {
        return "application/json";
    }
}
