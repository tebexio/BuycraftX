package net.buycraft.plugin.sponge;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.client.ApiClient;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.execution.placeholder.PlaceholderManager;
import net.buycraft.plugin.execution.strategy.CommandExecutor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Created by meyerzinn on 2/14/16.
 */
@RequiredArgsConstructor
public class SpongeBuycraftPlatform implements IBuycraftPlatform {

    private final BuycraftPlugin plugin;

    @Override public String getName() {
        return "sponge";
    }

    @Override public ApiClient getApiClient() {
        return plugin.getApiClient();
    }

    @Override public PlaceholderManager getPlaceholderManager() {
        return plugin.getPlaceholderManager();
    }

    @Override public void dispatchCommand(String command) {
        Sponge.getGame().getCommandManager().process(Sponge.getServer().getConsole().getCommandSource().get(), command);
    }

    @Override public void executeAsync(Runnable runnable) {
        Sponge.getScheduler().createTaskBuilder().execute(runnable).async().submit(plugin);
    }

    @Override public void executeAsyncLater(Runnable runnable, long time, TimeUnit unit) {
        Sponge.getScheduler().createTaskBuilder().execute(runnable).async().delay(time, unit).submit(plugin);
    }

    @Override public void executeBlocking(Runnable runnable) {
        Sponge.getScheduler().createTaskBuilder().execute(runnable).submit(plugin);
    }

    @Override public void executeBlockingLater(Runnable runnable, long time, TimeUnit unit) {
        Sponge.getScheduler().createTaskBuilder().execute(runnable).delay(time, unit).submit(plugin);
    }

    @Override public boolean isPlayerOnline(QueuedPlayer player) {
        String uuidwd = player.getUuid().replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5");
        return Sponge.getServer().getPlayer(UUID.fromString(uuidwd)).isPresent();
    }

    @Override public int getFreeSlots(QueuedPlayer player) {
        Optional<Player> player1 = Sponge.getServer().getPlayer(UUID.fromString(player.getUuid()));
        if (!player1.isPresent()) {
            return -1;
        } else {
            return player1.get().getInventory().query(ItemTypes.NONE).size();
        }
    }

    @Override public void log(Level level, String message) {
        plugin.getLogger().info(level.toString() + " " + message);
    }

    @Override public void log(Level level, String message, Throwable throwable) {
        plugin.getLogger().info(level.toString() + " " + message, throwable);
    }

    @Override public CommandExecutor getExecutor() {
        return plugin.getCommandExecutor();
    }
}
