package net.buycraft.plugin;

import net.buycraft.plugin.client.ApiClient;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.execution.placeholder.PlaceholderManager;
import net.buycraft.plugin.execution.strategy.CommandExecutor;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public interface IBuycraftPlatform {
    ApiClient getApiClient();
    PlaceholderManager getPlaceholderManager();
    void dispatchCommand(String command);
    void executeAsync(Runnable runnable);
    void executeAsyncLater(Runnable runnable, long time, TimeUnit unit);
    void executeBlocking(Runnable runnable);
    void executeBlockingLater(Runnable runnable, long time, TimeUnit unit);
    boolean isPlayerOnline(QueuedPlayer player);
    int getFreeSlots(QueuedPlayer player);
    void log(Level level, String message);
    void log(Level level, String message, Throwable throwable);
    CommandExecutor getExecutor();
}
