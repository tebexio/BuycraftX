package net.buycraft.plugin.bukkit.gui;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GUIUtil {
    private static BuycraftPlugin plugin;

    public static void setPlugin(BuycraftPlugin plugin) {
        if (GUIUtil.plugin != null)
            throw new IllegalStateException("Plugin already set");
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

    public static ItemStack withName(Material material, String name) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        stack.setItemMeta(meta);
        return stack;
    }
}
