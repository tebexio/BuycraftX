package net.buycraft.plugin.fabric;

import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpServer;
import net.buycraft.plugin.BuyCraftAPI;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.execution.DuePlayerFetcher;
import net.buycraft.plugin.execution.ServerEventSenderTask;
import net.buycraft.plugin.execution.placeholder.NamePlaceholder;
import net.buycraft.plugin.execution.placeholder.PlaceholderManager;
import net.buycraft.plugin.execution.placeholder.UuidPlaceholder;
import net.buycraft.plugin.execution.strategy.CommandExecutor;
import net.buycraft.plugin.execution.strategy.PostCompletedCommandsTask;
import net.buycraft.plugin.execution.strategy.QueuedCommandExecutor;
import net.buycraft.plugin.fabric.command.TebexCommand;
import net.buycraft.plugin.fabric.httplistener.Handler;
import net.buycraft.plugin.fabric.util.Multithreading;
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
import net.buycraft.plugin.shared.util.AnalyticsSend;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class BuycraftPlugin implements DedicatedServerModInitializer {
    private static final String MOD_ID = "buycraft";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

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

    private final String MOD_VERSION = "1.0.0";
    private final int TICKS_PER_SECOND = 50;

    private final Path MOD_PATH = new File("./mods/" + MOD_ID).toPath();

    private MinecraftServer server;

    @Override
    public void onInitializeServer() {
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

        CommandRegistrationCallback.EVENT.register(new TebexCommand(this)::register);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            this.server = server;

            i18n = configuration.createI18n();
            httpClient = Setup.okhttp(MOD_PATH.resolve("cache").toFile());

            // Check for latest version.
            if (configuration.isCheckForUpdates()) {
                VersionCheck check = new VersionCheck(this, MOD_VERSION, configuration.getServerKey());
                try {
                    check.verify();
                } catch (IOException e) {
                    LOGGER.error("Can't check for updates", e);
                }

                ServerPlayConnectionEvents.JOIN.register(check);
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

            Integer pushCommandsPort = configuration.getPushCommandsPort();
            if (pushCommandsPort != null) {
                this.initializeHttpListener(pushCommandsPort);
            }
            placeholderManager.addPlaceholder(new NamePlaceholder());
            placeholderManager.addPlaceholder(new UuidPlaceholder());
            platform.executeAsyncLater(duePlayerFetcher = new DuePlayerFetcher(platform, configuration.isVerbose()), 1, TimeUnit.SECONDS);
            completedCommandsTask = new PostCompletedCommandsTask(platform);
            commandExecutor = new QueuedCommandExecutor(platform, completedCommandsTask);

            Multithreading.schedule((Runnable) commandExecutor, TICKS_PER_SECOND, TICKS_PER_SECOND, TimeUnit.MILLISECONDS);
            Multithreading.schedule(completedCommandsTask, TICKS_PER_SECOND*20, TICKS_PER_SECOND*20, TimeUnit.MILLISECONDS);
            playerJoinCheckTask = new PlayerJoinCheckTask(platform);
            Multithreading.schedule(playerJoinCheckTask, TICKS_PER_SECOND*20, TICKS_PER_SECOND*20, TimeUnit.MILLISECONDS);
            serverEventSenderTask = new ServerEventSenderTask(platform, configuration.isVerbose());
            Multithreading.schedule(serverEventSenderTask, (TICKS_PER_SECOND*20)*60, (TICKS_PER_SECOND*20)*60, TimeUnit.MILLISECONDS);
            listingUpdateTask = new ListingUpdateTask(platform, null);
            if (apiClient != null) {
                getLogger().info("Fetching all server packages...");
                listingUpdateTask.run();
            }
            Multithreading.schedule(listingUpdateTask, 20, 20, TimeUnit.MINUTES);

            recentPurchaseSignStorage = new RecentPurchaseSignStorage();
            try {
                recentPurchaseSignStorage.load(MOD_PATH.resolve("purchase_signs.json"));
            } catch (IOException | JsonParseException e) {
                LOGGER.warn("Can't load purchase signs, continuing anyway", e);
            }

            buyNowSignStorage = new BuyNowSignStorage();
            try {
                buyNowSignStorage.load(MOD_PATH.resolve("buy_now_signs.json"));
            } catch (IOException | JsonParseException e) {
                LOGGER.warn("Can't load purchase signs, continuing anyway", e);
            }

            try {
                Path signLayoutDirectory = MOD_PATH.resolve("sign_layouts");
                try {
                    Files.createDirectory(signLayoutDirectory);
                } catch (FileAlreadyExistsException ignored) {
                }

                Path rpPath = signLayoutDirectory.resolve("recentpurchase.txt");
                Path bnPath = signLayoutDirectory.resolve("buynow.txt");

                try {
                    Files.copy(getClass().getClassLoader().getResourceAsStream("sign_layouts/recentpurchase.txt"), rpPath);
                } catch (FileAlreadyExistsException ignored) {
                }
                try {
                    Files.copy(getClass().getClassLoader().getResourceAsStream("sign_layouts/buynow.txt"), bnPath);
                } catch (FileAlreadyExistsException ignored) {
                }

                recentPurchaseSignLayout = new RecentPurchaseSignLayout(Files.readAllLines(rpPath, StandardCharsets.UTF_8));
                buyNowSignLayout = new BuyNowSignLayout(Files.readAllLines(bnPath, StandardCharsets.UTF_8));
            } catch (IOException e) {
                getLogger().error("Unable to load sign layouts", e);
            }

            Multithreading.schedule(() -> {
//                new SignUpdater(this);
            }, 60*15, 1, TimeUnit.SECONDS);
            Multithreading.schedule(() -> {
//                new BuyNowSignUpdater(this);
            }, 60*15, 1, TimeUnit.SECONDS);

            if (serverInformation != null) {
                Multithreading.schedule(() -> {
                    try {
                        AnalyticsSend.postServerInformation(httpClient, configuration.getServerKey(), platform, server.isOnlineMode());
                    } catch (IOException e) {
                        getLogger().warn("Can't send analytics", e);
                    }
                }, 1, 1, TimeUnit.DAYS);
            }

//            Sponge.getEventManager().registerListeners(this, new BuycraftListener(this));
//            Sponge.getEventManager().registerListeners(this, new RecentPurchaseSignListener(this));
//            Sponge.getEventManager().registerListeners(this, new BuyNowSignListener(this));

        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            try {
                recentPurchaseSignStorage.save(MOD_PATH.resolve("purchase_signs.json"));
            } catch (IOException e) {
                LOGGER.error("Can't save purchase signs, continuing anyway");
            }
            try {
                buyNowSignStorage.save(MOD_PATH.resolve("buy_now_signs.json"));
            } catch (IOException e) {
                LOGGER.error("Can't save purchase signs, continuing anyway");
            }
            completedCommandsTask.flush();
        });

    }

    private void initializeHttpListener(Integer port) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 50);
            server.createContext("/", new Handler(this));
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public org.apache.logging.log4j.Logger getLogger() {
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

    public MinecraftServer getServer() {
        return server;
    }

    public String getModVersion() {
        return MOD_VERSION;
    }
}
