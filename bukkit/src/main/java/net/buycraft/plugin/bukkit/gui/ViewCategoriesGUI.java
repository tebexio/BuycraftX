package net.buycraft.plugin.bukkit.gui;

import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.data.Category;
import net.buycraft.plugin.data.responses.Listing;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ViewCategoriesGUI implements Listener {
    private final BuycraftPlugin plugin;
    private final Inventory inventory;

    public ViewCategoriesGUI(BuycraftPlugin plugin) {
        this.plugin = plugin;
        // TODO: How are we going to handle over-sized categories?
        inventory = Bukkit.createInventory(null, 9, "Buycraft: Categories");
    }

    public void update() {
        inventory.clear();

        Listing listing = plugin.getListingUpdateTask().getListing();
        if (listing == null) {
            plugin.getLogger().warning("No listing found, so can't update categories.");
            return;
        }

        for (Category category : listing.getCategories()) {
            ItemStack stack = new ItemStack(Material.BOOK);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + category.getName());
            stack.setItemMeta(meta);
            inventory.addItem(stack);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (event.getClickedInventory() == inventory) {
            event.setCancelled(true);

            Listing listing = plugin.getListingUpdateTask().getListing();
            if (listing == null) {
                return;
            }

            if (event.getSlot() >= listing.getCategories().size()) {
                return;
            }

            Category category = listing.getCategories().get(event.getSlot());
            if (category == null) {
                return;
            }

            // TODO: Open category UI
        } else if (event.getView().getTopInventory() == inventory) {
            event.setCancelled(true);
        }
    }
}
