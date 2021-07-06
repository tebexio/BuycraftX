package net.buycraft.plugin.velocity;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.buycraft.plugin.BuyCraftAPI;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.data.ServerEvent;
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
import net.buycraft.plugin.shared.tasks.PlayerJoinCheckTask;
import net.buycraft.plugin.shared.util.AnalyticsSend;
import net.buycraft.plugin.velocity.command.*;
import net.buycraft.plugin.velocity.util.VersionCheck;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Date;
import java.util.concurrent.*;

@Plugin(id = "buycraft", name = "BuycraftX", authors = {"Tebex", "theminecoder"}, version = BuycraftPlugin.MAGIC_VERSION)
public class BuycraftPlugin {
    static final String MAGIC_VERSION = "SET_BY_MAGIC";

    private final ProxyServer server;
    private final Logger logger;
    private final File dataFolder;

    private final PlaceholderManager placeholderManager = new PlaceholderManager();
    private final BuycraftConfiguration configuration = new BuycraftConfiguration();
    private BuyCraftAPI apiClient;
    private DuePlayerFetcher duePlayerFetcher;
    private ServerInformation serverInformation;
    private OkHttpClient httpClient;
    private IBuycraftPlatform platform;
    private CommandExecutor commandExecutor;
    private BuycraftI18n i18n;
    private PostCompletedCommandsTask completedCommandsTask;
    private PlayerJoinCheckTask playerJoinCheckTask;
    private ServerEventSenderTask serverEventSenderTask;
    private ExecutorService service;

    @Inject
    public BuycraftPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataFolder) {
        this.server = server;
        this.logger = logger;
        this.dataFolder = dataFolder.toFile();
    }

    @Subscribe
    public void onEnable(ProxyInitializeEvent event) {
        // Pre-initialization.
        platform = new VelocityBuycraftPlatform(this);
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
            httpClient = runSyncTaskOnAsyncThread(() -> Setup.okhttpBuilder()
                    .cache(new Cache(new File(getDataFolder(), "cache"), 1024 * 1024 * 10))
                    .connectionPool(new ConnectionPool())
                    .dispatcher(new Dispatcher(getExecutorService()))
                    .build());
        } catch (ExecutionException e) {
            // We must bail early
            throw new RuntimeException("Can't create HTTP client", e);
        }

//        if (configuration.isPushCommandsEnabled()) { //TODO Find velocity netty injection path
//            try {
//                BungeeNettyChannelInjector.inject(this);
//            } catch (Throwable t) {
//                t.printStackTrace();
//            }
//        }

        // Initialize API client.
        final String serverKey = configuration.getServerKey();
        if (serverKey == null || serverKey.equals("INVALID")) {
            getLogger().info("Looks like this is a fresh setup. Get started by using 'tebex secret <key>' in the console.");
        } else {
            getLogger().info("Validating your server key...");
            final BuyCraftAPI client = BuyCraftAPI.create(configuration.getServerKey(), httpClient);
            // Hack due to SecurityManager shenanigans.
            try {
                runSyncTaskOnAsyncThread((Callable<Void>) () -> {
                    updateInformation(client);
                    return null;
                });
            } catch (ExecutionException e) {
                getLogger().error(String.format("We can't check if your server can connect to Tebex: %s", e.getMessage()));
            }
            apiClient = client;
        }

        // Check for latest version.
        if (configuration.isCheckForUpdates()) {
            final VersionCheck check = new VersionCheck(this, platform.getPluginVersion(), configuration.getServerKey());
            try {
                runSyncTaskOnAsyncThread((Callable<Void>) () -> {
                    check.verify();
                    return null;
                });
            } catch (ExecutionException e) {
                getLogger().error("Can't check for updates", e);
            }
            getServer().getEventManager().register(this, check);
        }

        // Initialize placeholders.
        placeholderManager.addPlaceholder(new NamePlaceholder());
        placeholderManager.addPlaceholder(new UuidPlaceholder());

        // Queueing tasks.
        getServer().getScheduler()
                .buildTask(this, duePlayerFetcher = new DuePlayerFetcher(platform, configuration.isVerbose()))
                .delay(1, TimeUnit.SECONDS)
                .schedule();
        completedCommandsTask = new PostCompletedCommandsTask(platform);
        commandExecutor = new QueuedCommandExecutor(platform, completedCommandsTask);
        getServer().getScheduler()
                .buildTask(this, completedCommandsTask)
                .delay(1, TimeUnit.SECONDS)
                .repeat(1, TimeUnit.SECONDS)
                .schedule();
        getServer().getScheduler()
                .buildTask(this, (Runnable) commandExecutor)
                .delay(50, TimeUnit.MILLISECONDS)
                .repeat(1, TimeUnit.MILLISECONDS)
                .schedule();
        playerJoinCheckTask = new PlayerJoinCheckTask(platform);
        getServer().getScheduler()
                .buildTask(this, playerJoinCheckTask)
                .delay(1, TimeUnit.SECONDS)
                .repeat(1, TimeUnit.SECONDS)
                .schedule();
        serverEventSenderTask = new ServerEventSenderTask(platform, configuration.isVerbose());
        getServer().getScheduler()
                .buildTask(this, serverEventSenderTask)
                .delay(1, TimeUnit.MINUTES)
                .repeat(1, TimeUnit.MINUTES)
                .schedule();

        // Initialize and register commands.
        BuycraftCommand command = new BuycraftCommand(this);
        command.getSubcommandMap().put("forcecheck", new ForceCheckSubcommand(this));
        command.getSubcommandMap().put("secret", new SecretSubcommand(this));
        command.getSubcommandMap().put("info", new InformationSubcommand(this));
        command.getSubcommandMap().put("report", new ReportCommand(this));
        command.getSubcommandMap().put("coupon", new CouponSubcommand(this));
        server.getCommandManager().register(server.getCommandManager().metaBuilder("buycraft").aliases("tebex").build(), command);

        // Send data to Keen IO
        if (serverInformation != null) {
            getServer().getScheduler().buildTask(this, () -> {
                try {
                    AnalyticsSend.postServerInformation(httpClient, serverKey, platform, getServer().getConfiguration().isOnlineMode());
                } catch (IOException e) {
                    getLogger().warn("Can't send analytics", e);
                }
            }).repeat(1, TimeUnit.DAYS).schedule();
        }
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent event) {
        if (completedCommandsTask != null) {
            completedCommandsTask.flush();
        }
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        if (getApiClient() == null) {
            return;
        }

        serverEventSenderTask.queueEvent(new ServerEvent(
                event.getPlayer().getUniqueId().toString().replace("-", ""),
                event.getPlayer().getUsername(),
                event.getPlayer().getRemoteAddress().getAddress().getHostAddress(),
                ServerEvent.JOIN_EVENT,
                new Date()
        ));

        QueuedPlayer qp = getDuePlayerFetcher().fetchAndRemoveDuePlayer(event.getPlayer().getUsername());
        if (qp != null) {
            getPlayerJoinCheckTask().queue(qp);
        }
    }

    @Subscribe
    public void onQuit(DisconnectEvent event) {
        if (getApiClient() == null) {
            return;
        }

        serverEventSenderTask.queueEvent(new ServerEvent(
                event.getPlayer().getUniqueId().toString().replace("-", ""),
                event.getPlayer().getUsername(),
                event.getPlayer().getRemoteAddress().getAddress().getHostAddress(),
                ServerEvent.LEAVE_EVENT,
                new Date()
        ));
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    @Deprecated
    public ExecutorService getExecutorService() {
        if (service == null) {
            ThreadGroup pluginThreadGroup = new ThreadGroup("BuycraftX");
            service = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                    .setNameFormat("BuycraftX Pool Thread #%1$d")
                    .setThreadFactory(r -> new Thread(pluginThreadGroup, r))
                    .build());
        }
        return service;
    }

    private <T> T runSyncTaskOnAsyncThread(Callable<T> runnable) throws ExecutionException {
        CompletableFuture<T> future = new CompletableFuture<>();
        try {
            server.getScheduler().buildTask(this, () -> {
                try {
                    future.complete(runnable.call());
                } catch (Throwable e) {
                    future.completeExceptionally(e);
                }
            }).schedule();
            return future.get();
        } catch (InterruptedException e) {
            throw new ExecutionException("interrupted", e);
        }
    }

    public void saveConfiguration() throws IOException {
        Path configPath = getDataFolder().toPath().resolve("config.properties");
        configuration.save(configPath);
    }

    public void updateInformation(BuyCraftAPI client) throws IOException {
        serverInformation = client.getServerInformation().execute().body();
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

    public ServerInformation getServerInformation() {
        return this.serverInformation;
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

    public BuycraftI18n getI18n() {
        return this.i18n;
    }

    public PlayerJoinCheckTask getPlayerJoinCheckTask() {
        return this.playerJoinCheckTask;
    }
}
