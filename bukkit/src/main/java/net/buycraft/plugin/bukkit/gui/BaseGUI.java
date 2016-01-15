package net.buycraft.plugin.bukkit.gui;

import com.google.common.base.Preconditions;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BaseGUI implements Listener {
    @NonNull
    private final String title;
    private final Map<Integer, GUIItem> itemMap = new HashMap<>();
    private final Inventory inventory;
    private final int size;

    public BaseGUI(String title, int maxSize) {
        Preconditions.checkNotNull(title, "title");
        Preconditions.checkArgument(maxSize > 0 && maxSize % 9 == 0, "size invalid");
        this.title = title;
        this.size = maxSize;
        this.inventory = Bukkit.createInventory(null, size, title);
    }

    public void setItem(int slot, GUIItem item) {
        Preconditions.checkNotNull(item, "item");
        Preconditions.checkArgument(slot >= 0 && slot < 54, "slot is not valid, must be from 0 to 54 exclusive");
        itemMap.put(slot, item);
        inventory.setItem(slot, item.getFinalStack());
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        if (event.getClickedInventory().equals(inventory)) {
            GUIItem item = itemMap.get(event.getRawSlot());
            event.setCancelled(true);
            item.getAction().onClick((Player) event.getWhoClicked(), event.getClick());
        } else if (event.getView().getTopInventory() == inventory) {
            event.setCancelled(true);
        }
    }
}
