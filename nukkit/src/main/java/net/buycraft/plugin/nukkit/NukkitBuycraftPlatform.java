package net.buycraft.plugin.nukkit;

import cn.nukkit.Player;
import cn.nukkit.scheduler.AsyncTask;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.client.ApiClient;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.execution.placeholder.PlaceholderManager;
import net.buycraft.plugin.execution.strategy.CommandExecutor;
import net.buycraft.plugin.platform.PlatformInformation;
import net.buycraft.plugin.platform.PlatformType;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class NukkitBuycraftPlatform implements IBuycraftPlatform {
    private final BuycraftPlugin plugin;

    public NukkitBuycraftPlatform(BuycraftPlugin plugin) {
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
        plugin.getServer().getCommandMap().dispatch(plugin.getServer().getConsoleSender(), command);
    }

    @Override
    public void executeAsync(Runnable runnable) {
        plugin.getServer().getScheduler().scheduleAsyncTask(plugin, new AsyncTask() {
            @Override
            public void onRun() {
                runnable.run();
            }
        });
    }

    @Override
    public void executeAsyncLater(Runnable runnable, long time, TimeUnit unit) {
        plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
            plugin.getServer().getScheduler().scheduleAsyncTask(plugin, new AsyncTask() {
                @Override
                public void onRun() {
                    runnable.run();
                }
            });
        }, (int) (unit.toMillis(time) / 50));
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

    private Player getPlayer(QueuedPlayer player) {
        return plugin.getServer().getPlayer(player.getName());
    }

    @Override
    public boolean isPlayerOnline(QueuedPlayer player) {
        return getPlayer(player) != null;
    }

    @Override
    public int getFreeSlots(QueuedPlayer player) {
        Player player1 = getPlayer(player);
        if (player1 != null) {
            int free = 0;
            for (int i = 0; i < player1.getInventory().getSize(); i++) {
                if (player1.getInventory().getItem(i).getId() == 0) {
                    free++;
                }
            }
            return free;
        }
        return -1;
    }

    @Override
    public void log(Level level, String message) {
        plugin.getLoggerUtils().log(level, message);
    }

    @Override
    public void log(Level level, String message, Throwable throwable) {
        plugin.getLoggerUtils().log(level, message, throwable);
    }

    @Override
    public CommandExecutor getExecutor() {
        return plugin.getCommandExecutor();
    }

    @Override
    public PlatformInformation getPlatformInformation() {
        return new PlatformInformation(PlatformType.NUKKIT, plugin.getServer().getNukkitVersion());
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
