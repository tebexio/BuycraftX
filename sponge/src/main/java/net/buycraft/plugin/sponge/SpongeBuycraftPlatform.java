package net.buycraft.plugin.sponge;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.UuidUtil;
import net.buycraft.plugin.client.ApiClient;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.execution.placeholder.PlaceholderManager;
import net.buycraft.plugin.execution.strategy.CommandExecutor;
import net.buycraft.plugin.platform.PlatformInformation;
import net.buycraft.plugin.platform.PlatformType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.Plugin;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@RequiredArgsConstructor
public class SpongeBuycraftPlatform implements IBuycraftPlatform {

    private final BuycraftPlugin plugin;

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
        Sponge.getGame().getCommandManager().process(Sponge.getServer().getConsole().getCommandSource().get(), command);
    }

    @Override
    public void executeAsync(Runnable runnable) {
        Sponge.getScheduler().createTaskBuilder().execute(runnable).async().submit(plugin);
    }

    @Override
    public void executeAsyncLater(Runnable runnable, long time, TimeUnit unit) {
        Sponge.getScheduler().createTaskBuilder().execute(runnable).async().delay(time, unit).submit(plugin);
    }

    @Override
    public void executeBlocking(Runnable runnable) {
        Sponge.getScheduler().createTaskBuilder().execute(runnable).submit(plugin);
    }

    @Override
    public void executeBlockingLater(Runnable runnable, long time, TimeUnit unit) {
        Sponge.getScheduler().createTaskBuilder().execute(runnable).delay(time, unit).submit(plugin);
    }

    private Optional<Player> getPlayer(QueuedPlayer player) {
        if (player.getUuid() != null && (plugin.getConfiguration().isBungeeCord() || Sponge.getServer().getOnlineMode())) {
            return Sponge.getServer().getPlayer(UuidUtil.mojangUuidToJavaUuid(player.getUuid()));
        }
        return Sponge.getServer().getPlayer(player.getName());
    }

    @Override
    public boolean isPlayerOnline(QueuedPlayer player) {
        return getPlayer(player).isPresent();
    }

    @Override
    public int getFreeSlots(QueuedPlayer player) {
        Optional<Player> player1 = getPlayer(player);
        if (!player1.isPresent()) {
            return -1;
        } else {
            return Math.max(0, 36 - player1.get().getInventory().size());
        }
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
        return new PlatformInformation(PlatformType.SPONGE, Sponge.getPlatform().getImplementation().getName() + " " +
                Sponge.getPlatform().getImplementation().getVersion().orElse("UNKNOWN"));
    }

    @Override
    public String getPluginVersion() {
        return plugin.getClass().getAnnotation(Plugin.class).version();
    }

    @Override
    public ServerInformation getServerInformation() {
        return plugin.getServerInformation();
    }
}
