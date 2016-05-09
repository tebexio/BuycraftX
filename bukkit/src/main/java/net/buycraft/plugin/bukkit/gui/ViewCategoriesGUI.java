package net.buycraft.plugin.bukkit.gui;

import com.google.common.collect.ImmutableList;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.data.Category;
import net.buycraft.plugin.data.responses.Listing;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Objects;

import static net.buycraft.plugin.bukkit.gui.GUIUtil.withName;

public class ViewCategoriesGUI implements Listener {
    private final BuycraftPlugin plugin;
    private Inventory inventory;

    public ViewCategoriesGUI(BuycraftPlugin plugin) {
        this.plugin = plugin;
        inventory = Bukkit.createInventory(null, 9, GUIUtil.trimName("Buycraft: " +
                plugin.getI18n().get("categories")));
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    private int roundNine(int s) {
        int sz = s - 1;
        return Math.max(9, sz - (sz % 9) + 9);
    }

    public void update() {
        inventory.clear();

        if (plugin.getApiClient() == null || plugin.getServerInformation() == null) {
            plugin.getLogger().warning("No secret key available (or no server information), so can't update inventories.");
            return;
        }

        Listing listing = plugin.getListingUpdateTask().getListing();
        if (listing == null) {
            plugin.getLogger().warning("No listing found, so can't update inventories.");
            return;
        }

        if (roundNine(listing.getCategories().size()) != inventory.getSize()) {
            Inventory work = Bukkit.createInventory(null, roundNine(listing.getCategories().size()),
                    GUIUtil.trimName("Buycraft: " + plugin.getI18n().get("categories")));
            GUIUtil.replaceInventory(inventory, work);
            inventory = work;
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
        Inventory clickedInventory = GUIUtil.getClickedInventory(event);

        if (clickedInventory != null && Objects.equals(inventory, clickedInventory)) {
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
                player.sendMessage(ChatColor.RED + plugin.getI18n().get("nothing_in_category"));
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
