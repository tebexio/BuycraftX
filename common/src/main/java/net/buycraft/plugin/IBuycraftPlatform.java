package net.buycraft.plugin;

import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.execution.placeholder.PlaceholderManager;
import net.buycraft.plugin.execution.strategy.CommandExecutor;
import net.buycraft.plugin.platform.PlatformInformation;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public interface IBuycraftPlatform {
    /**
     * Retrieves the {@link BuyCraftAPI} used to handle requests to the Buycraft plugin API.
     *
     * @return the {@link BuyCraftAPI} object
     */
    BuyCraftAPI getApiClient();

    /**
     * Retrieves the {@link PlaceholderManager} to use when executing commands.
     *
     * @return the {@link PlaceholderManager}
     */
    PlaceholderManager getPlaceholderManager();

    /**
     * Dispatches a command as a server superuser, typically as the console command sender.
     *
     * @param command the command to execute
     */
    void dispatchCommand(String command);

    /**
     * Executes a task to be scheduled as soon as possible asynchronously.
     *
     * @param runnable the task to run
     */
    void executeAsync(Runnable runnable);

    /**
     * Executes a task to be scheduled to after after a certain period of time.
     *
     * @param runnable the task to run
     * @param time     the quantity of time
     * @param unit     the unit of time to use
     */
    void executeAsyncLater(Runnable runnable, long time, TimeUnit unit);

    /**
     * Executes a task to be scheduled to be run on the main server thread as soon as possible. If the platform doesn't
     * have the concept of a main server thread, this call should run tasks asynchronously instead (BuycraftX is
     * thread-safe internally).
     *
     * @param runnable the task to run
     */
    void executeBlocking(Runnable runnable);

    /**
     * Executes a task to be scheduled to be run on the main server thread after a certain period of time. If the platform
     * doesn't have the concept of a main server thread, this call should run tasks asynchronously instead (BuycraftX is
     * thread-safe internally).
     *
     * @param runnable the task to run
     * @param time     the quantity of time
     * @param unit     the unit of time to use
     */
    void executeBlockingLater(Runnable runnable, long time, TimeUnit unit);

    /**
     * Determine whether or not the specified {@link QueuedPlayer} is online.
     *
     * @param player the player to check
     * @return whether or not the player is online
     */
    boolean isPlayerOnline(QueuedPlayer player);

    /**
     * Determines the number of free inventory slots available for the specified {@link QueuedPlayer}.
     *
     * @param player the player to check
     * @return -1 if not applicable (platform does not have this concept or player is offline), otherwise the number of
     * slots available in the player's inventory
     */
    int getFreeSlots(QueuedPlayer player);

    /**
     * Logs a message.
     *
     * @param level   the level of the message
     * @param message the message to log
     */
    void log(Level level, String message);

    /**
     * Logs a message with an exception.
     *
     * @param level     the level of the message
     * @param message   the message to log
     * @param throwable the throwable to log
     */
    void log(Level level, String message, Throwable throwable);

    /**
     * Retrieves the command executor to use.
     *
     * @return the command executor to use
     */
    CommandExecutor getExecutor();

    /**
     * Returns the current platform information.
     *
     * @return the platform information
     */
    PlatformInformation getPlatformInformation();

    /**
     * Returns the current plugin version.
     *
     * @return the current plugin version
     */
    String getPluginVersion();

    /**
     * Returns the platform's server information.
     *
     * @return the server information
     */
    ServerInformation getServerInformation();
}
