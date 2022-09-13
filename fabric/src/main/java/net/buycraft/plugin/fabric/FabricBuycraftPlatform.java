package net.buycraft.plugin.fabric;

import net.buycraft.plugin.BuyCraftAPI;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.UuidUtil;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.execution.placeholder.PlaceholderManager;
import net.buycraft.plugin.execution.strategy.CommandExecutor;
import net.buycraft.plugin.fabric.util.Multithreading;
import net.buycraft.plugin.platform.PlatformInformation;
import net.buycraft.plugin.platform.PlatformType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class FabricBuycraftPlatform implements IBuycraftPlatform {
    private final BuycraftPlugin plugin;

    public FabricBuycraftPlatform(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public BuyCraftAPI getApiClient() {
        return plugin.getApiClient();
    }

    @Override
    public PlaceholderManager getPlaceholderManager() {
        return plugin.getPlaceholderManager();
    }

    @Override
    public void dispatchCommand(String command) {
        plugin.getServer().getCommandManager().execute(plugin.getServer().getCommandSource(), command);
    }

    @Override
    public void executeAsync(Runnable runnable) {
        Multithreading.runAsync(runnable);
    }

    @Override
    public void executeAsyncLater(Runnable runnable, long time, TimeUnit unit) {
        Multithreading.schedule(runnable, time, unit);
    }

    @Override
    public void executeBlocking(Runnable runnable) {
        Multithreading.submit(runnable);
    }

    @Override
    public void executeBlockingLater(Runnable runnable, long time, TimeUnit unit) {
        Multithreading.schedule(() -> {
            Util.getMainWorkerExecutor().execute(runnable);
        }, time, unit);
    }

    private Optional<ServerPlayerEntity> getPlayer(QueuedPlayer player) {
        if (player.getUuid() != null && (plugin.getConfiguration().isBungeeCord() || plugin.getServer().isOnlineMode())) {
            return Optional.ofNullable(plugin.getServer().getPlayerManager().getPlayer(UuidUtil.mojangUuidToJavaUuid(player.getUuid())));
        }
        return Optional.ofNullable(plugin.getServer().getPlayerManager().getPlayer(player.getName()));
    }

    @Override
    public boolean isPlayerOnline(QueuedPlayer player) {
        return getPlayer(player).isPresent();
    }

    @Override
    public int getFreeSlots(QueuedPlayer player) {
        return getPlayer(player).map(value -> Math.max(0, 36 - value.inventory.size())).orElse(-1);
    }

    @Override
    public void log(Level level, String message) {
        plugin.getLogger().info(message);
    }

    @Override
    public void log(Level level, String message, Throwable throwable) {
        plugin.getLogger().info(message, throwable);
    }

    @Override
    public CommandExecutor getExecutor() {
        return plugin.getCommandExecutor();
    }

    @Override
    public PlatformInformation getPlatformInformation() {
        return new PlatformInformation(PlatformType.FABRIC, "Fabric" + " " + plugin.getServer().getVersion());
    }

    @Override
    public String getPluginVersion() {
        return plugin.getModVersion();
    }

    @Override
    public ServerInformation getServerInformation() {
        return plugin.getServerInformation();
    }

}
