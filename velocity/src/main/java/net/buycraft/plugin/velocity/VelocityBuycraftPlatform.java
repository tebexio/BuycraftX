package net.buycraft.plugin.velocity;

import com.velocitypowered.api.proxy.Player;
import net.buycraft.plugin.BuyCraftAPI;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.UuidUtil;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.execution.placeholder.PlaceholderManager;
import net.buycraft.plugin.execution.strategy.CommandExecutor;
import net.buycraft.plugin.platform.PlatformInformation;
import net.buycraft.plugin.platform.PlatformType;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Level;

public class VelocityBuycraftPlatform implements IBuycraftPlatform {

    private Map<Level, Function<Logger, BiConsumer<String, Throwable>>> LOG_LEVEL_MAP = new HashMap<Level, Function<Logger, BiConsumer<String, Throwable>>>() {{
        put(Level.INFO, l -> l::info);
        put(Level.WARNING, l -> l::warn);
        put(Level.SEVERE, l -> l::error);
    }};

    private final BuycraftPlugin plugin;

    public VelocityBuycraftPlatform(BuycraftPlugin plugin) {
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
        plugin.getServer().getCommandManager().executeAsync(plugin.getServer().getConsoleCommandSource(), command);
    }

    @Override
    public void executeAsync(Runnable runnable) {
        plugin.getServer().getScheduler().buildTask(plugin, runnable).schedule();
    }

    @Override
    public void executeAsyncLater(Runnable runnable, long time, TimeUnit unit) {
        plugin.getServer().getScheduler().buildTask(plugin, runnable).delay(time, unit).schedule();
    }

    @Override
    public void executeBlocking(Runnable runnable) {
        executeAsync(runnable);
    }

    @Override
    public void executeBlockingLater(Runnable runnable, long time, TimeUnit unit) {
        executeAsyncLater(runnable, time, unit);
    }

    private Player getPlayer(QueuedPlayer player) {
        if (player.getUuid() != null && plugin.getServer().getConfiguration().isOnlineMode()) {
            return plugin.getServer().getPlayer(UuidUtil.mojangUuidToJavaUuid(player.getUuid())).orElse(null);
        }
        return plugin.getServer().getPlayer(player.getName()).orElse(null);
    }

    @Override
    public boolean isPlayerOnline(QueuedPlayer player) {
        return getPlayer(player) != null;
    }

    @Override
    public int getFreeSlots(QueuedPlayer player) {
        return 0;
    }

    @Override
    public void log(Level level, String message) {
        LOG_LEVEL_MAP.get(level).apply(plugin.getLogger()).accept(message, null);
    }

    @Override
    public void log(Level level, String message, Throwable throwable) {
        LOG_LEVEL_MAP.get(level).apply(plugin.getLogger()).accept(message, throwable);
    }

    @Override
    public CommandExecutor getExecutor() {
        return plugin.getCommandExecutor();
    }

    @Override
    public PlatformInformation getPlatformInformation() {
        return new PlatformInformation(PlatformType.VELOCITY, plugin.getServer().getVersion().getVersion());
    }

    @Override
    public String getPluginVersion() {
        return plugin.getServer().getPluginManager().fromInstance(plugin).orElseThrow(IllegalStateException::new)
                .getDescription().getVersion().orElse("UNKNOWN-SNAPSHOT");
    }

    @Override
    public ServerInformation getServerInformation() {
        return plugin.getServerInformation();
    }
}
