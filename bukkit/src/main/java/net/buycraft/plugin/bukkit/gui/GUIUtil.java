package net.buycraft.plugin.bukkit.gui;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GUIUtil {
    private static BuycraftPlugin plugin;

    public static void setPlugin(BuycraftPlugin plugin) {
        Objects.requireNonNull(GUIUtil.plugin, "Plugin already set");
        GUIUtil.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public static void closeInventoryLater(final Player player) {
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                player.closeInventory();
            }
        });
    }
}
