package net.buycraft.plugin.bukkit.gui;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.bukkit.tasks.SendCheckoutLink;
import net.buycraft.plugin.data.Category;
import net.buycraft.plugin.data.Package;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.buycraft.plugin.bukkit.gui.GUIUtil.withName;

@RequiredArgsConstructor
public class CategoryViewGUI {
    private final BuycraftPlugin plugin;
    private final Map<Integer, List<GUIImpl>> categoryMenus = new HashMap<>();

    public class GUIImpl implements Listener {
        private final Inventory inventory;
        private final Integer parentId;
        private Category category;
        private final int page;

        private int calculateSize(Category category, int page) {
            int needed = 45; // bottom row
            if (!category.getSubcategories().isEmpty()) {
                int pagesWithSubcats = (int) Math.ceil(category.getSubcategories().size() / 9);
                if (pagesWithSubcats > page) {
                    // more pages exist
                    needed += 9;
                }
            }

            // if we show subcategories, we can't show as many pages
            return needed;
        }

        private String trimName(String name) {
            if (name.length() <= 32)
                return name;

            return name.substring(0, 29) + "...";
        }

        public GUIImpl(Category category, Integer parentId, int page) {
            this.inventory = Bukkit.createInventory(null, calculateSize(category, page), trimName("Buycraft: " + category.getName()));
            this.parentId = parentId;
            this.page = page;
            update(category);
        }

        public void update(Category category) {
            this.category = category;
            inventory.clear();

            List<List<Category>> subcatPartition = Lists.partition(category.getSubcategories(), 9);
            if (!category.getSubcategories().isEmpty()) {
                if (subcatPartition.size() < page) {
                    List<Category> subcats = subcatPartition.get(page);
                    for (int i = 0; i < subcats.size(); i++) {
                        Category subcat = subcats.get(i);
                        inventory.setItem(i, withName(Material.BOOK, ChatColor.YELLOW + subcat.getName()));
                    }
                }
            }

            List<List<Package>> packagePartition = Lists.partition(category.getPackages(), 36);
            int base = category.getSubcategories().isEmpty() ? 0 : 9;

            if (packagePartition.size() < page) {
                List<Package> packages = packagePartition.get(page);
                for (int i = 0; i < packages.size(); i++) {
                    Package p = packages.get(i);

                    ItemStack stack = new ItemStack(Material.PAPER);
                    ItemMeta meta = stack.getItemMeta();
                    meta.setDisplayName(ChatColor.GREEN + p.getName());

                    List<String> lore = new ArrayList<>();
                    // Price
                    String price = String.valueOf(ChatColor.GRAY) +
                            "Price: " +
                            ChatColor.DARK_GREEN +
                            ChatColor.BOLD +
                            plugin.getServerInformation().getAccount().getCurrency().getPrefix() +
                            p.getPrice().toPlainString() +
                            plugin.getServerInformation().getAccount().getCurrency().getIso4217();
                    lore.add(price);
                    meta.setLore(lore);

                    stack.setItemMeta(meta);
                    inventory.setItem(base + i, stack);
                }
            }

            // Determine if we should draw a previous or next button
            int bottomBase = base + 36;
            if (page > 0) {
                // Definitely draw a previous button
                inventory.setItem(bottomBase + 1, withName(Material.NETHER_STAR, ChatColor.AQUA + "Previous Page"));
            }

            if (subcatPartition.size() > page || packagePartition.size() > page) {
                // Definitely draw a next button
                inventory.setItem(bottomBase + 7, withName(Material.NETHER_STAR, ChatColor.AQUA + "Next Page"));
            }

            // Draw a parent or "view all categories" button
            ItemStack parent = new ItemStack(Material.BOOK_AND_QUILL);
            ItemMeta meta = parent.getItemMeta();
            meta.setDisplayName(ChatColor.GRAY + (parentId == null ? "View All Categories" : "Back to Parent"));
            parent.setItemMeta(meta);
            inventory.setItem(bottomBase + 4, parent);
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!(event.getWhoClicked() instanceof Player)) {
                return;
            }

            final Player player = (Player) event.getWhoClicked();

            if (event.getClickedInventory() == inventory) {
                event.setCancelled(true);

                if (category == null)
                    return;

                ItemStack stack = event.getClickedInventory().getItem(event.getSlot());
                if (stack == null) {
                    return;
                }

                String displayName = stack.getItemMeta().getDisplayName();
                if (displayName.startsWith(ChatColor.YELLOW.toString())) {
                    // Subcategory was clicked
                    for (Category category1 : category.getSubcategories()) {
                        if (category1.getName().equals(ChatColor.stripColor(displayName))) {
                            // TODO: Open category
                            return;
                        }
                    }
                } else if (displayName.startsWith(ChatColor.GREEN.toString())) {
                    // Package was clicked
                    for (Package aPackage : category.getPackages()) {
                        if (aPackage.getName().equals(ChatColor.stripColor(displayName))) {
                            GUIUtil.closeInventoryLater(player);
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, new SendCheckoutLink(plugin, aPackage, player));
                            return;
                        }
                    }
                }

                // TODO: Open category UI
            } else if (event.getView().getTopInventory() == inventory) {
                event.setCancelled(true);
            }
        }
    }
}
