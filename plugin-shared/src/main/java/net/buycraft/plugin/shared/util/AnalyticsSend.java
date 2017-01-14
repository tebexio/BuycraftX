package net.buycraft.plugin.shared.util;

import com.google.gson.Gson;
import lombok.experimental.UtilityClass;
import net.buycraft.plugin.IBuycraftPlatform;
import okhttp3.*;

import java.io.IOException;
import java.util.LinkedHashMap;
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

    public static void postServerInformation(OkHttpClient client, String serverKey, IBuycraftPlatform platform, boolean onlineMode) throws IOException {
        Map<String, Object> serverData = new LinkedHashMap<>();
        Map<String, Object> pluginData = new LinkedHashMap<>();

        // Server data
        serverData.put("platform", platform.getPlatformInformation().getType().platformName());
        serverData.put("platform_version", platform.getPlatformInformation().getVersion());
        serverData.put("online_mode", onlineMode);

        // Plugin data
        pluginData.put("version", platform.getPluginVersion());

        // Combine and send to Buycraft
        Map<String, Object> keenData = new LinkedHashMap<>();
        keenData.put("server", serverData);
        keenData.put("plugin", pluginData);

        AnalyticsSend.sendAnalytics(client, serverKey, keenData);
    }
}
