package net.buycraft.plugin.sponge;

import com.bugsnag.Bugsnag;
import com.google.gson.JsonParseException;
import com.google.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.client.ApiClient;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.client.ProductionApiClient;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.execution.DuePlayerFetcher;
import net.buycraft.plugin.execution.placeholder.NamePlaceholder;
import net.buycraft.plugin.execution.placeholder.PlaceholderManager;
import net.buycraft.plugin.execution.placeholder.UuidPlaceholder;
import net.buycraft.plugin.execution.strategy.CommandExecutor;
import net.buycraft.plugin.execution.strategy.PostCompletedCommandsTask;
import net.buycraft.plugin.execution.strategy.QueuedCommandExecutor;
import net.buycraft.plugin.shared.Setup;
import net.buycraft.plugin.shared.config.BuycraftConfiguration;
import net.buycraft.plugin.shared.config.BuycraftI18n;
import net.buycraft.plugin.shared.config.signs.RecentPurchaseSignLayout;
import net.buycraft.plugin.shared.config.signs.storage.RecentPurchaseSignStorage;
import net.buycraft.plugin.shared.config.signs.BuyNowSignLayout;
import net.buycraft.plugin.shared.config.signs.storage.BuyNowSignStorage;
import net.buycraft.plugin.shared.tasks.ListingUpdateTask;
import net.buycraft.plugin.shared.tasks.PlayerJoinCheckTask;
import net.buycraft.plugin.shared.util.AnalyticsSend;
import net.buycraft.plugin.sponge.command.*;
import net.buycraft.plugin.sponge.logging.LoggerUtils;
import net.buycraft.plugin.sponge.signs.purchases.RecentPurchaseSignListener;
import net.buycraft.plugin.sponge.signs.buynow.BuyNowSignListener;
import net.buycraft.plugin.sponge.tasks.SignUpdater;
import net.buycraft.plugin.sponge.tasks.BuyNowSignUpdater;
import net.buycraft.plugin.sponge.util.VersionCheck;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Plugin(id = "buycraft", name = "Buycraft", version = BuycraftPlugin.MAGIC_VERSION)
public class BuycraftPlugin {

    static final String MAGIC_VERSION = "SET_BY_MAGIC";
    @Getter
    private final PlaceholderManager placeholderManager = new PlaceholderManager();
    @Getter
    private final BuycraftConfiguration configuration = new BuycraftConfiguration();
    @Getter
    @Setter
    private ApiClient apiClient;
    @Getter
    private DuePlayerFetcher duePlayerFetcher;
    @Getter
    private ListingUpdateTask listingUpdateTask;
    @Getter
    private ServerInformation serverInformation;
    @Getter
    private RecentPurchaseSignStorage recentPurchaseSignStorage;
    @Getter
    private BuyNowSignStorage buyNowSignStorage;
    @Getter
    private OkHttpClient httpClient;
    @Getter
    private IBuycraftPlatform platform;
    @Getter
    private CommandExecutor commandExecutor;

    @Getter
    @Inject
    private Logger logger;
    @Getter
    private LoggerUtils loggerUtils;

    @Getter
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path baseDirectory;

    @Getter
    private RecentPurchaseSignLayout recentPurchaseSignLayout = RecentPurchaseSignLayout.DEFAULT;

    @Getter
    private BuyNowSignLayout buyNowSignLayout = BuyNowSignLayout.DEFAULT;

    @Getter
    private BuycraftI18n i18n;

    private PostCompletedCommandsTask completedCommandsTask;
    @Getter
    private PlayerJoinCheckTask playerJoinCheckTask;

    @Listener
    public void onGamePreInitializationEvent(GamePreInitializationEvent event) {
        platform = new SpongeBuycraftPlatform(this);
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
        String curVersion = getClass().getAnnotation(Plugin.class).version();

        if (configuration.isCheckForUpdates()) {
            VersionCheck check = new VersionCheck(this, curVersion, configuration.getServerKey());
            try {
                check.verify();
            } catch (IOException e) {
                getLogger().error("Can't check for updates", e);
            }
            Sponge.getEventManager().registerListeners(this, check);
        }

        String implVersion = Sponge.getPlatform().getImplementation().getVersion().orElse("UNKNOWN");
        Bugsnag bugsnagClient = Setup.bugsnagClient(httpClient, "sponge", curVersion,
                implVersion, this::getServerInformation);
        loggerUtils = new LoggerUtils(this, bugsnagClient);

        String serverKey = configuration.getServerKey();
        if (serverKey == null || serverKey.equals("INVALID")) {
            getLogger().info("Looks like this is a fresh setup. Get started by using 'buycraft secret <key>' in the console.");
        } else {
            getLogger().info("Validating your server key...");
            ApiClient client = new ProductionApiClient(configuration.getServerKey(), httpClient);
            try {
                updateInformation(client);
            } catch (IOException | ApiException e) {
                getLogger().error(String.format("We can't check if your server can connect to Buycraft: %s", e.getMessage()));
            }
            apiClient = client;
        }
        placeholderManager.addPlaceholder(new NamePlaceholder());
        placeholderManager.addPlaceholder(new UuidPlaceholder());
        platform.executeAsyncLater(duePlayerFetcher = new DuePlayerFetcher(platform, configuration.isVerbose()), 1, TimeUnit.SECONDS);
        completedCommandsTask = new PostCompletedCommandsTask(platform);
        commandExecutor = new QueuedCommandExecutor(platform, completedCommandsTask);
        Sponge.getScheduler().createTaskBuilder().intervalTicks(1).delayTicks(1).execute((Runnable) commandExecutor).submit(this);
        Sponge.getScheduler().createTaskBuilder().intervalTicks(20).delayTicks(20).async().execute(completedCommandsTask).submit(this);
        playerJoinCheckTask = new PlayerJoinCheckTask(platform);
        Sponge.getScheduler().createTaskBuilder().intervalTicks(20).delayTicks(20).execute(playerJoinCheckTask).submit(this);
        listingUpdateTask = new ListingUpdateTask(platform, null);
        if (apiClient != null) {
            getLogger().info("Fetching all server packages...");
            listingUpdateTask.run();
        }
        Sponge.getScheduler().createTaskBuilder().delayTicks(20 * 60 * 20).intervalTicks(20 * 60 * 20).execute(listingUpdateTask).async()
                .submit(this);

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

        Sponge.getScheduler().createTaskBuilder()
                .delay(1, TimeUnit.SECONDS)
                .interval(15, TimeUnit.MINUTES)
                .execute(new SignUpdater(this))
                .submit(this);

        Sponge.getScheduler().createTaskBuilder()
                .delay(1, TimeUnit.SECONDS)
                .interval(15, TimeUnit.MINUTES)
                .execute(new BuyNowSignUpdater(this))
                .submit(this);

        if (serverInformation != null) {
            Sponge.getScheduler().createTaskBuilder()
                    .delay(0, TimeUnit.SECONDS)
                    .interval(1, TimeUnit.DAYS)
                    .execute(() -> {
                        try {
                            AnalyticsSend.postServerInformation(httpClient, configuration.getServerKey(), platform,
                                    Sponge.getServer().getOnlineMode());
                        } catch (IOException e) {
                            getLogger().warn("Can't send analytics", e);
                        }
                    })
                    .submit(this);
        }

        Sponge.getEventManager().registerListeners(this, new BuycraftListener(this));
        Sponge.getEventManager().registerListeners(this, new RecentPurchaseSignListener(this));
        Sponge.getEventManager().registerListeners(this, new BuyNowSignListener(this));

        Sponge.getCommandManager().register(this, buildCommands(), "buycraft");
        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .description(Text.of(i18n.get("usage_sponge_listing")))
                .executor(new ListPackagesCmd(this))
                .build(), configuration.getBuyCommandName());
    }

    @Listener
    public void onGameStoppingServerEvent(GameStoppingServerEvent event) {
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

    private CommandSpec buildCommands() {
        CommandSpec refresh = CommandSpec.builder()
                .description(Text.of(i18n.get("usage_refresh")))
                .permission("buycraft.admin")
                .executor(new RefreshCmd(this))
                .build();
        CommandSpec secret = CommandSpec.builder()
                .description(Text.of(i18n.get("usage_secret")))
                .permission("buycraft.admin")
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("secret"))))
                .executor(new SecretCmd(this))
                .build();
        CommandSpec report = CommandSpec.builder()
                .description(Text.of(i18n.get("usage_report")))
                .executor(new ReportCmd(this))
                .permission("buycraft.admin")
                .build();
        CommandSpec info = CommandSpec.builder()
                .description(Text.of(i18n.get("usage_information")))
                .executor(new InfoCmd(this))
                .build();
        CommandSpec forcecheck = CommandSpec.builder()
                .description(Text.of(i18n.get("usage_forcecheck")))
                .executor(new ForceCheckCmd(this))
                .permission("buycraft.admin")
                .build();
        CommandSpec coupon = buildCouponCommands();
        return CommandSpec.builder()
                .description(Text.of("Main command for the Buycraft plugin."))
                .child(report, "report")
                .child(secret, "secret")
                .child(refresh, "refresh")
                .child(info, "info")
                .child(forcecheck, "forcecheck")
                .child(coupon, "coupon")
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

    public void updateInformation(ApiClient client) throws IOException, ApiException {
        serverInformation = client.getServerInformation();

        if (!configuration.isBungeeCord() && Sponge.getServer().getOnlineMode() != serverInformation.getAccount().isOnlineMode()) {
            getLogger().warn("Your server and webstore online mode settings are mismatched. Unless you are using" +
                    " a proxy and server combination (such as BungeeCord/Spigot or LilyPad/Connect) that corrects UUIDs, then" +
                    " you may experience issues with packages not applying.");
            getLogger().warn("If you have verified that your set up is correct, you can suppress this message by setting " +
                    "is-bungeecord=true in your BuycraftX config.properties.");
        }
    }

}
