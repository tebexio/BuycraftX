package net.buycraft.plugin.bungeecord;

import com.bugsnag.Client;
import lombok.Getter;
import lombok.Setter;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.bungeecord.command.*;
import net.buycraft.plugin.bungeecord.logging.BugsnagGlobalLoggingHandler;
import net.buycraft.plugin.bungeecord.logging.BugsnagLoggingHandler;
import net.buycraft.plugin.bungeecord.logging.BugsnagNilLogger;
import net.buycraft.plugin.bungeecord.util.AnalyticsUtil;
import net.buycraft.plugin.bungeecord.util.VersionCheck;
import net.buycraft.plugin.client.ApiClient;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.client.ProductionApiClient;
import net.buycraft.plugin.config.BuycraftConfiguration;
import net.buycraft.plugin.config.BuycraftI18n;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.data.responses.Version;
import net.buycraft.plugin.execution.DuePlayerFetcher;
import net.buycraft.plugin.execution.placeholder.NamePlaceholder;
import net.buycraft.plugin.execution.placeholder.PlaceholderManager;
import net.buycraft.plugin.execution.placeholder.UuidPlaceholder;
import net.buycraft.plugin.execution.strategy.CommandExecutor;
import net.buycraft.plugin.execution.strategy.QueuedCommandExecutor;
import net.buycraft.plugin.util.Ipv4PreferDns;
import net.buycraft.plugin.util.VersionUtil;
import net.md_5.bungee.api.plugin.Plugin;
import okhttp3.Cache;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BuycraftPlugin extends Plugin {
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
    private ServerInformation serverInformation;
    @Getter
    private OkHttpClient httpClient;
    @Getter
    private IBuycraftPlatform platform;
    @Getter
    private CommandExecutor commandExecutor;
    @Getter
    private BuycraftI18n i18n;

    @Override
    public void onEnable() {
        // Pre-initialization.
        platform = new BungeeCordBuycraftPlatform(this);

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
            throw new RuntimeException("Unable to load configuration!", e);
        }

        i18n = configuration.createI18n();

        // Initialize API client.
        // This has to be done partially async due to the SecurityManager.
        FutureTask<Cache> cacheFutureTask = new FutureTask<>(new Callable<Cache>() {
            @Override
            public Cache call() throws Exception {
                return new Cache(new File(getDataFolder(), "cache"), 1024 * 1024 * 10);
            }
        });
        getProxy().getScheduler().runAsync(this, cacheFutureTask);
        Cache cache;
        try {
            cache = cacheFutureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            getLogger().log(Level.SEVERE, "Can't create cache", e);
            cache = null;
        }
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .dispatcher(new Dispatcher(getExecutorService()))
                .cache(cache)
                .dns(new Ipv4PreferDns())
                .build();
        String serverKey = configuration.getServerKey();
        if (serverKey == null || serverKey.equals("INVALID")) {
            getLogger().info("Looks like this is a fresh setup. Get started by using 'buycraft secret <key>' in the console.");
        } else {
            getLogger().info("Validating your server key...");
            final ApiClient client = new ProductionApiClient(configuration.getServerKey(), httpClient);
            // Hack due to SecurityManager shenanigans.
            FutureTask<Void> hackTask = new FutureTask<>(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    updateInformation(client);
                    return null;
                }
            });
            getProxy().getScheduler().runAsync(this, hackTask);
            try {
                hackTask.get();
            } catch (InterruptedException | ExecutionException e) {
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
        getProxy().getPluginManager().registerListener(this, check);

        // Initialize placeholders.
        placeholderManager.addPlaceholder(new NamePlaceholder());
        placeholderManager.addPlaceholder(new UuidPlaceholder());

        // Queueing tasks.
        getProxy().getScheduler().schedule(this, duePlayerFetcher = new DuePlayerFetcher(platform, configuration.isVerbose()), 1, TimeUnit.SECONDS);
        commandExecutor = new QueuedCommandExecutor(platform);
        getProxy().getScheduler().schedule(this, (Runnable) commandExecutor, 50, 50, TimeUnit.MILLISECONDS);

        // Register listener.
        getProxy().getPluginManager().registerListener(this, new BuycraftListener(this));

        // Initialize and register commands.
        BuycraftCommand command = new BuycraftCommand(this);
        command.getSubcommandMap().put("forcecheck", new ForceCheckSubcommand(this));
        command.getSubcommandMap().put("secret", new SecretSubcommand(this));
        command.getSubcommandMap().put("info", new InformationSubcommand(this));
        command.getSubcommandMap().put("report", new ReportCommand(this));
        getProxy().getPluginManager().registerCommand(this, command);

        // Send data to Keen IO
        if (serverInformation != null) {
            getProxy().getScheduler().schedule(this, new Runnable() {
                @Override
                public void run() {
                    AnalyticsUtil.postServerInformation(BuycraftPlugin.this);
                }
            }, 0, 1, TimeUnit.DAYS);
        }

        // Set up Bugsnag.
        // Hack due to SecurityManager shenanigans.
        FutureTask<Client> clientInit = new FutureTask<>(new Callable<Client>() {
            @Override
            public Client call() throws Exception {
                return new Client("cac4ea0fdbe89b5004d8ab8d5409e594", false);
            }
        });
        getProxy().getScheduler().runAsync(this, clientInit);
        try {
            Client bugsnagClient = clientInit.get();
            bugsnagClient.setAppVersion(getDescription().getVersion());
            bugsnagClient.setLogger(new BugsnagNilLogger());
            getProxy().getLogger().addHandler(new BugsnagGlobalLoggingHandler(bugsnagClient, this));
            getLogger().addHandler(new BugsnagLoggingHandler(bugsnagClient, this));
        } catch (InterruptedException | ExecutionException e) {
            getLogger().log(Level.SEVERE, "Unable to initialize Bugsnag", e);
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
