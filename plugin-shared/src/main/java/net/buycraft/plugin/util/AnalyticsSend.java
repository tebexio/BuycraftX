package net.buycraft.plugin.util;

import com.google.gson.Gson;
import lombok.experimental.UtilityClass;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;

@UtilityClass
public class AnalyticsSend {
    public static void sendAnalytics(OkHttpClient client, String secret, Map<String, Object> info) throws IOException {
        Response response = client.newCall(new Request.Builder()
                .url("https://plugin.buycraft.net/analytics/startup")
                .post(RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(info)))
                .header("X-Buycraft-Secret", secret)
                .build()).execute();

        try (ResponseBody body = response.body()) {
            if (response.code() != 201) {
                throw new IOException("Error whilst sending analytics (" + response.code() + "): " + body.string());
            }
        }
    }
}
