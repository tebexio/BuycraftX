package io.tebex.plugin;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.util.ProxyVersion;
import dev.dejvokep.boostedyaml.YamlDocument;
import io.tebex.plugin.event.JoinListener;
import io.tebex.plugin.manager.CommandManager;
import io.tebex.sdk.SDK;
import io.tebex.sdk.Tebex;
import io.tebex.sdk.obj.Category;
import io.tebex.sdk.placeholder.PlaceholderManager;
import io.tebex.sdk.platform.Platform;
import io.tebex.sdk.platform.PlatformTelemetry;
import io.tebex.sdk.platform.PlatformType;
import io.tebex.sdk.platform.config.ProxyPlatformConfig;
import io.tebex.sdk.request.response.ServerInformation;
import io.tebex.sdk.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Plugin(
        id = "tebex",
        name = "Tebex",
        version = "@VERSION@",
        description = "The Velocity plugin for Tebex.",
        url = "https://tebex.io",
        authors = {"Tebex"}
)
public class TebexPlugin implements Platform {
    private SDK sdk;
    private ProxyPlatformConfig config;
    private boolean setup;
    private PlaceholderManager placeholderManager;
    private Map<Object, Integer> queuedPlayers;
    private YamlDocument configYaml;

    private ServerInformation storeInformation;
    private List<Category> storeCategories;

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDirectory;

    @Inject
    public TebexPlugin(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    @Subscribe
    public void onEnable(ProxyInitializeEvent event) {
        // Bind SDK.
        Tebex.init(this);

        try {
            // Load the platform config file.
            configYaml = initPlatformConfig();
            config = loadProxyPlatformConfig(configYaml);
        } catch (IOException e) {
            log(Level.WARNING, "Failed to load config: " + e.getMessage());
//            proxy.getPluginManager().unregisterListeners(this);
            return;
        }

        // Initialise Managers.
        new CommandManager(this).register();

        // Initialise SDK.
        sdk = new SDK(this, config.getSecretKey());
        placeholderManager = new PlaceholderManager();
        queuedPlayers = Maps.newConcurrentMap();
        storeCategories = new ArrayList<>();

        placeholderManager.registerDefaults();

        proxy.getEventManager().register(this, new JoinListener(this));

        // Migrate the config from BuycraftX.
        migrateConfig();

        // Initialise the platform.
        init();

        proxy.getScheduler()
                .buildTask(this, () -> {
                    getSDK().getServerInformation().thenAccept(information -> storeInformation = information);
                    getSDK().getListing().thenAccept(listing -> storeCategories = listing);
                })
                .repeat(5, TimeUnit.MINUTES)
                .delay(0, TimeUnit.MINUTES)
                .schedule();
    }

    public List<Category> getStoreCategories() {
        return storeCategories;
    }

    public ServerInformation getStoreInformation() {
        return storeInformation;
    }

    public void migrateConfig() {
        File oldPluginDir = new File("plugins/BuycraftX");
        if (!oldPluginDir.exists()) return;

        File oldConfigFile = new File(oldPluginDir, "config.properties");
        if(!oldConfigFile.exists()) return;

        info("You're running the legacy BuycraftX plugin. Attempting to migrate..");

        try {
            // Load old properties
            Properties properties = new Properties();
            properties.load(Files.newInputStream(oldConfigFile.toPath()));

            String secretKey = properties.getProperty("server-key", null);
            secretKey = !Objects.equals(secretKey, "INVALID") ? secretKey : null;

            if(secretKey != null) {
                // Migrate their existing config.
                configYaml.set("check-for-updates", properties.getOrDefault("check-for-updates", null));
                configYaml.set("verbose", properties.getOrDefault("verbose", false));

                configYaml.set("server.secret-key", secretKey);

                // Save new config
                configYaml.save();

                config = loadProxyPlatformConfig(configYaml);

                sdk = new SDK(this, config.getSecretKey());

                info("Successfully migrated your config from BuycraftX.");
            }

            // If BuycraftX is installed, delete it.
            boolean legacyPluginEnabled = getProxy().getPluginManager().getPlugin("BuycraftX").isPresent();

            boolean deletedLegacyPluginDir = FileUtils.deleteDirectory(oldPluginDir);
            if(legacyPluginEnabled || !deletedLegacyPluginDir) {
                warning("Please manually delete the BuycraftX files in your /plugins folder to avoid conflicts.");
            }
        } catch (IOException e) {
            warning("Failed to migrate config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public PlatformType getType() {
        return PlatformType.VELOCITY;
    }

    @Override
    public SDK getSDK() {
        return sdk;
    }

    @Override
    public File getDirectory() {
        return dataDirectory.toFile();
    }

    @Override
    public boolean isSetup() {
        return setup;
    }

    @Override
    public void setSetup(boolean setup) {
        this.setup = setup;
    }

    @Override
    public boolean isOnlineMode() {
        return proxy.getConfiguration().isOnlineMode();
    }

    @Override
    public void configure() {
        setup = true;
        performCheck();
        sdk.sendTelemetry();
    }

    @Override
    public void halt() {
        setup = false;
    }


    @Override
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    @Override
    public Map<Object, Integer> getQueuedPlayers() {
        return queuedPlayers;
    }

    @Override
    public void dispatchCommand(String command) {
        proxy.getCommandManager().executeAsync(proxy.getConsoleCommandSource(), command);
    }

    @Override
    public void executeAsync(Runnable runnable) {
        proxy.getScheduler()
                .buildTask(this, runnable)
                .schedule();
    }

    @Override
    public void executeAsyncLater(Runnable runnable, long time, TimeUnit unit) {
        proxy.getScheduler()
                .buildTask(this, runnable)
                .delay(time, unit)
                .schedule();
    }

    @Override
    public void executeBlocking(Runnable runnable) {
        // Velocity has no concept of "blocking"
        executeAsync(runnable);
    }

    @Override
    public void executeBlockingLater(Runnable runnable, long time, TimeUnit unit) {
        // Velocity has no concept of "blocking"
        executeAsyncLater(runnable, time, unit);
    }


    private Optional<Player> getPlayer(Object player) {
        if(player == null) return Optional.empty();

        if (isOnlineMode()) {
            return proxy.getPlayer((UUID) player);
        }

        return proxy.getPlayer((String) player);
    }

    @Override
    public boolean isPlayerOnline(Object player) {
        return getPlayer(player).isPresent();
    }

    @Override
    public int getFreeSlots(Object player) {
        // Bungee has no concept of an inventory
        return 0;
    }

    @Override
    public String getVersion() {
        return "@VERSION@";
    }

    @Override
    public void log(Level level, String message) {
        logger.log(level, message);
    }

    @Override
    public ProxyPlatformConfig getPlatformConfig() {
        return config;
    }

    @Override
    public PlatformTelemetry getTelemetry() {
        ProxyVersion proxyVersion = proxy.getVersion();
        String serverVersion = proxyVersion.getVersion();

        Pattern pattern = Pattern.compile("MC: (\\d+\\.\\d+\\.\\d+)");
        Matcher matcher = pattern.matcher(serverVersion);
        if (matcher.find()) {
            serverVersion = matcher.group(1);
        }

        return new PlatformTelemetry(
                getVersion(),
                proxyVersion.getName(),
                serverVersion,
                System.getProperty("java.version"),
                System.getProperty("os.arch"),
                proxy.getConfiguration().isOnlineMode()
        );
    }
}
