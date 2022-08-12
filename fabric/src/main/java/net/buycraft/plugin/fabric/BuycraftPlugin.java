package net.buycraft.plugin.fabric;

import net.buycraft.plugin.BuyCraftAPI;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.execution.DuePlayerFetcher;
import net.buycraft.plugin.execution.ServerEventSenderTask;
import net.buycraft.plugin.execution.placeholder.PlaceholderManager;
import net.buycraft.plugin.execution.strategy.CommandExecutor;
import net.buycraft.plugin.execution.strategy.PostCompletedCommandsTask;
import net.buycraft.plugin.fabric.util.VersionCheck;
import net.buycraft.plugin.shared.Setup;
import net.buycraft.plugin.shared.config.BuycraftConfiguration;
import net.buycraft.plugin.shared.config.BuycraftI18n;
import net.buycraft.plugin.shared.config.signs.BuyNowSignLayout;
import net.buycraft.plugin.shared.config.signs.RecentPurchaseSignLayout;
import net.buycraft.plugin.shared.config.signs.storage.BuyNowSignStorage;
import net.buycraft.plugin.shared.config.signs.storage.RecentPurchaseSignStorage;
import net.buycraft.plugin.shared.tasks.ListingUpdateTask;
import net.buycraft.plugin.shared.tasks.PlayerJoinCheckTask;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class BuycraftPlugin implements ModInitializer {
    private final PlaceholderManager placeholderManager = new PlaceholderManager();
    private final BuycraftConfiguration configuration = new BuycraftConfiguration();

    private BuyCraftAPI apiClient;
    private DuePlayerFetcher duePlayerFetcher;
    private ListingUpdateTask listingUpdateTask;
    private ServerInformation serverInformation;
    private RecentPurchaseSignStorage recentPurchaseSignStorage;
    private BuyNowSignStorage buyNowSignStorage;
    private OkHttpClient httpClient;
    private IBuycraftPlatform platform;
    private CommandExecutor commandExecutor;

    private RecentPurchaseSignLayout recentPurchaseSignLayout = RecentPurchaseSignLayout.DEFAULT;
    private BuyNowSignLayout buyNowSignLayout = BuyNowSignLayout.DEFAULT;
    private BuycraftI18n i18n;
    private PostCompletedCommandsTask completedCommandsTask;
    private PlayerJoinCheckTask playerJoinCheckTask;
    private ServerEventSenderTask serverEventSenderTask;

    private final String MOD_ID = "buycraft";
    private final String MOD_VERSION = "1.0.0";

    private final Path MOD_PATH = new File("./mods/" + MOD_ID).toPath();
    public final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private MinecraftServer server;

    @Override
    public void onInitialize() {
        platform = new FabricBuycraftPlatform(this);
        try {
            try {
                Files.createDirectory(MOD_PATH);
            } catch (FileAlreadyExistsException ignored) {
            }
            Path configPath = MOD_PATH.resolve("config.properties");
            try {
                configuration.load(configPath);
            } catch (NoSuchFileException e) {
                // Save defaults
                configuration.fillDefaults();
                configuration.save(configPath);
            }
        } catch (IOException e) {
            LOGGER.error("Unable to load configuration! The plugin will disable itself now.", e);
            return;
        }

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            this.server = server;

            i18n = configuration.createI18n();
            httpClient = Setup.okhttp(MOD_PATH.resolve("cache").toFile());

            if (configuration.isCheckForUpdates()) {
                VersionCheck check = new VersionCheck(this, MOD_VERSION, configuration.getServerKey());
                try {
                    check.verify();
                } catch (IOException e) {
                    LOGGER.error("Can't check for updates", e);
                }

//            Sponge.getEventManager().registerListeners(this, check);
            }

            String serverKey = configuration.getServerKey();
            if (serverKey == null || serverKey.equals("INVALID")) {
                LOGGER.info("Looks like this is a fresh setup. Get started by using 'tebex secret <key>' in the console.");
            } else {
                LOGGER.info("Validating your server key...");
                BuyCraftAPI client = BuyCraftAPI.create(configuration.getServerKey(), httpClient);
                try {
                    updateInformation(client);
                } catch (IOException e) {
                    LOGGER.error(String.format("We can't check if your server can connect to Tebex: %s", e.getMessage()));
                }
                apiClient = client;
            }
        });
    }

//    private CommandSpec buildCommands() {
//        CommandSpec refresh = CommandSpec.builder()
//                .description(Text.of(i18n.get("usage_refresh")))
//                .permission("buycraft.admin")
//                .executor(new RefreshCmd(this))
//                .build();
//        CommandSpec secret = CommandSpec.builder()
//                .description(Text.of(i18n.get("usage_secret")))
//                .permission("buycraft.admin")
//                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("secret"))))
//                .executor(new SecretCmd(this))
//                .build();
//        CommandSpec report = CommandSpec.builder()
//                .description(Text.of(i18n.get("usage_report")))
//                .executor(new ReportCmd(this))
//                .permission("buycraft.admin")
//                .build();
//        CommandSpec info = CommandSpec.builder()
//                .description(Text.of(i18n.get("usage_information")))
//                .executor(new InfoCmd(this))
//                .build();
//        CommandSpec forcecheck = CommandSpec.builder()
//                .description(Text.of(i18n.get("usage_forcecheck")))
//                .executor(new ForceCheckCmd(this))
//                .permission("buycraft.admin")
//                .build();
//        CommandSpec coupon = buildCouponCommands();
//        return CommandSpec.builder()
//                .description(Text.of("Main command for the Tebex plugin."))
//                .child(report, "report")
//                .child(secret, "secret")
//                .child(refresh, "refresh")
//                .child(info, "info")
//                .child(forcecheck, "forcecheck")
//                .child(coupon, "coupon")
//                .build();
//    }
//
//    private CommandSpec buildCouponCommands() {
//        CouponCmd cmd = new CouponCmd(this);
//        CommandSpec create = CommandSpec.builder()
//                .executor(cmd::createCoupon)
//                .arguments(GenericArguments.allOf(GenericArguments.string(Text.of("args"))))
//                .build();
//        CommandSpec delete = CommandSpec.builder()
//                .executor(cmd::deleteCoupon)
//                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("code"))))
//                .build();
//        return CommandSpec.builder()
//                .description(Text.of(i18n.get("usage_coupon")))
//                .permission("buycraft.admin")
//                .child(create, "create")
//                .child(delete, "delete")
//                .build();
//    }

    public void saveConfiguration() throws IOException {
        configuration.save(MOD_PATH.resolve("config.properties"));
    }

    public void updateInformation(BuyCraftAPI client) throws IOException {
        serverInformation = client.getServerInformation().execute().body();
        if (!configuration.isBungeeCord() && server.isOnlineMode() != serverInformation.getAccount().isOnlineMode()) {
            getLogger().warn("Your server and webstore online mode settings are mismatched. Unless you are using" +
                    " a proxy and server combination (such as BungeeCord/Spigot or LilyPad/Connect) that corrects UUIDs, then" +
                    " you may experience issues with packages not applying.");
            getLogger().warn("If you have verified that your set up is correct, you can suppress this message by setting " +
                    "is-bungeecord=true in your BuycraftX config.properties.");
        }
    }

    public PlaceholderManager getPlaceholderManager() {
        return this.placeholderManager;
    }

    public BuycraftConfiguration getConfiguration() {
        return this.configuration;
    }

    public BuyCraftAPI getApiClient() {
        return this.apiClient;
    }

    public void setApiClient(final BuyCraftAPI apiClient) {
        this.apiClient = apiClient;
    }

    public DuePlayerFetcher getDuePlayerFetcher() {
        return this.duePlayerFetcher;
    }

    public ListingUpdateTask getListingUpdateTask() {
        return this.listingUpdateTask;
    }

    public ServerInformation getServerInformation() {
        return this.serverInformation;
    }

    public RecentPurchaseSignStorage getRecentPurchaseSignStorage() {
        return this.recentPurchaseSignStorage;
    }

    public BuyNowSignStorage getBuyNowSignStorage() {
        return this.buyNowSignStorage;
    }

    public OkHttpClient getHttpClient() {
        return this.httpClient;
    }

    public IBuycraftPlatform getPlatform() {
        return this.platform;
    }

    public CommandExecutor getCommandExecutor() {
        return this.commandExecutor;
    }

    public Logger getLogger() {
        return LOGGER;
    }

    public Path getBaseDirectory() {
        return MOD_PATH;
    }

    public RecentPurchaseSignLayout getRecentPurchaseSignLayout() {
        return this.recentPurchaseSignLayout;
    }

    public BuyNowSignLayout getBuyNowSignLayout() {
        return this.buyNowSignLayout;
    }

    public BuycraftI18n getI18n() {
        return this.i18n;
    }

    public PlayerJoinCheckTask getPlayerJoinCheckTask() {
        return this.playerJoinCheckTask;
    }

    public ServerEventSenderTask getServerEventSenderTask() {
        return serverEventSenderTask;
    }
}
