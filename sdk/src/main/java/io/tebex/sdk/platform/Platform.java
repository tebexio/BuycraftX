package io.tebex.sdk.platform;

import dev.dejvokep.boostedyaml.YamlDocument;
import io.tebex.sdk.SDK;
import io.tebex.sdk.exception.ServerNotFoundException;
import io.tebex.sdk.obj.QueuedCommand;
import io.tebex.sdk.obj.QueuedPlayer;
import io.tebex.sdk.placeholder.PlaceholderManager;
import io.tebex.sdk.platform.config.IPlatformConfig;
import io.tebex.sdk.platform.config.ProxyPlatformConfig;
import io.tebex.sdk.platform.config.ServerPlatformConfig;
import io.tebex.sdk.request.response.ServerInformation;
import io.tebex.sdk.triage.TriageEvent;
import io.tebex.sdk.util.StringUtil;
import io.tebex.sdk.util.UUIDUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static io.tebex.sdk.util.ResourceUtil.getBundledFile;

/**
 * The Platform interface defines the base methods required for interacting with a server platform.
 * Implementations should provide functionality specific to their platform, such as Bukkit or Sponge.
 */
public interface Platform {
    int MAX_COMMANDS_PER_BATCH = 3;

    /**
     * Gets the platform type.
     *
     * @return The PlatformType enum value representing the server platform.
     */
    PlatformType getType();

    /**
     * Gets the SDK instance associated with this platform.
     *
     * @return The SDK instance.
     */
    SDK getSDK();

    /**
     * Gets the directory where the plugin is running from.
     *
     * @return The directory.
     */
    File getDirectory();

    /**
     * Checks if the platform is set up and ready to use.
     *
     * @return True if the platform is set up, false otherwise.
     */
    boolean isSetup();

    /**
     * Sets whether the platform is set up and ready to use.
     */
    void setSetup(boolean setup);

    /**
     * Checks if the platform is in online mode.
     *
     * @return Whether the server is in online mode.
     */
    boolean isOnlineMode();

    /**
     * Configures the platform for use.
     */
    void configure();

    /**
     * Halts the platform and stops any ongoing tasks.
     */
    void halt();

    default void init() {
        if (getPlatformConfig().getSecretKey() != null && !getPlatformConfig().getSecretKey().isEmpty()) {
            getSDK().getServerInformation().thenAccept(serverInformation -> {
                ServerInformation.Server server = serverInformation.getServer();
                ServerInformation.Store store = serverInformation.getStore();

                info(String.format("Connected to %s - %s server.", server.getName(), store.getGameType()));

                setSetup(true);
                configure();
            }).exceptionally(ex -> {
                Throwable cause = ex.getCause();
                setSetup(false);

                if (cause instanceof ServerNotFoundException) {
                    warning("Failed to connect. Please double-check your server key or run the setup command again.");
                    this.halt();
                } else {
                    warning("Failed to get server information: " + cause.getMessage());
                    cause.printStackTrace();
                }

                return null;
            });
        } else {
            log(Level.WARNING, "Welcome to Tebex! It seems like this is a new setup.");
            log(Level.WARNING, "To get started, please use the 'tebex secret <key>' command in the console.");
        }
    }

    PlaceholderManager getPlaceholderManager();

    Map<Object, Integer> getQueuedPlayers();

    /**
     * Dispatches a command to the server.
     * @param command The command to dispatch.
     */
    void dispatchCommand(String command);

    void executeAsync(Runnable runnable);
    void executeAsyncLater(Runnable runnable, long time, TimeUnit unit);
    void executeBlocking(Runnable runnable);
    void executeBlockingLater(Runnable runnable, long time, TimeUnit unit);
    boolean isPlayerOnline(Object player);
    int getFreeSlots(Object player);

    default void performCheck() {
        performCheck(true);
    }

    default void performCheck(boolean runAfter) {
        if(! isSetup()) return;

        debug("Checking for due players..");
        getQueuedPlayers().clear();

        getSDK().getDuePlayers().thenAccept(duePlayersResponse -> {
            if(runAfter) {
                executeAsyncLater(this::performCheck, duePlayersResponse.getNextCheck(), TimeUnit.SECONDS);
            }

            List<QueuedPlayer> playerList = duePlayersResponse.getPlayers();

            if(! playerList.isEmpty()) {
                debug("Found " + playerList.size() + " " + StringUtil.pluralise(playerList.size(), "player", "players") + " with pending commands.");
                playerList.forEach(this::handleOnlineCommands);
            }

            if(! duePlayersResponse.canExecuteOffline()) return;
            handleOfflineCommands();
        }).exceptionally(ex -> {
            warning("Failed to perform check: " + ex.getMessage());
            ex.printStackTrace();
            sendTriageEvent(ex);
            return null;
        });
    }

    default void sendTriageEvent(String errorMessage) {
        TriageEvent.fromPlatform(this).withErrorMessage(errorMessage).send();
    }

    default void sendTriageEvent(Throwable exception) {
        StringWriter traceWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(traceWriter));

        TriageEvent.fromPlatform(this)
                .withErrorMessage(exception.getMessage())
                .withTrace(traceWriter.toString()).send();
    }

    default void handleOnlineCommands(QueuedPlayer player) {
        if(! isSetup()) return;

        debug("Handling commands for player '" + player.getName() + "'..");

        getSDK().getOnlineCommands(player).thenAccept(onlineCommands -> {
            if(onlineCommands.isEmpty()) {
                info("No commands found for " + player.getName() + ".");
                return;
            }

            debug("Found " + onlineCommands.size() + " online " + StringUtil.pluralise(onlineCommands.size(), "command") + ".");

            Object playerId = getPlayerId(player.getName(), UUIDUtil.mojangIdToJavaId(player.getUuid()));
            if(! isPlayerOnline(playerId)) {
                debug("Skipping player " + player.getName() + " as they are offline.");
                getQueuedPlayers().put(playerId, player.getId());
                return;
            }

            processOnlineCommands(player.getName(), playerId, onlineCommands);
        }).exceptionally(ex -> {
            warning("Failed to get online commands: " + ex.getMessage());
            ex.printStackTrace();
            sendTriageEvent(ex);
            return null;
        });
    }

    /**
     * Gets the player ID for a player.
     * @param name The name of the player.
     * @param uuid The UUID of the player.
     * @return The player ID to use.
     */
    default Object getPlayerId(String name, UUID uuid) {
        return isOnlineMode() ? uuid : name;
    }

    /**
     * Processes the online commands for a player.
     *
     * @param playerName The name of the player.
     * @param playerId The Unique Identifier of the player.
     * @param commands The commands to process.
     */
    default void processOnlineCommands(String playerName, Object playerId, List<QueuedCommand> commands) {
        if(! isSetup()) return;

        List<Integer> completedCommands = new ArrayList<>();
        boolean hasInventorySpace = true;
        for (QueuedCommand command : commands) {
            if(getFreeSlots(playerId) < command.getRequiredSlots()) {
                debug(String.format("Skipping command '%s' for player '%s' due to no inventory space.", command.getCommand(), playerName));
                hasInventorySpace = false;
                continue;
            }

            executeBlocking(() -> {
                info(String.format("Dispatching command '%s' for player '%s'.", command.getCommand(), playerName));
                dispatchCommand(command.getCommand());
            });
            completedCommands.add(command.getId());

            if(completedCommands.size() % MAX_COMMANDS_PER_BATCH == 0) {
                deleteCompletedCommands(completedCommands);
                completedCommands.clear();
            }
        }

        if (!completedCommands.isEmpty()) {
            deleteCompletedCommands(completedCommands);
            completedCommands.clear();
        }

        if(! hasInventorySpace) return;
        getQueuedPlayers().remove(playerId);
    }

    default void handleOfflineCommands() {
        if(! isSetup()) return;

        getSDK().getOfflineCommands().thenAccept(offlineData -> {
            if(offlineData.getCommands().isEmpty()) {
                return;
            }

            List<Integer> completedCommands = new ArrayList<>();
            for (QueuedCommand command : offlineData.getCommands()) {
                executeBlockingLater(() -> {
                    info(String.format("Dispatching offline command '%s' for player '%s'.", command.getCommand(), command.getPlayer().getName()));
                    dispatchCommand(command.getCommand());
                }, command.getDelay(), TimeUnit.SECONDS);
                completedCommands.add(command.getId());

                if(completedCommands.size() % MAX_COMMANDS_PER_BATCH == 0) {
                    deleteCompletedCommands(completedCommands);
                    completedCommands.clear();
                }
            }

            if (! completedCommands.isEmpty()) {
                deleteCompletedCommands(completedCommands);
                completedCommands.clear();
            }
        }).exceptionally(ex -> {
            warning("Failed to get offline commands: " + ex.getMessage());
            ex.printStackTrace();
            sendTriageEvent(ex);
            return null;
        });
    }

    default void deleteCompletedCommands(List<Integer> completedCommands) {
        getSDK().deleteCommands(completedCommands).thenRun(completedCommands::clear).exceptionally(ex -> {
            warning("Failed to delete commands: " + ex.getMessage());
            ex.printStackTrace();
            sendTriageEvent(ex);
            return null;
        });

    }

    /**
     * Gets the version of the platform implementation.
     *
     * @return The version string.
     */
    String getVersion();

    /**
     * Converts the version string into a version number.
     *
     * @return The version number.
     */
    default int getVersionNumber() {
        return Integer.parseInt(getVersion().replace(".", ""));
    }

    /**
     * Logs a message to the console with the specified level.
     *
     * @param level   The level of the message.
     * @param message The message to log.
     */
    void log(Level level, String message);

    /**
     * Logs an informational message to the console.
     *
     * @param message The message to log.
     */
    default void info(String message) {
        log(Level.INFO, message);
    }

    /**
     * Logs a warning message to the console.
     *
     * @param message The message to log.
     */
    default void warning(String message) {
        log(Level.WARNING, message);
    }

    /**
     * Logs a debug message to the console if debugging is enabled in the platform configuration.
     *
     * @param message The message to log.
     */
    default void debug(String message) {
        if (! getPlatformConfig().isVerbose()) return;
        info("[DEBUG] " + message);
    }

    // Create and update the file
    default YamlDocument initPlatformConfig() throws IOException {
        return YamlDocument.create(getBundledFile(this, getDirectory(), "config.yml"));
    }

    /**
     * Loads the server platform configuration from the file.
     *
     * @param configFile The configuration file.
     * @return The PlatformConfig instance representing the loaded configuration.
     */
    default ServerPlatformConfig loadServerPlatformConfig(YamlDocument configFile) {
        ServerPlatformConfig config = new ServerPlatformConfig(configFile.getInt("config-version", 1));
        config.setYamlDocument(configFile);

        if(config.getConfigVersion() < 2) {
            return config;
        }

        config.setSecretKey(configFile.getString("server.secret-key"));
        config.setBuyCommandName(configFile.getString("buy-command.name", "buy"));
        config.setBuyCommandEnabled(configFile.getBoolean("buy-command.enabled", true));

        config.setCheckForUpdates(configFile.getBoolean("check-for-updates", true));
        config.setVerbose(configFile.getBoolean("verbose", false));

        config.setProxyMode(configFile.getBoolean("server.proxy", false));
        config.setAutoReportEnabled(configFile.getBoolean("auto-report-enabled", true));

        return config;
    }

    /**
     * Loads the proxy platform configuration from the file.
     *
     * @param configFile The configuration file.
     * @return The PlatformConfig instance representing the loaded configuration.
     */
    default ProxyPlatformConfig loadProxyPlatformConfig(YamlDocument configFile) {
        ProxyPlatformConfig config = new ProxyPlatformConfig(configFile.getInt("config-version", 1));
        config.setYamlDocument(configFile);

        if(config.getConfigVersion() < 2) {
            return config;
        }

        config.setSecretKey(configFile.getString("server.secret-key"));
        config.setVerbose(configFile.getBoolean("verbose", false));

        return config;
    }

    /**
     * Gets the current platform configuration.
     *
     * @return The PlatformConfig instance representing the current configuration.
     */
    IPlatformConfig getPlatformConfig();

    /**
     * Gets the platform telemetry instance.
     *
     * @return The PlatformTelemetry instance.
     */
    PlatformTelemetry getTelemetry();
}
