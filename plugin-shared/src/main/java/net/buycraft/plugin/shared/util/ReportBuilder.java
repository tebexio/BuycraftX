package net.buycraft.plugin.shared.util;

import lombok.Builder;
import lombok.NonNull;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.execution.DuePlayerFetcher;
import net.buycraft.plugin.shared.config.BuycraftConfiguration;
import net.buycraft.plugin.shared.tasks.ListingUpdateTask;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

@Builder
public class ReportBuilder {
    @NonNull
    private final DuePlayerFetcher duePlayerFetcher;
    private final ListingUpdateTask listingUpdateTask;
    @NonNull
    private final String ip;
    private final int port;
    private final boolean serverOnlineMode;
    @NonNull
    private final IBuycraftPlatform platform;
    @NonNull
    private final BuycraftConfiguration configuration;
    @NonNull
    private final OkHttpClient client;

    public String generate() {
        final StringWriter out = new StringWriter();
        final PrintWriter writer = new PrintWriter(out, true);

        String date = new Date().toString();
        final String os = System.getProperty("os.name") + " | " + System.getProperty("os.version") + " | " + System.getProperty("os.arch");
        String javaVersion = System.getProperty("java.version") + " | " + System.getProperty("java.vendor");

        writer.println("### Server Information ###");
        writer.println("Report generated on " + date);
        writer.println();

        writer.println("Operating system: " + os);
        writer.println("Java version: " + javaVersion);
        writer.println("Server version: " + platform.getPlatformInformation().getType() + " / " +
                platform.getPlatformInformation().getVersion());
        writer.println("Server IP and port: " + ip + " / " + port);
        writer.println("Online mode: " + serverOnlineMode);
        writer.println("Buycraft is-bungeecord setting: " + configuration.isBungeeCord());
        writer.println();

        writer.println("### Plugin Information ###");
        writer.println("Plugin version: " + platform.getPluginVersion());
        writer.println("Platform: " + platform.getPlatformInformation().getType() + " / " +
                platform.getPlatformInformation().getVersion());
        writer.println();
        writer.println("Connected to Buycraft? " + (platform.getApiClient() != null));
        ServerInformation information = platform.getServerInformation();
        writer.println("Web store information found? " + (information != null));
        if (information != null) {
            writer.println("Web store ID: " +information.getAccount().getId());
            writer.println("Web store URL: " + information.getAccount().getDomain());
            writer.println("Web store name: " + information.getAccount().getName());
            writer.println("Web store currency: " + information.getAccount().getCurrency().getIso4217());
            writer.println("Web store in online mode? " + information.getAccount().isOnlineMode());

            writer.println("Server name: " + information.getServer().getName());
            writer.println("Server ID: " + information.getServer().getId());
        }

        writer.println("Players in queue: " + duePlayerFetcher.getDuePlayers());
        if (listingUpdateTask != null) {
            writer.println("Listing update last completed: " + listingUpdateTask.getLastUpdate());
        }
        writer.println();
        writer.println("### Service status ###");

        // Try fetching test URLs
        tryGet("Buycraft plugin API", "https://plugin.buycraft.net", writer);
        tryGet("Google over HTTPS", "https://encrypted.google.com", writer);
        tryGet("Google over HTTP", "http://www.google.com", writer);

        return out.toString();
    }

    private void tryGet(String type, String url, PrintWriter writer) {
        Request request = new Request.Builder()
                .get()
                .cacheControl(new CacheControl.Builder().noStore().build())
                .url(url)
                .build();

        try {
            Response response = client.newCall(request).execute();
            writer.println("Can access " + type + " (" + url + ")");

            // close the body!
            response.body().close();
        } catch (IOException e) {
            writer.println("Can't access " + type + " (" + url + "):");
            e.printStackTrace(writer);
        }
    }
}
