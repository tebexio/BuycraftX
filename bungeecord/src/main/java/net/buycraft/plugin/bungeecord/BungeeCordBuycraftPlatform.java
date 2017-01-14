package net.buycraft.plugin.bungeecord;

import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.UuidUtil;
import net.buycraft.plugin.client.ApiClient;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.execution.placeholder.PlaceholderManager;
import net.buycraft.plugin.execution.strategy.CommandExecutor;
import net.buycraft.plugin.platform.NoBlocking;
import net.buycraft.plugin.platform.PlatformInformation;
import net.buycraft.plugin.platform.PlatformType;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@NoBlocking
public class BungeeCordBuycraftPlatform implements IBuycraftPlatform {
    private final BuycraftPlugin plugin;

    public BungeeCordBuycraftPlatform(BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public ApiClient getApiClient() {
        return plugin.getApiClient();
    }

    @Override
    public PlaceholderManager getPlaceholderManager() {
        return plugin.getPlaceholderManager();
    }

    @Override
    public void dispatchCommand(String command) {
        plugin.getProxy().getPluginManager().dispatchCommand(plugin.getProxy().getConsole(), command);
    }

    @Override
    public void executeAsync(Runnable runnable) {
        plugin.getProxy().getScheduler().runAsync(plugin, runnable);
    }

    @Override
    public void executeAsyncLater(Runnable runnable, long time, TimeUnit unit) {
        plugin.getProxy().getScheduler().schedule(plugin, runnable, time, unit);
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

    private ProxiedPlayer getPlayer(QueuedPlayer player) {
        if (player.getUuid() != null && plugin.getProxy().getConfig().isOnlineMode()) {
            return plugin.getProxy().getPlayer(UuidUtil.mojangUuidToJavaUuid(player.getUuid()));
        }
        return plugin.getProxy().getPlayer(player.getName());
    }

    @Override
    public boolean isPlayerOnline(QueuedPlayer player) {
        return getPlayer(player) != null;
    }

    @Override
    public int getFreeSlots(QueuedPlayer player) {
        return 0; // Bungee has no concept of an inventory
    }

    @Override
    public void log(Level level, String message) {
        plugin.getLogger().log(level, message);
    }

    @Override
    public void log(Level level, String message, Throwable throwable) {
        plugin.getLogger().log(level, message, throwable);
    }

    @Override
    public CommandExecutor getExecutor() {
        return plugin.getCommandExecutor();
    }

    @Override
    public PlatformInformation getPlatformInformation() {
        return new PlatformInformation(PlatformType.BUNGEECORD, plugin.getProxy().getVersion());
    }

    @Override
    public String getPluginVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public ServerInformation getServerInformation() {
        return plugin.getServerInformation();
    }
}
