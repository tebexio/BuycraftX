package net.buycraft.plugin.shared.util;

import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.execution.DuePlayerFetcher;
import net.buycraft.plugin.shared.config.BuycraftConfiguration;
import net.buycraft.plugin.shared.tasks.ListingUpdateTask;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Objects;

public class ReportBuilder {
    @NotNull
    private final DuePlayerFetcher duePlayerFetcher;
    private final ListingUpdateTask listingUpdateTask;
    @NotNull
    private final String ip;
    private final int port;
    private final boolean serverOnlineMode;
    @NotNull
    private final IBuycraftPlatform platform;
    @NotNull
    private final BuycraftConfiguration configuration;
    @NotNull
    private final OkHttpClient client;

    ReportBuilder(@NotNull final DuePlayerFetcher duePlayerFetcher, final ListingUpdateTask listingUpdateTask, @NotNull final String ip, final int port, final boolean serverOnlineMode, @NotNull final IBuycraftPlatform platform, @NotNull final BuycraftConfiguration configuration, @NotNull final OkHttpClient client) {
        this.duePlayerFetcher = Objects.requireNonNull(duePlayerFetcher);
        this.listingUpdateTask = listingUpdateTask;
        this.ip = Objects.requireNonNull(ip);
        this.port = port;
        this.serverOnlineMode = serverOnlineMode;
        this.platform = Objects.requireNonNull(platform);
        this.configuration = Objects.requireNonNull(configuration);
        this.client = Objects.requireNonNull(client);
    }

    public static ReportBuilderBuilder builder() {
        return new ReportBuilderBuilder();
    }

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
        writer.println("Server version: " + platform.getPlatformInformation().getType() + " / " + platform.getPlatformInformation().getVersion());
        writer.println("Server IP and port: " + ip + " / " + port);
        writer.println("Online mode: " + serverOnlineMode);
        writer.println("Buycraft is-bungeecord setting: " + configuration.isBungeeCord());
        writer.println();
        writer.println("### Plugin Information ###");
        writer.println("Plugin version: " + platform.getPluginVersion());
        writer.println("Platform: " + platform.getPlatformInformation().getType() + " / " + platform.getPlatformInformation().getVersion());
        writer.println();
        writer.println("Connected to Buycraft? " + (platform.getApiClient() != null));
        ServerInformation information = platform.getServerInformation();
        writer.println("Web store information found? " + (information != null));
        if (information != null) {
            writer.println("Web store ID: " + information.getAccount().getId());
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
        Request request = new Request.Builder().get().cacheControl(new CacheControl.Builder().noStore().build()).url(url).build();
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

    public static class ReportBuilderBuilder {
        private DuePlayerFetcher duePlayerFetcher;
        private ListingUpdateTask listingUpdateTask;
        private String ip;
        private int port;
        private boolean serverOnlineMode;
        private IBuycraftPlatform platform;
        private BuycraftConfiguration configuration;
        private OkHttpClient client;

        ReportBuilderBuilder() {
        }

        public ReportBuilderBuilder duePlayerFetcher(@NotNull final DuePlayerFetcher duePlayerFetcher) {
            this.duePlayerFetcher = Objects.requireNonNull(duePlayerFetcher);
            return this;
        }

        public ReportBuilderBuilder listingUpdateTask(final ListingUpdateTask listingUpdateTask) {
            this.listingUpdateTask = listingUpdateTask;
            return this;
        }

        public ReportBuilderBuilder ip(@NotNull final String ip) {
            this.ip = Objects.requireNonNull(ip);
            return this;
        }

        public ReportBuilderBuilder port(final int port) {
            this.port = port;
            return this;
        }

        public ReportBuilderBuilder serverOnlineMode(final boolean serverOnlineMode) {
            this.serverOnlineMode = serverOnlineMode;
            return this;
        }

        public ReportBuilderBuilder platform(@NotNull final IBuycraftPlatform platform) {
            this.platform = Objects.requireNonNull(platform);
            return this;
        }

        public ReportBuilderBuilder configuration(@NotNull final BuycraftConfiguration configuration) {
            this.configuration = Objects.requireNonNull(configuration);
            return this;
        }

        public ReportBuilderBuilder client(@NotNull final OkHttpClient client) {
            this.client = Objects.requireNonNull(client);
            return this;
        }

        public ReportBuilder build() {
            return new ReportBuilder(duePlayerFetcher, listingUpdateTask, ip, port, serverOnlineMode, platform, configuration, client);
        }

        @Override
        public String toString() {
            return "ReportBuilder.ReportBuilderBuilder(duePlayerFetcher=" + this.duePlayerFetcher + ", listingUpdateTask=" + this.listingUpdateTask + ", ip=" + this.ip + ", port=" + this.port + ", serverOnlineMode=" + this.serverOnlineMode + ", platform=" + this.platform + ", configuration=" + this.configuration + ", client=" + this.client + ")";
        }
    }
}
