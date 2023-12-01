package io.tebex.plugin;

import com.google.common.collect.Maps;
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
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TebexPlugin extends Plugin implements Platform {
    private SDK sdk;
    private ProxyPlatformConfig config;
    private boolean setup;
    private PlaceholderManager placeholderManager;
    private Map<Object, Integer> queuedPlayers;
    private YamlDocument configYaml;

    private ServerInformation storeInformation;
    private List<Category> storeCategories;

    @Override
    public void onEnable() {
        // Bind SDK.
        Tebex.init(this);

        try {
            // Load the platform config file.
            configYaml = initPlatformConfig();
            config = loadProxyPlatformConfig(configYaml);
        } catch (IOException e) {
            log(Level.WARNING, "Failed to load config: " + e.getMessage());
            getProxy().getPluginManager().unregisterListeners(this);
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

        getProxy().getPluginManager().registerListener(this, new JoinListener(this));

        // Migrate the config from BuycraftX.
        migrateConfig();

        // Initialise the platform.
        init();

        getProxy().getScheduler().schedule(this, () -> {
            getSDK().getServerInformation().thenAccept(information -> storeInformation = information);
            getSDK().getListing().thenAccept(listing -> storeCategories = listing);
        }, 0, 5, TimeUnit.MINUTES);
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
            PluginManager pluginManager = getProxy().getPluginManager();
            boolean legacyPluginEnabled = pluginManager.getPlugin("BuycraftX") != null;
            boolean deletedLegacyPluginJar = false;

            if (legacyPluginEnabled) {
                try {
                    Plugin legacyPlugin = pluginManager.getPlugin("BuycraftX");

                    if (legacyPlugin != null) {
                        Method getFileMethod = Plugin.class.getDeclaredMethod("getFile");
                        getFileMethod.setAccessible(true);
                        File file = (File) getFileMethod.invoke(legacyPlugin);

                        legacyPlugin.onDisable();
                        pluginManager.unregisterListeners(legacyPlugin);
                        pluginManager.unregisterCommands(legacyPlugin);
                        deletedLegacyPluginJar = file.delete();
                    }
                } catch (Exception e) {
                    // Failed to get the plugin file via reflection.
                    getLogger().warning("Failed to delete the old BuycraftX files: " + e.getMessage());
                }
            }

            boolean finalDeletedLegacyPluginJar = deletedLegacyPluginJar;
            executeAsyncLater(() -> {
                boolean deletedLegacyPluginDir = FileUtils.deleteDirectory(oldPluginDir);
                if(! deletedLegacyPluginDir || !finalDeletedLegacyPluginJar) {
                    warning("Failed to delete the old BuycraftX files. Please delete them manually in your /plugins folder to avoid conflicts.");
                }

                warning("Please restart this proxy to complete the migration.");
            }, 1L, TimeUnit.SECONDS);
        } catch (IOException e) {
            warning("Failed to migrate config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public PlatformType getType() {
        return PlatformType.BUNGEECORD;
    }

    @Override
    public SDK getSDK() {
        return sdk;
    }

    @Override
    public File getDirectory() {
        return getDataFolder();
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
        return false;
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
        getProxy().getPluginManager().dispatchCommand(getProxy().getConsole(), command);
    }

    @Override
    public void executeAsync(Runnable runnable) {
        getProxy().getScheduler().runAsync(this, runnable);
    }

    @Override
    public void executeAsyncLater(Runnable runnable, long time, TimeUnit unit) {
        getProxy().getScheduler().schedule(this, runnable, time, unit);
    }

    @Override
    public void executeBlocking(Runnable runnable) {
        // BungeeCord has no concept of "blocking"
        executeAsync(runnable);
    }

    @Override
    public void executeBlockingLater(Runnable runnable, long time, TimeUnit unit) {
        // BungeeCord has no concept of "blocking"
        executeAsyncLater(runnable, time, unit);
    }


    private ProxiedPlayer getPlayer(Object player) {
        if(player == null) return null;

        if (isOnlineMode()) {
            return getProxy().getPlayer((UUID) player);
        }

        return getProxy().getPlayer((String) player);
    }

    @Override
    public boolean isPlayerOnline(Object player) {
        return getPlayer(player) != null;
    }

    @Override
    public int getFreeSlots(Object player) {
        // Bungee has no concept of an inventory
        return 0;
    }

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }

    @Override
    public void log(Level level, String message) {
        getLogger().log(level, message);
    }

    @Override
    public ProxyPlatformConfig getPlatformConfig() {
        return config;
    }

    @Override
    public PlatformTelemetry getTelemetry() {
        String serverVersion = getProxy().getVersion();

        Pattern pattern = Pattern.compile("MC: (\\d+\\.\\d+\\.\\d+)");
        Matcher matcher = pattern.matcher(serverVersion);
        if (matcher.find()) {
            serverVersion = matcher.group(1);
        }

        return new PlatformTelemetry(
                getVersion(),
                getProxy().getName(),
                serverVersion,
                System.getProperty("java.version"),
                System.getProperty("os.arch"),
                getProxy().getConfig().isOnlineMode()
        );
    }
}
