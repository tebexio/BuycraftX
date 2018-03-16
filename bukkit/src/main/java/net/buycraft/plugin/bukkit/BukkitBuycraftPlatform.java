package net.buycraft.plugin.bukkit;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

@RequiredArgsConstructor
public class BukkitBuycraftPlatform implements IBuycraftPlatform {
    private static final int MAXIMUM_USABLE_INVENTORY_SIZE = 36;
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
        plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @Override
    public void executeAsync(Runnable runnable) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    @Override
    public void executeAsyncLater(Runnable runnable, long time, TimeUnit unit) {
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, runnable, unit.toMillis(time) / 50);
    }

    @Override
    public void executeBlocking(Runnable runnable) {
        plugin.getServer().getScheduler().runTask(plugin, runnable);
    }

    @Override
    public void executeBlockingLater(Runnable runnable, long time, TimeUnit unit) {
        Bukkit.getScheduler().runTaskLater(plugin, runnable, unit.toMillis(time) / 50);
    }

    private Player getPlayer(QueuedPlayer player) {
        if (player.getUuid() != null && (plugin.getServer().getOnlineMode() || plugin.getConfiguration().isBungeeCord())) {
            return plugin.getServer().getPlayer(UuidUtil.mojangUuidToJavaUuid(player.getUuid()));
        }
        return plugin.getServer().getPlayerExact(player.getName());
    }

    @Override
    public boolean isPlayerOnline(QueuedPlayer player) {
        return getPlayer(player) != null;
    }

    @Override
    public int getFreeSlots(QueuedPlayer player) {
        Player player1 = getPlayer(player);
        if (player1 == null)
            return -1;

        int s = 0;

        ItemStack[] contents = player1.getInventory().getContents();
        if (contents.length > MAXIMUM_USABLE_INVENTORY_SIZE) {
            // Spigot 1.9 and above merged regular inventory space with armor space. BuycraftX is only interested in
            // inventory space.
            contents = Arrays.copyOfRange(contents, 0, MAXIMUM_USABLE_INVENTORY_SIZE);
        }

        for (ItemStack stack : contents) {
            if (stack == null)
                s++;
        }
        return s;
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
        return new PlatformInformation(PlatformType.BUKKIT, plugin.getServer().getVersion());
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
