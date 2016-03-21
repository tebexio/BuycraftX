package net.buycraft.plugin.util;

import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;
import net.buycraft.plugin.data.responses.Version;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;

@UtilityClass
public class VersionUtil {
    public static Version getVersion(OkHttpClient client, String platform) throws IOException {
        Request request = new Request.Builder()
                .url("https://plugin.buycraft.net/versions/" + platform)
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            return null;
        }

        try (ResponseBody body = response.body()) {
            return new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .create()
                    .fromJson(body.string(), Version.class);
        }
    }
}
