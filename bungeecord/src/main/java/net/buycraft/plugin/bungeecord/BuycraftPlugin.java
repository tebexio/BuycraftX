package net.buycraft.plugin.bungeecord;

import com.bugsnag.Bugsnag;
import com.google.common.base.Supplier;
import lombok.Getter;
import lombok.Setter;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.bungeecord.command.*;
import net.buycraft.plugin.bungeecord.util.VersionCheck;
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
import net.buycraft.plugin.shared.logging.BugsnagHandler;
import net.buycraft.plugin.shared.tasks.PlayerJoinCheckTask;
import net.buycraft.plugin.shared.util.AnalyticsSend;
import net.md_5.bungee.api.plugin.Plugin;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BuycraftPlugin extends Plugin {
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
    private ServerInformation serverInformation;
    @Getter
    private OkHttpClient httpClient;
    @Getter
    private IBuycraftPlatform platform;
    @Getter
    private CommandExecutor commandExecutor;
    @Getter
    private BuycraftI18n i18n;
    private PostCompletedCommandsTask completedCommandsTask;
    @Getter
    private PlayerJoinCheckTask playerJoinCheckTask;

    @Override
    public void onEnable() {
        // Pre-initialization.
        platform = new BungeeCordBuycraftPlatform(this);

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
            throw new RuntimeException("Unable to load configuration", e);
        }

        i18n = configuration.createI18n();

        // This has to be done in a different thread due to the SecurityManager.
        try {
            httpClient = runTaskToAppeaseBungeeSecurityManager(new Callable<OkHttpClient>() {
                @Override
                public OkHttpClient call() throws Exception {
                    return Setup.okhttpBuilder()
                            .cache(new Cache(new File(getDataFolder(), "cache"), 1024 * 1024 * 10))
                            .connectionPool(new ConnectionPool())
                            .dispatcher(new Dispatcher(getExecutorService()))
                            .build();
                }
            });
        } catch (ExecutionException e) {
            // We must bail early
            throw new RuntimeException("Can't create HTTP client", e);
        }

        // Set up Bugsnag.
        // Hack due to SecurityManager shenanigans.
        try {
            Bugsnag bugsnagClient = runTaskToAppeaseBungeeSecurityManager(new Callable<Bugsnag>() {
                @Override
                public Bugsnag call() throws Exception {
                    return Setup.bugsnagClient(httpClient, "bungeecord", getDescription().getVersion(),
                            getProxy().getVersion(), new Supplier<ServerInformation>() {
                                @Override
                                public ServerInformation get() {
                                    return getServerInformation();
                                }
                            });
                }
            });
            getProxy().getLogger().addHandler(new BugsnagHandler(bugsnagClient));
        } catch (ExecutionException e) {
            getLogger().log(Level.SEVERE, "Unable to initialize Bugsnag", e);
        }

        // Initialize API client.
        final String serverKey = configuration.getServerKey();
        if (serverKey == null || serverKey.equals("INVALID")) {
            getLogger().info("Looks like this is a fresh setup. Get started by using 'buycraft secret <key>' in the console.");
        } else {
            getLogger().info("Validating your server key...");
            final ApiClient client = new ProductionApiClient(configuration.getServerKey(), httpClient);
            // Hack due to SecurityManager shenanigans.
            try {
                runTaskToAppeaseBungeeSecurityManager(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        updateInformation(client);
                        return null;
                    }
                });
            } catch (ExecutionException e) {
                getLogger().severe(String.format("We can't check if your server can connect to Buycraft: %s", e.getMessage()));
            }
            apiClient = client;
        }

        // Check for latest version.
        if (configuration.isCheckForUpdates()) {
            final VersionCheck check = new VersionCheck(this, getDescription().getVersion(), configuration.getServerKey());
            try {
                runTaskToAppeaseBungeeSecurityManager(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        check.verify();
                        return null;
                    }
                });
            } catch (ExecutionException e) {
                getLogger().log(Level.SEVERE, "Can't check for updates", e);
            }
            getProxy().getPluginManager().registerListener(this, check);
        }

        // Initialize placeholders.
        placeholderManager.addPlaceholder(new NamePlaceholder());
        placeholderManager.addPlaceholder(new UuidPlaceholder());

        // Queueing tasks.
        getProxy().getScheduler().schedule(this, duePlayerFetcher = new DuePlayerFetcher(platform, configuration.isVerbose()), 1, TimeUnit.SECONDS);
        completedCommandsTask = new PostCompletedCommandsTask(platform);
        commandExecutor = new QueuedCommandExecutor(platform, completedCommandsTask);
        getProxy().getScheduler().schedule(this, completedCommandsTask, 1, 1, TimeUnit.SECONDS);
        getProxy().getScheduler().schedule(this, (Runnable) commandExecutor, 50, 50, TimeUnit.MILLISECONDS);
        playerJoinCheckTask = new PlayerJoinCheckTask(platform);
        getProxy().getScheduler().schedule(this, playerJoinCheckTask, 1, 1, TimeUnit.SECONDS);

        // Register listener.
        getProxy().getPluginManager().registerListener(this, new BuycraftListener(this));

        // Initialize and register commands.
        BuycraftCommand command = new BuycraftCommand(this);
        command.getSubcommandMap().put("forcecheck", new ForceCheckSubcommand(this));
        command.getSubcommandMap().put("secret", new SecretSubcommand(this));
        command.getSubcommandMap().put("info", new InformationSubcommand(this));
        command.getSubcommandMap().put("report", new ReportCommand(this));
        command.getSubcommandMap().put("coupon", new CouponSubcommand(this));
        getProxy().getPluginManager().registerCommand(this, command);

        // Send data to Keen IO
        if (serverInformation != null) {
            getProxy().getScheduler().schedule(this, new Runnable() {
                @Override
                public void run() {
                    try {
                        AnalyticsSend.postServerInformation(httpClient, serverKey, platform, getProxy().getConfig().isOnlineMode());
                    } catch (IOException e) {
                        getLogger().log(Level.WARNING, "Can't send analytics", e);
                    }
                }
            }, 0, 1, TimeUnit.DAYS);
        }
    }

    @Override
    public void onDisable() {
        if (completedCommandsTask != null) {
            completedCommandsTask.flush();
        }
    }

    private <T> T runTaskToAppeaseBungeeSecurityManager(Callable<T> runnable) throws ExecutionException {
        try {
            return getExecutorService().submit(runnable).get();
        } catch (InterruptedException e) {
            throw new ExecutionException("interrupted", e);
        }
    }

    public void saveConfiguration() throws IOException {
        Path configPath = getDataFolder().toPath().resolve("config.properties");
        configuration.save(configPath);
    }

    public void updateInformation(ApiClient client) throws IOException, ApiException {
        serverInformation = client.getServerInformation();
    }
}
