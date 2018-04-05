package net.buycraft.plugin.bukkit.util;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GUIUtil {
    private static BuycraftPlugin plugin;

    public static void setPlugin(BuycraftPlugin plugin) {
        if (GUIUtil.plugin != null) {
            throw new IllegalStateException("Plugin already set");
        }
        GUIUtil.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public static void closeInventoryLater(final Player player) {
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                player.closeInventory();
            }
        });
    }

    public static void replaceInventory(Inventory oldInv, Inventory newInv) {
        // getViewers() is updated as we remove players, so we need to make a copy
        for (HumanEntity entity : ImmutableList.copyOf(oldInv.getViewers())) {
            entity.openInventory(newInv);
        }
    }

    public static Inventory getClickedInventory(InventoryClickEvent event) {
        if (event.getSlot() < 0)
            return null;

        InventoryView view = event.getView();

        if (view.getTopInventory() != null && event.getSlot() < view.getTopInventory().getSize()) {
            return view.getTopInventory();
        } else {
            return view.getBottomInventory();
        }
    }

    public static ItemStack withName(Material material, String name) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        stack.setItemMeta(meta);
        return stack;
    }

    public static ItemStack withName(Material material, String name, short variant) {
        ItemStack stack = new ItemStack(material, 1, variant);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        stack.setItemMeta(meta);
        return stack;
    }

    public static String trimName(String name) {
        if (name.length() <= 32)
            return name;

        return name.substring(0, 29) + "...";
    }
}
