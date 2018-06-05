package net.buycraft.plugin.nukkit;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginBase;
import com.bugsnag.Bugsnag;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class BuycraftPlugin extends PluginBase {
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
    @Getter
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

        // Set up Bugsnag.
        Bugsnag bugsnag = Setup.bugsnagClient(httpClient, "bukkit", getDescription().getVersion(),
                getServer().getNukkitVersion(), this::getServerInformation);
        loggerUtils = new LoggerUtils(getLogger(), bugsnag);

        // Initialize API client.
        final String serverKey = configuration.getServerKey();
        if (serverKey == null || serverKey.equals("INVALID")) {
            getLogger().info("Looks like this is a fresh setup. Get started by using 'buycraft secret <key>' in the console.");
        } else {
            getLogger().info("Validating your server key...");
            final ApiClient client = new ProductionApiClient(configuration.getServerKey(), httpClient);
            try {
                updateInformation(client);
            } catch (IOException | ApiException e) {
                getLogger().error(String.format("We can't check if your server can connect to Buycraft: %s", e.getMessage()));
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
            getServer().getScheduler().scheduleDelayedRepeatingTask(this, new Runnable() {
                @Override
                public void run() {
                    try {
                        AnalyticsSend.postServerInformation(httpClient, serverKey, platform, false);
                    } catch (IOException e) {
                        getLogger().warning("Can't send analytics", e);
                    }
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

    public void updateInformation(ApiClient client) throws IOException, ApiException {
        serverInformation = client.getServerInformation();
    }
}
