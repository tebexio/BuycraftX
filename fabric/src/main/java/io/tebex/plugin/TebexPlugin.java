package io.tebex.plugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dev.dejvokep.boostedyaml.YamlDocument;
import io.tebex.plugin.event.JoinListener;
import io.tebex.plugin.manager.CommandManager;
import io.tebex.plugin.util.Multithreading;
import io.tebex.sdk.SDK;
import io.tebex.sdk.Tebex;
import io.tebex.sdk.obj.Category;
import io.tebex.sdk.obj.ServerEvent;
import io.tebex.sdk.placeholder.PlaceholderManager;
import io.tebex.sdk.platform.Platform;
import io.tebex.sdk.platform.PlatformTelemetry;
import io.tebex.sdk.platform.PlatformType;
import io.tebex.sdk.platform.config.ServerPlatformConfig;
import io.tebex.sdk.request.response.ServerInformation;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TebexPlugin implements Platform, DedicatedServerModInitializer {
    // Fabric Related
    private static final String MOD_ID = "tebex";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    private final String MOD_VERSION = "@VERSION@";
    private final File MOD_PATH = new File("./mods/" + MOD_ID);
    private MinecraftServer server;

    private SDK sdk;
    private ServerPlatformConfig config;
    private boolean setup;
    private PlaceholderManager placeholderManager;
    private Map<Object, Integer> queuedPlayers;
    private YamlDocument configYaml;

    private ServerInformation storeInformation;
    private List<Category> storeCategories;
    private List<ServerEvent> serverEvents;

    /**
     * Starts the Fabric platform.
     */
    @Override
    public void onInitializeServer() {
        try {
            // Load the platform config file.
            configYaml = initPlatformConfig();
            config = loadServerPlatformConfig(configYaml);
        } catch (IOException e) {
            log(Level.WARNING, "Failed to load config: " + e.getMessage());
            return;
        }

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            this.server = server;
            onEnable();
        });

        // Initialise Managers.
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> new CommandManager(this).register(dispatcher));

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> Multithreading.shutdown());
    }

    private void onEnable() {
        // Bind SDK.
        Tebex.init(this);

        // Initialise SDK.
        sdk = new SDK(this, config.getSecretKey());
        placeholderManager = new PlaceholderManager();
        queuedPlayers = Maps.newConcurrentMap();
        storeCategories = new ArrayList<>();
        serverEvents = new ArrayList<>();

        placeholderManager.registerDefaults();

        // Initialise the platform.
        init();

        new JoinListener(this);

        executeAsync(new Runnable() {
            @Override
            public void run() {
                info("Loading store information...");
                getSDK().getServerInformation()
                        .thenAccept(information -> storeInformation = information)
                        .exceptionally(error -> {
                            warning("Failed to load server information: " + error.getMessage());
                            return null;
                        });
                getSDK().getListing()
                        .thenAccept(listing -> storeCategories = listing)
                        .exceptionally(error -> {
                            warning("Failed to load store categories: " + error.getMessage());
                            return null;
                        });
            }
        });

        Multithreading.executeAsync(() -> {
            getSDK().getServerInformation().thenAccept(information -> storeInformation = information);
            getSDK().getListing().thenAccept(listing -> storeCategories = listing);
        }, 0, 30, TimeUnit.MINUTES);

        Multithreading.executeAsync(() -> {
            List<ServerEvent> runEvents = Lists.newArrayList(serverEvents.subList(0, Math.min(serverEvents.size(), 750)));
            if (runEvents.isEmpty()) return;

            sdk.sendEvents(runEvents)
                    .thenAccept(aVoid -> {
                        serverEvents.removeAll(runEvents);
                        debug("Successfully sent analytics.");
                    })
                    .exceptionally(throwable -> {
                        warning("Failed to send analytics: " + throwable.getMessage());
                        return null;
                    });
        }, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public PlatformType getType() {
        return PlatformType.FABRIC;
    }

    @Override
    public SDK getSDK() {
        return sdk;
    }

    @Override
    public File getDirectory() {
        return MOD_PATH;
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
        return getPlatformConfig().isProxyMode() || server.isOnlineMode();
    }

    public List<Category> getStoreCategories() {
        return storeCategories;
    }

    public ServerInformation getStoreInformation() {
        return storeInformation;
    }

    public List<ServerEvent> getServerEvents() {
        return serverEvents;
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
        server.getCommandManager().execute(server.getCommandSource(), command);
    }

    @Override
    public void executeAsync(Runnable runnable) {
        Multithreading.runAsync(runnable);
    }

    @Override
    public void executeAsyncLater(Runnable runnable, long time, TimeUnit unit) {
        Multithreading.executeAsyncLater(runnable, time, unit);
    }

    @Override
    public void executeBlocking(Runnable runnable) {
        try {
            Multithreading.executeBlocking(runnable);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void executeBlockingLater(Runnable runnable, long time, TimeUnit unit) {
        try {
            Multithreading.executeBlockingLater(runnable, time, unit);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<ServerPlayerEntity> getPlayer(Object player) {
        if(player == null) return Optional.empty();

        if(isOnlineMode()) {
            return Optional.ofNullable(server.getPlayerManager().getPlayer((UUID) player));
        }

        return Optional.ofNullable(server.getPlayerManager().getPlayer((String) player));
    }

    @Override
    public boolean isPlayerOnline(Object player) {
        return getPlayer(player).isPresent();
    }

    @Override
    public int getFreeSlots(Object playerId) {
        ServerPlayerEntity player = getPlayer(playerId).orElse(null);
        if (player == null) return -1;

        DefaultedList<ItemStack> inv = player.inventory.main;
        return (int) inv.stream()
                .filter(obj -> obj == null || obj.isEmpty())
                .count();
    }

    @Override
    public String getVersion() {
        return MOD_VERSION;
    }

    @Override
    public void log(Level level, String message) {
        if(level == Level.INFO) {
            LOGGER.info(message);
        } else if(level == Level.WARNING) {
            LOGGER.warn(message);
        } else if(level == Level.SEVERE) {
            LOGGER.error(message);
        } else {
            LOGGER.info(message);
        }
    }

    @Override
    public ServerPlatformConfig getPlatformConfig() {
        return config;
    }

    @Override
    public PlatformTelemetry getTelemetry() {
        String serverVersion = server.getVersion();

        Pattern pattern = Pattern.compile("MC: (\\d+\\.\\d+\\.\\d+)");
        Matcher matcher = pattern.matcher(serverVersion);
        if (matcher.find()) {
            serverVersion = matcher.group(1);
        }

        return new PlatformTelemetry(
                getVersion(),
                server.getName(),
                serverVersion,
                System.getProperty("java.version"),
                System.getProperty("os.arch"),
                server.isOnlineMode()
        );
    }
}
