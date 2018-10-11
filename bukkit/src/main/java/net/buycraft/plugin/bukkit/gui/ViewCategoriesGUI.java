package net.buycraft.plugin.bukkit.gui;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.bukkit.util.GUIUtil;
import net.buycraft.plugin.data.Category;
import net.buycraft.plugin.data.responses.Listing;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.util.Objects;

import static net.buycraft.plugin.bukkit.util.GUIUtil.withName;

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
        if (inventoryNeedsReloading()) {
            Bukkit.getLogger().info("Inventory appears to be empty, trying to read from gui.cache file...");
            try {
                BufferedReader reader = new BufferedReader(new FileReader(plugin.getDataFolder() + "/gui.cache"));
                String jsonString = reader.readLine();
                Listing listing = new Gson().fromJson(jsonString, Listing.class);
                if (listing != null)
                    listing.order();

                inventory = Bukkit.createInventory(null, 9, GUIUtil.trimName("Buycraft: " +
                        plugin.getI18n().get("categories")));

                this.createInventoryFromListing(listing);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        player.openInventory(inventory);


    }

    private boolean inventoryNeedsReloading(){
        if(this.inventory == null){
            return true;
        }

        for(ItemStack is : this.inventory.getContents())
        {
            if(is != null) return false;
        }
        return true;
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

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(plugin.getDataFolder() + "/gui.cache"));
            bw.write(new Gson().toJson(listing));
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.createInventoryFromListing(listing);

    }

    private void createInventoryFromListing(Listing listing){
        if (roundNine(listing.getCategories().size()) != inventory.getSize()) {
            Inventory work = Bukkit.createInventory(null, roundNine(listing.getCategories().size()),
                    GUIUtil.trimName("Buycraft: " + plugin.getI18n().get("categories")));
            GUIUtil.replaceInventory(inventory, work);
            inventory = work;
        }

        for (Category category : listing.getCategories()) {
            String gui_item = category.getGui_item();

            Material material = Material.matchMaterial("CHEST");
            short variant = 0;

            if (gui_item != null && !gui_item.equals("")) {
                if(gui_item.matches("^\\d+$")){
                    material = Material.getMaterial(gui_item);
                }else if(!gui_item.contains(":")){
                    material = Material.matchMaterial(gui_item);
                }else {
                    String[] parts = gui_item.split(":");
                    if(parts[0].matches("^\\d+$")){
                        material = Material.getMaterial(parts[0]);
                    }else{
                        material = Material.matchMaterial(parts[0]);
                    }
                    variant = Short.valueOf(parts[1]);
                }
            }

            if(material == null){
                material = Material.matchMaterial("CHEST");
            }

            inventory.setItem(inventory.firstEmpty(), withName(material, ChatColor.YELLOW + category.getName(), variant));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = GUIUtil.getClickedInventory(event);

        if (clickedInventory != null && Objects.equals(inventory, clickedInventory)) {
            event.setCancelled(true);

            if (!(event.getWhoClicked() instanceof Player)) {
                return;
            }

            final Player player = (Player) event.getWhoClicked();

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

            plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
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
