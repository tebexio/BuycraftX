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

import static net.buycraft.plugin.bukkit.gui.GUIUtil.withName;

public class ViewCategoriesGUI implements Listener {
    private final BuycraftPlugin plugin;
    private final Inventory inventory;

    public ViewCategoriesGUI(BuycraftPlugin plugin) {
        this.plugin = plugin;
        // TODO: How are we going to handle over-sized categories?
        inventory = Bukkit.createInventory(null, 9, "Buycraft: Categories");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public void update() {
        inventory.clear();

        Listing listing = plugin.getListingUpdateTask().getListing();
        if (listing == null) {
            plugin.getLogger().warning("No listing found, so can't update categories.");
            return;
        }

        for (Category category : listing.getCategories()) {
            inventory.addItem(withName(Material.BOOK, ChatColor.YELLOW + category.getName()));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getWhoClicked();

        if (event.getClickedInventory().equals(inventory)) {
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

            final CategoryViewGUI.GUIImpl gui = plugin.getCategoryViewGUI().getFirstPage(category);
            if (gui == null) {
                return;
            }

            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    gui.open(player);
                }
            });
        } else if (event.getView().getTopInventory() == inventory) {
            event.setCancelled(true);
        }
    }
}
