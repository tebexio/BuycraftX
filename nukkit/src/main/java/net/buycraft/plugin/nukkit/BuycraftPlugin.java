package net.buycraft.plugin.nukkit;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginBase;
import net.buycraft.plugin.BuyCraftAPI;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.execution.DuePlayerFetcher;
import net.buycraft.plugin.execution.ServerEventSenderTask;
import net.buycraft.plugin.execution.placeholder.NamePlaceholder;
import net.buycraft.plugin.execution.placeholder.PlaceholderManager;
import net.buycraft.plugin.execution.placeholder.XuidPlaceholder;
import net.buycraft.plugin.execution.strategy.CommandExecutor;
import net.buycraft.plugin.execution.strategy.PostCompletedCommandsTask;
import net.buycraft.plugin.execution.strategy.QueuedCommandExecutor;
import net.buycraft.plugin.nukkit.command.*;
import net.buycraft.plugin.nukkit.logging.LoggerUtils;
import net.buycraft.plugin.nukkit.util.VersionCheck;
import net.buycraft.plugin.shared.Setup;
import net.buycraft.plugin.shared.config.BuycraftConfiguration;
import net.buycraft.plugin.shared.config.BuycraftI18n;
import net.buycraft.plugin.shared.tasks.PlayerJoinCheckTask;
import net.buycraft.plugin.shared.util.AnalyticsSend;
import okhttp3.OkHttpClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class BuycraftPlugin extends PluginBase {
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
    private LoggerUtils loggerUtils;
    private BuycraftCommand command;

    @Override
    public void onEnable() {
        // Pre-initialization.
        platform = new NukkitBuycraftPlatform(this);

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
        httpClient = Setup.okhttp(new File(getDataFolder(), "cache"));
        loggerUtils = new LoggerUtils(getLogger());

        // Initialize API client.
        final String serverKey = configuration.getServerKey();
        if (serverKey == null || serverKey.equals("INVALID")) {
            getLogger().info("Looks like this is a fresh setup. Get started by using 'tebex secret <key>' in the console.");
        } else {
            getLogger().info("Validating your server key...");
            final BuyCraftAPI client = BuyCraftAPI.create(configuration.getServerKey(), httpClient);
            try {
                updateInformation(client);
            } catch (IOException e) {
                getLogger().error(String.format("We can't check if your server can connect to Tebex: %s", e.getMessage()));
            }
            apiClient = client;
        }

        // Check for latest version.
        if (configuration.isCheckForUpdates()) {
            final VersionCheck check = new VersionCheck(this, getDescription().getVersion(), configuration.getServerKey());
            try {
                check.verify();
            } catch (IOException e) {
                getLogger().error("Can't check for updates", e);
            }
            getServer().getPluginManager().registerEvents(check, this);
        }

        // Initialize placeholders.
        placeholderManager.addPlaceholder(new NamePlaceholder());
        placeholderManager.addPlaceholder(new XuidPlaceholder());

        // Queueing tasks.
        platform.executeAsyncLater(duePlayerFetcher = new DuePlayerFetcher(platform, configuration.isVerbose()), 1, TimeUnit.SECONDS);
        completedCommandsTask = new PostCompletedCommandsTask(platform);
        commandExecutor = new QueuedCommandExecutor(platform, completedCommandsTask);
        getServer().getScheduler().scheduleDelayedRepeatingTask(this, completedCommandsTask, 20, 20);
        getServer().getScheduler().scheduleDelayedRepeatingTask(this, (Runnable) commandExecutor, 1, 1);
        playerJoinCheckTask = new PlayerJoinCheckTask(platform);
        getServer().getScheduler().scheduleDelayedRepeatingTask(this, playerJoinCheckTask, 1, 1);
        serverEventSenderTask = new ServerEventSenderTask(platform, configuration.isVerbose());
        getServer().getScheduler().scheduleDelayedRepeatingTask(this, serverEventSenderTask, 20 * 60, 20 * 60, true);

        // Register listener.
        getServer().getPluginManager().registerEvents(new BuycraftListener(this), this);

        // Initialize and register commands.
        command = new BuycraftCommand(this);
        command.getSubcommandMap().put("forcecheck", new ForceCheckSubcommand(this));
        command.getSubcommandMap().put("secret", new SecretSubcommand(this));
        command.getSubcommandMap().put("info", new InformationSubcommand(this));
        command.getSubcommandMap().put("report", new ReportCommand(this));
        command.getSubcommandMap().put("coupon", new CouponSubcommand(this));

        // Send data to Keen IO
        if (serverInformation != null) {
            getServer().getScheduler().scheduleDelayedRepeatingTask(this, () -> {
                try {
                    AnalyticsSend.postServerInformation(httpClient, serverKey, platform, false);
                } catch (IOException e) {
                    getLogger().warning("Can't send analytics", e);
                }
            }, 0, 20 * 60 * 60 * 24);
        }
    }

    @Override
    public void onDisable() {
        if (completedCommandsTask != null) {
            completedCommandsTask.flush();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return this.command.onCommand(sender, command, label, args);
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

    public ServerEventSenderTask getServerEventSenderTask() {
        return serverEventSenderTask;
    }

    public LoggerUtils getLoggerUtils() {
        return this.loggerUtils;
    }
}
