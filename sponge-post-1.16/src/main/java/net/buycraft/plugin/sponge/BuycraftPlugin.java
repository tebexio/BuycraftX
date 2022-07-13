package net.buycraft.plugin.sponge;

import com.google.gson.JsonParseException;
import com.google.inject.Inject;
import com.sun.net.httpserver.HttpServer;
import jdk.javadoc.internal.doclets.formats.html.markup.Text;
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
import net.buycraft.plugin.sponge.command.*;
import net.buycraft.plugin.sponge.httplistener.Handler;
import net.buycraft.plugin.sponge.logging.LoggerUtils;
import net.buycraft.plugin.sponge.signs.buynow.BuyNowSignListener;
import net.buycraft.plugin.sponge.signs.purchases.RecentPurchaseSignListener;
import net.buycraft.plugin.sponge.tasks.BuyNowSignUpdater;
import net.buycraft.plugin.sponge.tasks.SignUpdater;
import net.buycraft.plugin.sponge.util.VersionCheck;
import net.kyori.adventure.text.Component;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.spongepowered.api.Engine;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.LoadedGameEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StoppedGameEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.sql.Time;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Plugin("buycraft")
public class BuycraftPlugin {
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
    @Inject
    private Logger logger;
    private LoggerUtils loggerUtils;
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path baseDirectory;
    private RecentPurchaseSignLayout recentPurchaseSignLayout = RecentPurchaseSignLayout.DEFAULT;
    private BuyNowSignLayout buyNowSignLayout = BuyNowSignLayout.DEFAULT;
    private BuycraftI18n i18n;
    private PostCompletedCommandsTask completedCommandsTask;
    private PlayerJoinCheckTask playerJoinCheckTask;
    private ServerEventSenderTask serverEventSenderTask;

    private PluginContainer plugin;

    public PluginContainer getPlugin() {
        return plugin;
    }

    @Listener
    public void onGamePreInitializationEvent(LoadedGameEvent event) {
        platform = new SpongeBuycraftPlatform(this);
        plugin = event.game().pluginManager().plugin("buycraft").get();

        try {
            try {
                Files.createDirectory(baseDirectory);
            } catch (FileAlreadyExistsException ignored) {
            }
            Path configPath = baseDirectory.resolve("config.properties");
            try {
                configuration.load(configPath);
            } catch (NoSuchFileException e) {
                // Save defaults
                configuration.fillDefaults();
                configuration.save(configPath);
            }
        } catch (IOException e) {
            getLogger().error("Unable to load configuration! The plugin will disable itself now.", e);
            return;
        }
        i18n = configuration.createI18n();
        httpClient = Setup.okhttp(baseDirectory.resolve("cache").toFile());
        // Check for latest version.
        String curVersion = plugin.metadata().version().toString();
        if (configuration.isCheckForUpdates()) {
            VersionCheck check = new VersionCheck(this, curVersion, configuration.getServerKey());
            try {
                check.verify();
            } catch (IOException e) {
                getLogger().error("Can't check for updates", e);
            }
            Sponge.eventManager().registerListeners(plugin, check);
        }
        String serverKey = configuration.getServerKey();
        if (serverKey == null || serverKey.equals("INVALID")) {
            getLogger().info("Looks like this is a fresh setup. Get started by using 'tebex secret <key>' in the console.");
        } else {
            getLogger().info("Validating your server key...");
            BuyCraftAPI client = BuyCraftAPI.create(configuration.getServerKey(), httpClient);
            try {
                updateInformation(client);
            } catch (IOException e) {
                getLogger().error(String.format("We can't check if your server can connect to Tebex: %s", e.getMessage()));
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

        Sponge.asyncScheduler().submit(Task.builder().execute((Runnable) commandExecutor).interval(Ticks.of(1)).delay(Ticks.of(1)).build());
        Sponge.asyncScheduler().submit(Task.builder().execute(completedCommandsTask).interval(Ticks.of(20)).delay(Ticks.of(20)).build());


        playerJoinCheckTask = new PlayerJoinCheckTask(platform);
        Sponge.asyncScheduler().submit(Task.builder().execute(playerJoinCheckTask).interval(Ticks.of(20)).delay(Ticks.of(20)).build());

        serverEventSenderTask = new ServerEventSenderTask(platform, configuration.isVerbose());
        Sponge.asyncScheduler().submit(Task.builder().execute(serverEventSenderTask).delay(Ticks.of(20 * 60)).build());

        listingUpdateTask = new ListingUpdateTask(platform, null);
        if (apiClient != null) {
            getLogger().info("Fetching all server packages...");
            listingUpdateTask.run();
        }
        Sponge.asyncScheduler().submit(Task.builder().execute(listingUpdateTask).interval(20, TimeUnit.MINUTES).interval(20, TimeUnit.SECONDS).build());

        recentPurchaseSignStorage = new RecentPurchaseSignStorage();
        try {
            recentPurchaseSignStorage.load(baseDirectory.resolve("purchase_signs.json"));
        } catch (IOException | JsonParseException e) {
            logger.warn("Can't load purchase signs, continuing anyway", e);
        }

        buyNowSignStorage = new BuyNowSignStorage();
        try {
            buyNowSignStorage.load(baseDirectory.resolve("buy_now_signs.json"));
        } catch (IOException | JsonParseException e) {
            logger.warn("Can't load purchase signs, continuing anyway", e);
        }

        try {
            Path signLayoutDirectory = baseDirectory.resolve("sign_layouts");
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


        Sponge.asyncScheduler().submit(Task.builder().execute(new SignUpdater(this)).interval(15, TimeUnit.MINUTES).delay(1, TimeUnit.SECONDS).build());
        Sponge.asyncScheduler().submit(Task.builder().execute(new BuyNowSignUpdater(this)).interval(15, TimeUnit.MINUTES).delay(1, TimeUnit.SECONDS).build());

        if (serverInformation != null) {
            Sponge.asyncScheduler().submit(Task.builder().execute(() -> {
                try {
                    AnalyticsSend.postServerInformation(httpClient, configuration.getServerKey(), platform, Sponge.server().isOnlineModeEnabled());
                } catch (IOException e) {
                    getLogger().warn("Can't send analytics", e);
                }
            }).interval(1, TimeUnit.DAYS).delay(0, TimeUnit.SECONDS).build());
        }

        Sponge.eventManager().registerListeners(plugin, new BuycraftListener(this));
        Sponge.eventManager().registerListeners(plugin, new RecentPurchaseSignListener(this));
        Sponge.eventManager().registerListeners(plugin, new BuyNowSignListener(this));

//        Sponge.getCommandManager().register(this, buildCommands(), "tebex", "buycraft");
//        Sponge.getCommandManager().register(this, CommandSpec.builder()
//                .description(Text.of(i18n.get("usage_sponge_listing")))
//                .executor(new ListPackagesCmd(this))
//                .build(), configuration.getBuyCommandName());
    }

    @Listener
    public void onCommandRegister(RegisterCommandEvent<Command.Parameterized> event) {
        event.register(plugin, buildCommands(), "tebex", "buycraft");
        event.register(plugin, Command.builder()
        .shortDescription(Component.text(i18n.get("usage_sponge_listing")))
        .executor(new ListPackagesCmd(this))
        .build(), configuration.getBuyCommandName());
    }

    @Listener
    public void onGameStoppingServerEvent(StoppedGameEvent event) {
        try {
            recentPurchaseSignStorage.save(baseDirectory.resolve("purchase_signs.json"));
        } catch (IOException e) {
            logger.error("Can't save purchase signs, continuing anyway");
        }
        try {
            buyNowSignStorage.save(baseDirectory.resolve("buy_now_signs.json"));
        } catch (IOException e) {
            logger.error("Can't save purchase signs, continuing anyway");
        }
        completedCommandsTask.flush();
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

    private Command buildCommands() {
        Command refresh = Command.builder()
                .shortDescription(Component.text(i18n.get("usage_refresh")))
                .permission("buycraft.admin")
                .executor(new RefreshCmd(this))
                .build();
        Command secret = Command.builder()
                .shortDescription(Component.text(i18n.get("usage_secret")))
                .permission("buycraft.admin")
                .addParameter(Parameter.string().key("secret").build())
                .executor(new SecretCmd(this))
                .build();
        Command report = Command.builder()
                .shortDescription(Component.text(i18n.get("usage_report")))
                .executor(new ReportCmd(this))
                .permission("buycraft.admin")
                .build();
        Command info = Command.builder()
                .shortDescription(Component.text(i18n.get("usage_information")))
                .executor(new InfoCmd(this))
                .build();
        Command forcecheck = Command.builder()
                .shortDescription(Component.text(i18n.get("usage_forcecheck")))
                .executor(new ForceCheckCmd(this))
                .permission("buycraft.admin")
                .build();
        Command coupon = buildCouponCommands();
        return Command.builder()
                .shortDescription(Component.text("Main command for the Tebex plugin."))
                .addChild(report, "report")
                .addChild(secret, "secret")
                .addChild(refresh, "refresh")
                .addChild(info, "info")
                .addChild(forcecheck, "forcecheck")
                .addChild(coupon, "coupon")
                .build();
    }

    private CommandSpec buildCouponCommands() {
        CouponCmd cmd = new CouponCmd(this);
        CommandSpec create = CommandSpec.builder()
                .executor(cmd::createCoupon)
                .arguments(GenericArguments.allOf(GenericArguments.string(Text.of("args"))))
                .build();
        CommandSpec delete = CommandSpec.builder()
                .executor(cmd::deleteCoupon)
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("code"))))
                .build();
        return CommandSpec.builder()
                .description(Text.of(i18n.get("usage_coupon")))
                .permission("buycraft.admin")
                .child(create, "create")
                .child(delete, "delete")
                .build();
    }

    public void saveConfiguration() throws IOException {
        configuration.save(baseDirectory.resolve("config.properties"));
    }

    public void updateInformation(BuyCraftAPI client) throws IOException {
        serverInformation = client.getServerInformation().execute().body();
        if (!configuration.isBungeeCord() && Sponge.getServer().getOnlineMode() != serverInformation.getAccount().isOnlineMode()) {
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
        return this.logger;
    }

    public LoggerUtils getLoggerUtils() {
        return this.loggerUtils;
    }

    public Path getBaseDirectory() {
        return this.baseDirectory;
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
