package net.buycraft.plugin.bukkit;

import com.google.gson.JsonParseException;
import io.netty.channel.Channel;
import net.buycraft.plugin.BuyCraftAPI;
import net.buycraft.plugin.bukkit.command.*;
import net.buycraft.plugin.bukkit.gui.CategoryViewGUI;
import net.buycraft.plugin.bukkit.gui.ViewCategoriesGUI;
import net.buycraft.plugin.bukkit.httplistener.Decoder;
import net.buycraft.plugin.bukkit.httplistener.NettyInjector;
import net.buycraft.plugin.bukkit.signs.buynow.BuyNowSignListener;
import net.buycraft.plugin.bukkit.signs.purchases.RecentPurchaseSignListener;
import net.buycraft.plugin.bukkit.tasks.BuyNowSignUpdater;
import net.buycraft.plugin.bukkit.tasks.GUIUpdateTask;
import net.buycraft.plugin.bukkit.tasks.RecentPurchaseSignUpdateFetcher;
import net.buycraft.plugin.bukkit.util.GUIUtil;
import net.buycraft.plugin.bukkit.util.VersionCheck;
import net.buycraft.plugin.bukkit.util.placeholder.BukkitNamePlaceholder;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.execution.DuePlayerFetcher;
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
import okhttp3.OkHttpClient;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public abstract class BuycraftPluginBase extends JavaPlugin {
    private final PlaceholderManager placeholderManager = new PlaceholderManager();
    private final BuycraftConfiguration configuration = new BuycraftConfiguration();

    private BuyCraftAPI apiClient;
    private DuePlayerFetcher duePlayerFetcher;
    private BukkitTask duePlayerFetcherTask;
    private ListingUpdateTask listingUpdateTask;
    private ServerInformation serverInformation;
    private CategoryViewGUI categoryViewGUI;
    private ViewCategoriesGUI viewCategoriesGUI;
    private RecentPurchaseSignStorage recentPurchaseSignStorage;
    private OkHttpClient httpClient;
    private BuyNowSignStorage buyNowSignStorage;
    private BuyNowSignListener buyNowSignListener;
    private BukkitBuycraftPlatformBase platform;
    private CommandExecutor commandExecutor;
    private BuycraftI18n i18n;
    private BuyNowSignLayout buyNowSignLayout = BuyNowSignLayout.DEFAULT;
    private RecentPurchaseSignLayout recentPurchaseSignLayout = RecentPurchaseSignLayout.DEFAULT;
    private PostCompletedCommandsTask completedCommandsTask;
    private PlayerJoinCheckTask playerJoinCheckTask;
    private Object injector;

    @Override
    public void onEnable() {
        // Pre-initialization.
        GUIUtil.setPlugin(this);
        platform = createBukkitPlatform();

        if (!platform.ensureCompatibleServerVersion()) {
            throw new IllegalStateException("Wrong version of plugin used for server version. Please ensure that you downloaded the correct file.");
        }

        // Initialize configuration.
        getDataFolder().mkdir();
        Path configPath = getDataFolder().toPath().resolve("config.properties");
        try {
            try {
                configuration.load(configPath);
            } catch (NoSuchFileException e) {
                // Save defaults
                configuration.fillDefaults();
                configuration.save(configPath);
            }
        } catch (IOException e) {
            getLogger().log(Level.INFO, "Unable to load configuration! The plugin will disable itself now.", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        i18n = configuration.createI18n();
        httpClient = Setup.okhttp(new File(getDataFolder(), "cache"));

        // Initialize API client.
        final String serverKey = configuration.getServerKey();
        if (serverKey == null || serverKey.equals("INVALID")) {
            getLogger().info("Looks like this is a fresh setup. Get started by using 'tebex secret <key>' in the console.");
        } else {
            getLogger().info("Validating your server key...");
            BuyCraftAPI client = BuyCraftAPI.create(configuration.getServerKey(), httpClient);
            try {
                updateInformation(client);
            } catch (Exception e) {
                getLogger().severe(String.format("We can't check if your server can connect to Tebex: %s", e.getMessage()));
            }
            apiClient = client;
        }

        // Check for latest version.
        if (configuration.isCheckForUpdates()) {
            VersionCheck check = new VersionCheck(this, getDescription().getVersion(), configuration.getServerKey());
            try {
                check.verify();
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Can't check for updates", e);
            }
            getServer().getPluginManager().registerEvents(check, this); // out!
        }

        if (configuration.isPushCommandsEnabled()) {
            injector = new NettyInjector() {
                @Override
                protected void injectChannel(Channel channel) {
                    channel.pipeline().addFirst(new Decoder(BuycraftPluginBase.this));
                }
            };
            ((NettyInjector) injector).inject();
        }

        // Initialize placeholders.
        placeholderManager.addPlaceholder(new BukkitNamePlaceholder());
        placeholderManager.addPlaceholder(new UuidPlaceholder());

        // Start the essential tasks.
        this.duePlayerFetcherTask = getServer().getScheduler().runTaskLaterAsynchronously(this, duePlayerFetcher = new DuePlayerFetcher(platform,
                configuration.isVerbose()), 20);
        completedCommandsTask = new PostCompletedCommandsTask(platform);

        commandExecutor = new QueuedCommandExecutor(platform, completedCommandsTask);
        ((QueuedCommandExecutor) commandExecutor).setRunMaxCommandsBlocking(configuration.getCommandsPerTick());

        getServer().getScheduler().runTaskTimer(this, (Runnable) this.commandExecutor, 1, 1);
        getServer().getScheduler().runTaskTimerAsynchronously(this, completedCommandsTask, 20, 20);
        playerJoinCheckTask = new PlayerJoinCheckTask(platform);
        getServer().getScheduler().runTaskTimer(this, playerJoinCheckTask, 20, 20);

        // Initialize the GUIs.
        viewCategoriesGUI = new ViewCategoriesGUI(this);
        categoryViewGUI = new CategoryViewGUI(this);

        // Update listing.
        listingUpdateTask = new ListingUpdateTask(platform, () -> {
            if (!this.isEnabled()) return;
            Bukkit.getScheduler().runTask(this, new GUIUpdateTask(this));
            Bukkit.getScheduler().runTask(this, new BuyNowSignUpdater(this));
        });

        if (apiClient != null) {
            getLogger().info("Fetching all server packages...");
            try {
                // for a first synchronous run
                listingUpdateTask.run();

                // Update GUIs too.
                viewCategoriesGUI.update();
                categoryViewGUI.update();
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Unable to fetch server packages", e);
            }
        }
        getServer().getScheduler().runTaskTimerAsynchronously(this, listingUpdateTask, 20 * 60 * 10, 20 * 60 * 10);

        // Register listener.
        getServer().getPluginManager().registerEvents(new BuycraftListener(this), this);

        // Initialize and register commands.
        BuycraftCommand command = new BuycraftCommand(this);
        command.getSubcommandMap().put("forcecheck", new ForceCheckSubcommand(this));
        command.getSubcommandMap().put("secret", new SecretSubcommand(this));
        command.getSubcommandMap().put("info", new InformationSubcommand(this));
        command.getSubcommandMap().put("refresh", new RefreshSubcommand(this));
        command.getSubcommandMap().put("signupdate", new SignUpdateSubcommand(this));
        command.getSubcommandMap().put("report", new ReportCommand(this));
        command.getSubcommandMap().put("coupon", new CouponSubcommand(this));
        command.getSubcommandMap().put("sendlink", new SendLinkSubcommand(this));
        getCommand("buycraft").setExecutor(command);

        // Initialize sign layouts.
        try {
            Path signLayoutDirectory = getDataFolder().toPath().resolve("sign_layouts");
            try {
                Files.createDirectory(signLayoutDirectory);
            } catch (FileAlreadyExistsException ignored) {
            }

            Path rpPath = signLayoutDirectory.resolve("recentpurchase.txt");
            Path bnPath = signLayoutDirectory.resolve("buynow.txt");

            try {
                Files.copy(getResource("sign_layouts/recentpurchase.txt"), rpPath);
            } catch (FileAlreadyExistsException ignored) {
            }
            try {
                Files.copy(getResource("sign_layouts/buynow.txt"), bnPath);
            } catch (FileAlreadyExistsException ignored) {
            }

            recentPurchaseSignLayout = new RecentPurchaseSignLayout(Files.readAllLines(rpPath, StandardCharsets.UTF_8));
            buyNowSignLayout = new BuyNowSignLayout(Files.readAllLines(bnPath, StandardCharsets.UTF_8));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Unable to load sign layouts", e);
        }

        // Initialize recent purchase sign data and listener.
        recentPurchaseSignStorage = new RecentPurchaseSignStorage();
        try {
            recentPurchaseSignStorage.load(getDataFolder().toPath().resolve("purchase_signs.json"));
        } catch (IOException | JsonParseException e) {
            getLogger().log(Level.WARNING, "Can't load purchase signs, continuing anyway");
        }
        getServer().getScheduler().runTaskTimerAsynchronously(this, new RecentPurchaseSignUpdateFetcher(this), 20, 3600 * 15);
        getServer().getPluginManager().registerEvents(new RecentPurchaseSignListener(this), this);

        // Initialize purchase signs.
        buyNowSignStorage = new BuyNowSignStorage();
        try {
            buyNowSignStorage.load(getDataFolder().toPath().resolve("buy_now_signs.json"));
        } catch (IOException | JsonParseException e) {
            getLogger().log(Level.WARNING, "Can't load buy now signs, continuing anyway");
        }
        buyNowSignListener = new BuyNowSignListener(this);
        getServer().getPluginManager().registerEvents(buyNowSignListener, this);

        // Send data to Keen IO
        if (serverInformation != null) {
            getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
                String fullPlatformVersion = getServer().getVersion();
                int start = fullPlatformVersion.indexOf("(MC:");
                String pv = fullPlatformVersion.substring(start + 5, fullPlatformVersion.length() - 1);
                try {
                    AnalyticsSend.postServerInformation(httpClient, serverKey, platform, getServer().getOnlineMode());
                } catch (IOException e) {
                    getLogger().log(Level.WARNING, "Can't send analytics", e);
                }
            }, 0, 20 * TimeUnit.DAYS.toSeconds(1));
        }
    }

    @Override
    public void onDisable() {
        if (!platform.ensureCompatibleServerVersion()) return;
        try {
            this.duePlayerFetcherTask.cancel();
        } catch (Exception ignored) {
        }
        try {
            this.duePlayerFetcherTask.cancel();
        } catch (Exception ignored) {
        }
        try {
            recentPurchaseSignStorage.save(getDataFolder().toPath().resolve("purchase_signs.json"));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Can't save purchase signs, continuing anyway");
        }
        try {
            buyNowSignStorage.save(getDataFolder().toPath().resolve("buy_now_signs.json"));
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Can't save buy now signs, continuing anyway", e);
        }
        if (configuration.isPushCommandsEnabled() && injector != null) {
            ((NettyInjector) injector).close();
        }
        completedCommandsTask.flush();
    }

    protected abstract BukkitBuycraftPlatformBase createBukkitPlatform();

    public void saveConfiguration() throws IOException {
        Path configPath = getDataFolder().toPath().resolve("config.properties");
        configuration.save(configPath);
    }

    public void updateInformation(BuyCraftAPI client) throws IOException {
        serverInformation = client.getServerInformation().execute().body();
        if (!configuration.isBungeeCord() && getServer().getOnlineMode() != serverInformation.getAccount().isOnlineMode()) {
            getLogger().log(Level.WARNING, "Your server and webstore online mode settings are mismatched. Unless you are using" +
                    " a proxy and server combination (such as BungeeCord/Spigot or LilyPad/Connect) that corrects UUIDs, then" +
                    " you may experience issues with packages not applying.");
            getLogger().log(Level.WARNING, "If you have verified that your set up is correct, you can suppress this message by" +
                    " setting is-bungeecord=true in your BuycraftX config.properties.");
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

    public CategoryViewGUI getCategoryViewGUI() {
        return this.categoryViewGUI;
    }

    public ViewCategoriesGUI getViewCategoriesGUI() {
        return this.viewCategoriesGUI;
    }

    public RecentPurchaseSignStorage getRecentPurchaseSignStorage() {
        return this.recentPurchaseSignStorage;
    }

    public OkHttpClient getHttpClient() {
        return this.httpClient;
    }

    public BuyNowSignStorage getBuyNowSignStorage() {
        return this.buyNowSignStorage;
    }

    public BuyNowSignListener getBuyNowSignListener() {
        return this.buyNowSignListener;
    }

    public BukkitBuycraftPlatformBase getPlatform() {
        return this.platform;
    }

    public CommandExecutor getCommandExecutor() {
        return this.commandExecutor;
    }

    public BuycraftI18n getI18n() {
        return this.i18n;
    }

    public BuyNowSignLayout getBuyNowSignLayout() {
        return this.buyNowSignLayout;
    }

    public RecentPurchaseSignLayout getRecentPurchaseSignLayout() {
        return this.recentPurchaseSignLayout;
    }

    public PostCompletedCommandsTask getCompletedCommandsTask() {
        return this.completedCommandsTask;
    }

    public PlayerJoinCheckTask getPlayerJoinCheckTask() {
        return this.playerJoinCheckTask;
    }
}
