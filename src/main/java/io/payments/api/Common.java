package io.payments.api;

import com.google.gson.*;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.lang.reflect.Type;

public class Common {
    private static final Gson gson;

    static {
        gson = new GsonBuilder().
                registerTypeAdapter(Money.class, new MoneyTransformer()).
                create();
    }

    public static Gson gson() { return gson; }
    public static String json() {
        return "application/json";
    }

    private static class MoneyTransformer implements JsonSerializer<Money>, JsonDeserializer<Money> {
        @Override
        public Money deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            return Money.of(
                    CurrencyUnit.getInstance(jsonObject.get("currency").getAsString()),
                    jsonObject.get("amount").getAsBigDecimal()
            );
        }

        @Override
        public JsonElement serialize(Money src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("currency", src.getCurrencyUnit().getCode());
            jsonObject.addProperty("amount", src.getAmount());

            return jsonObject;
        }
    }
}
