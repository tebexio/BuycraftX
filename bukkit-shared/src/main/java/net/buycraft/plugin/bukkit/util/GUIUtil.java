package net.buycraft.plugin.bukkit.util;

import com.google.common.collect.ImmutableList;
import net.buycraft.plugin.bukkit.BuycraftPluginBase;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public class GUIUtil {
    private static BuycraftPluginBase plugin;

    private GUIUtil() {
    }

    public static void setPlugin(BuycraftPluginBase plugin) {
        if (GUIUtil.plugin != null) {
            throw new IllegalStateException("Plugin already set");
        }
        GUIUtil.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public static void closeInventoryLater(final Player player) {
        plugin.getServer().getScheduler().runTask(plugin, player::closeInventory);
    }

    public static void replaceInventory(Inventory oldInv, Inventory newInv) {
        // getViewers() is updated as we remove players, so we need to make a copy
        for (HumanEntity entity : ImmutableList.copyOf(oldInv.getViewers())) {
            entity.openInventory(newInv);
        }
    }

    public static Inventory getClickedInventory(InventoryClickEvent event) {
        if (event.getSlot() < 0) return null;
        InventoryView view = event.getView();

        if (view.getTopInventory() != null && event.getSlot() < view.getTopInventory().getSize()) {
            return view.getTopInventory();
        } else {
            return view.getBottomInventory();
        }
    }

    public static ItemStack withName(ItemStack stack, String name) {
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        stack.setItemMeta(meta);
        return stack;
    }

    public static String trimName(String name) {
        if (name.length() <= 32) return name;
        return name.substring(0, 29) + "...";
    }
}
