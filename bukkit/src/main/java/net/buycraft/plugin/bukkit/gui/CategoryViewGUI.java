package net.buycraft.plugin.bukkit.gui;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.data.Category;
import net.buycraft.plugin.data.Package;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CategoryViewGUI {
    private final BuycraftPlugin plugin;
    private final Map<Integer, Supplier<GUIImpl>> categoryMenus = new HashMap<>();

    public class GUIImpl implements Listener {
        private final Inventory inventory;
        private final Integer parentId;
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
            this.inventory = Bukkit.createInventory(null, calculateSize(category, page), "Buycraft: " + trimName(category.getName()));
            this.parentId = parentId;
            this.page = page;
            update(category);
        }

        public void update(Category category) {
            inventory.clear();

            List<List<Category>> subcatPartition = Lists.partition(category.getSubcategories(), 9);
            if (!category.getSubcategories().isEmpty()) {
                if (subcatPartition.size() < page) {
                    List<Category> subcats = subcatPartition.get(page);
                    for (int i = 0; i < subcats.size(); i++) {
                        Category subcat = subcats.get(i);

                        ItemStack stack = new ItemStack(Material.BOOK);
                        ItemMeta meta = stack.getItemMeta();
                        meta.setDisplayName(ChatColor.YELLOW + subcat.getName());
                        stack.setItemMeta(meta);
                        inventory.setItem(i, stack);
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
                ItemStack stack = new ItemStack(Material.NETHER_STAR);
                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(ChatColor.AQUA + "Previous Page");
                stack.setItemMeta(meta);
                inventory.setItem(bottomBase + 1, stack);
            }

            if (subcatPartition.size() > page || packagePartition.size() > page) {
                // Definitely draw a next button
                ItemStack stack = new ItemStack(Material.NETHER_STAR);
                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(ChatColor.AQUA + "Next Page");
                stack.setItemMeta(meta);
                inventory.setItem(bottomBase + 7, stack);
            }

            // Draw a parent or "view all categories" button
            ItemStack parent = new ItemStack(Material.BOOK_AND_QUILL);
            ItemMeta meta = parent.getItemMeta();
            meta.setDisplayName(ChatColor.GRAY + (parentId == null ? "View All Categories" : "Back to Parent"));
            parent.setItemMeta(meta);
            inventory.setItem(bottomBase + 4, parent);
        }
    }
}
