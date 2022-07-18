package net.buycraft.plugin.sponge;

import net.buycraft.plugin.BuyCraftAPI;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.UuidUtil;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.execution.placeholder.PlaceholderManager;
import net.buycraft.plugin.execution.strategy.CommandExecutor;
import net.buycraft.plugin.platform.PlatformInformation;
import net.buycraft.plugin.platform.PlatformType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.Task;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class SpongeBuycraftPlatform implements IBuycraftPlatform {
    private final BuycraftPlugin plugin;

    public SpongeBuycraftPlatform(final BuycraftPlugin plugin) {
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
        try {
            Sponge.game().server().commandManager().process(Sponge.game().systemSubject(), command);
        } catch (CommandException e) {
            // TODO: Handle this.
            throw new RuntimeException(e);
        }
    }

    @Override
    public void executeAsync(Runnable runnable) {
        Sponge.asyncScheduler().submit(Task.builder().execute(runnable).plugin(plugin.getPlugin()).build());
    }

    @Override
    public void executeAsyncLater(Runnable runnable, long time, TimeUnit unit) {
        Sponge.asyncScheduler().submit(Task.builder().execute(runnable).delay(time, unit).plugin(plugin.getPlugin()).build());
    }

    @Override
    public void executeBlocking(Runnable runnable) {
        Sponge.game().server().scheduler().submit(Task.builder().execute(runnable).plugin(plugin.getPlugin()).build());
    }

    @Override
    public void executeBlockingLater(Runnable runnable, long time, TimeUnit unit) {
        Sponge.game().server().scheduler().submit(Task.builder().execute(runnable).delay(time, unit).plugin(plugin.getPlugin()).build());
    }

    private Optional<ServerPlayer> getPlayer(QueuedPlayer player) {
        if (player.getUuid() != null && (plugin.getConfiguration().isBungeeCord() || Sponge.server().isOnlineModeEnabled())) {
            return Sponge.server().player(UuidUtil.mojangUuidToJavaUuid(player.getUuid()));
        }
        return Sponge.server().player(player.getName());
    }

    @Override
    public boolean isPlayerOnline(QueuedPlayer player) {
        return getPlayer(player).isPresent();
    }

    @Override
    public int getFreeSlots(QueuedPlayer player) {
        return getPlayer(player).map(value -> Math.max(0, 36 - value.inventory().slots().size())).orElse(-1);
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
        return new PlatformInformation(PlatformType.SPONGE, Sponge.platform().type().name() + " " + Sponge.platform().minecraftVersion().name());
    }

    @Override
    public String getPluginVersion() {
        return plugin.getPlugin().metadata().version().toString();
    }

    @Override
    public ServerInformation getServerInformation() {
        return plugin.getServerInformation();
    }
}
