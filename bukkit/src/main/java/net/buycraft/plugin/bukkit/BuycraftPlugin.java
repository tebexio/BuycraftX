package net.buycraft.plugin.bukkit;

import com.bugsnag.Client;
import lombok.Getter;
import lombok.Setter;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.bukkit.command.*;
import net.buycraft.plugin.bukkit.gui.CategoryViewGUI;
import net.buycraft.plugin.bukkit.gui.GUIUtil;
import net.buycraft.plugin.bukkit.gui.ViewCategoriesGUI;
import net.buycraft.plugin.bukkit.logging.BugsnagGlobalLoggingHandler;
import net.buycraft.plugin.bukkit.logging.BugsnagLoggingHandler;
import net.buycraft.plugin.bukkit.logging.BugsnagNilLogger;
import net.buycraft.plugin.bukkit.signs.buynow.BuyNowSignListener;
import net.buycraft.plugin.bukkit.signs.buynow.BuyNowSignStorage;
import net.buycraft.plugin.bukkit.signs.purchases.RecentPurchaseSignListener;
import net.buycraft.plugin.bukkit.signs.purchases.RecentPurchaseSignStorage;
import net.buycraft.plugin.bukkit.tasks.ListingUpdateTask;
import net.buycraft.plugin.bukkit.tasks.SignUpdater;
import net.buycraft.plugin.bukkit.util.AnalyticsUtil;
import net.buycraft.plugin.bukkit.util.VersionCheck;
import net.buycraft.plugin.client.ApiClient;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.client.ProductionApiClient;
import net.buycraft.plugin.config.BuycraftConfiguration;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.execution.DuePlayerFetcher;
import net.buycraft.plugin.execution.placeholder.NamePlaceholder;
import net.buycraft.plugin.execution.placeholder.PlaceholderManager;
import net.buycraft.plugin.execution.placeholder.UuidPlaceholder;
import net.buycraft.plugin.execution.strategy.CommandExecutor;
import net.buycraft.plugin.execution.strategy.QueuedCommandExecutor;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BuycraftPlugin extends JavaPlugin {
    @Getter
    @Setter
    private ApiClient apiClient;
    @Getter
    private DuePlayerFetcher duePlayerFetcher;
    @Getter
    private final PlaceholderManager placeholderManager = new PlaceholderManager();
    @Getter
    private final BuycraftConfiguration configuration = new BuycraftConfiguration();
    @Getter
    private ListingUpdateTask listingUpdateTask;
    @Getter
    private ServerInformation serverInformation;
    @Getter
    private CategoryViewGUI categoryViewGUI;
    @Getter
    private ViewCategoriesGUI viewCategoriesGUI;
    @Getter
    private RecentPurchaseSignStorage recentPurchaseSignStorage;
    @Getter
    private OkHttpClient httpClient;
    @Getter
    private BuyNowSignStorage buyNowSignStorage;
    @Getter
    private BuyNowSignListener buyNowSignListener;
    @Getter
    private IBuycraftPlatform platform;
    @Getter
    private CommandExecutor commandExecutor;

    @Override
    public void onEnable() {
        // Pre-initialization.
        GUIUtil.setPlugin(this);
        platform = new BukkitBuycraftPlatform(this);

        // Initialize configuration.
        getDataFolder().mkdir();
        try {
            Path configPath = getDataFolder().toPath().resolve("config.properties");
            if (!configPath.toFile().exists()) {
                configuration.fillDefaults();
                configuration.save(configPath);
            } else {
                configuration.load(getDataFolder().toPath().resolve("config.properties"));
                configuration.fillDefaults();
            }
        } catch (IOException e) {
            getLogger().log(Level.INFO, "Unable to load configuration! The plugin will disable itself now.", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize API client.
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .cache(new Cache(new File(getDataFolder(), "cache"), 1024 * 1024 * 10))
                .build();
        String serverKey = configuration.getServerKey();
        if (serverKey == null || serverKey.equals("INVALID")) {
            getLogger().info("Looks like this is a fresh setup. Get started by using 'buycraft secret <key>' in the console.");
        } else {
            getLogger().info("Validating your server key...");
            ApiClient client = new ProductionApiClient(configuration.getServerKey(), httpClient);
            try {
                updateInformation(client);
            } catch (IOException | ApiException e) {
                getLogger().severe(String.format("We can't check if your server can connect to Buycraft: %s", e.getMessage()));
            }
            apiClient = client;
        }

        // Check for latest version.
        VersionCheck check = new VersionCheck(this, getDescription().getVersion());
        try {
            check.verify();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Can't check for updates", e);
        }
        getServer().getPluginManager().registerEvents(check, this); // out!

        // Initialize placeholders.
        placeholderManager.addPlaceholder(new NamePlaceholder());
        placeholderManager.addPlaceholder(new UuidPlaceholder());

        // Start the essential tasks.
        getServer().getScheduler().runTaskLaterAsynchronously(this, duePlayerFetcher = new DuePlayerFetcher(platform,
                configuration.isVerbose()), 20);
        commandExecutor = new QueuedCommandExecutor(platform);
        getServer().getScheduler().runTaskTimer(this, (Runnable) commandExecutor, 1, 1);

        // Initialize the GUIs.
        viewCategoriesGUI = new ViewCategoriesGUI(this);
        categoryViewGUI = new CategoryViewGUI(this);

        // Update listing.
        listingUpdateTask = new ListingUpdateTask(this);
        if (apiClient != null) {
            getLogger().info("Fetching all server packages...");
            listingUpdateTask.run(); // for a first synchronous run
            getServer().getScheduler().runTaskTimerAsynchronously(this, listingUpdateTask, 20 * 60 * 10, 20 * 60 * 10);

            // Update GUIs too.
            viewCategoriesGUI.update();
            categoryViewGUI.update();
        }

        // Register listener.
        getServer().getPluginManager().registerEvents(new BuycraftListener(this), this);

        // Initialize and register commands.
        BuycraftCommand command = new BuycraftCommand();
        command.getSubcommandMap().put("forcecheck", new ForceCheckSubcommand(this));
        command.getSubcommandMap().put("secret", new SecretSubcommand(this));
        command.getSubcommandMap().put("info", new InformationSubcommand(this));
        command.getSubcommandMap().put("refresh", new RefreshSubcommand(this));
        command.getSubcommandMap().put("signupdate", new SignUpdateSubcommand(this));
        command.getSubcommandMap().put("report", new ReportCommand(this));
        getCommand("buycraft").setExecutor(command);

        // Initialize sign data and listener.
        recentPurchaseSignStorage = new RecentPurchaseSignStorage();
        try {
            recentPurchaseSignStorage.load(getDataFolder().toPath().resolve("purchase_signs.json"));
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Can't load purchase igns, continuing anyway", e);
        }
        getServer().getScheduler().runTaskTimerAsynchronously(this, new SignUpdater(this), 20, 3600 * 15);
        getServer().getPluginManager().registerEvents(new RecentPurchaseSignListener(this), this);

        // Initialize purchase signs.
        buyNowSignStorage = new BuyNowSignStorage();
        try {
            buyNowSignStorage.load(getDataFolder().toPath().resolve("buy_now_signs.json"));
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Can't load buy now signs, continuing anyway", e);
        }
        buyNowSignListener = new BuyNowSignListener(this);
        getServer().getPluginManager().registerEvents(buyNowSignListener, this);

        // Send data to Keen IO
        if (serverInformation != null) {
            getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
                @Override
                public void run() {
                    AnalyticsUtil.postServerInformation(BuycraftPlugin.this);
                }
            }, 0, 20 * TimeUnit.DAYS.toSeconds(1));
        }

        // Set up Bugsnag.
        Client bugsnagClient = new Client("cac4ea0fdbe89b5004d8ab8d5409e594", false);
        bugsnagClient.setAppVersion(getDescription().getVersion());
        bugsnagClient.setLogger(new BugsnagNilLogger());
        Bukkit.getLogger().addHandler(new BugsnagGlobalLoggingHandler(bugsnagClient, this));
        getLogger().addHandler(new BugsnagLoggingHandler(bugsnagClient, this));
    }

    @Override
    public void onDisable() {
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
    }

    public void saveConfiguration() throws IOException {
        Path configPath = getDataFolder().toPath().resolve("config.properties");
        configuration.save(configPath);
    }

    public void updateInformation(ApiClient client) throws IOException, ApiException {
        serverInformation = client.getServerInformation();

        if (!configuration.isBungeeCord() && getServer().getOnlineMode() != serverInformation.getAccount().isOnlineMode()) {
            getLogger().log(Level.WARNING, "Your server and webstore online mode settings are mismatched. Unless you are using" +
                    " a proxy and server combination (such as BungeeCord/Spigot or LilyPad/Connect) that corrects UUIDs, then" +
                    " you may experience issues with packages not applying.");
            getLogger().log(Level.WARNING, "If you are sure you have understood and verified that this has been set up, set " +
                    "is-bungeecord=true in your BuycraftX config.properties.");
        }
    }
}
