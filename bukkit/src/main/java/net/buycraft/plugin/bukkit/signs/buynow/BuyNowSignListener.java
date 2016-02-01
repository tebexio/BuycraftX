package net.buycraft.plugin.bukkit.signs.buynow;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BuyNowSignListener {
    @Getter
    private final Set<UUID> settingUpSigns = new HashSet<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory().getTitle() != null && event.getClickedInventory().getTitle().startsWith("Buycraft:") &&
                !event.getClickedInventory().getTitle().equals("Buycraft: Categories")) {

        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (settingUpSigns.remove(event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage(ChatColor.RED + "Buy sign set up cancelled.");
        }
    }
}
