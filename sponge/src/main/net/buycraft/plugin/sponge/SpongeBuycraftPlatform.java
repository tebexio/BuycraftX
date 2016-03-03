package net.buycraft.plugin.sponge;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.client.ApiClient;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.execution.placeholder.PlaceholderManager;
import net.buycraft.plugin.execution.strategy.CommandExecutor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.EmptyInventory;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class SpongeBuycraftPlatform implements IBuycraftPlatform {
    private final BuycraftPlugin plugin;

    @Override
    public String getName() {
        return "sponge";
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
        Sponge.getCommandManager().process(
                Sponge.getServer().getConsole(),
                command
        );
    }

    @Override
    public void executeAsync(Runnable runnable) {
        executeAsyncLater(
                runnable,
                0,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void executeAsyncLater(Runnable runnable, long time, TimeUnit unit) {
        Sponge.getScheduler().createAsyncExecutor(plugin).schedule(
                runnable,
                time,
                unit
        ).run();
    }

    @Override
    public void executeBlocking(Runnable runnable) {
        executeBlockingLater(
                runnable,
                0,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void executeBlockingLater(Runnable runnable, long time, TimeUnit unit) {
        Sponge.getScheduler().createSyncExecutor(plugin).schedule(
                runnable,
                time,
                unit
        ).run();
    }

    @Override
    public boolean isPlayerOnline(QueuedPlayer player) {
        return convert(player).isPresent();
    }

    @Override
    public int getFreeSlots(QueuedPlayer player) {
        Optional<Player> optional = convert(player);

        if (!optional.isPresent()) {
            return -1;
        }

        return optional.get().getInventory().query(
                EmptyInventory.class
        ).size();
    }

    @Override
    public void log(Level level, String message) {
        switch (level.getName()) {
            case "WARNING":
                plugin.getLogger().warn(message);
                break;
            default:
                plugin.getLogger().info(message);
                break;
        }
    }

    @Override
    public void log(Level level, String message, Throwable throwable) {
        switch (level.getName()) {
            case "WARNING":
                plugin.getLogger().warn(
                        message,
                        throwable
                );
                break;
            default:
                plugin.getLogger().info(
                        message,
                        throwable
                );
                break;
        }
    }

    @Override
    public CommandExecutor getExecutor() {
        return plugin.getCommandExecutor();
    }

    private Optional<Player> convert(QueuedPlayer player) {
        return Sponge.getServer().getPlayer(
                UUID.fromString(
                        player.getUuid()
                )
        );
    }
}
