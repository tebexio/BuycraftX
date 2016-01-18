package net.buycraft.plugin.data.responses;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
public class ServerInformation {
    private final Map<String, List<Platform>> plugins;
    private final Account account;
    private final Server server;

    @Value
    public static class Platform {
        private final String version;
        @SerializedName("download_url")
        private final String downloadUrl;
    }

    @Value
    public static class Account {
        private final int id;
        private final String domain;
        private final String name;
        private final AccountCurrency currency;
        @SerializedName("online_mode")
        private final boolean onlineMode;
    }

    @Value
    public static class Server {
        private final int id;
        private final String name;
    }

    @Value
    public static class AccountCurrency {
        @SerializedName("iso_4217")
        private final String iso4217;
        private final String symbol;
    }
}
