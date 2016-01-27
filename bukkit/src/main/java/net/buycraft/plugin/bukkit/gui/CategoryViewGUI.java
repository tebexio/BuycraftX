package net.buycraft.plugin.bukkit.gui;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.bukkit.tasks.SendCheckoutLink;
import net.buycraft.plugin.data.Category;
import net.buycraft.plugin.data.Package;
import net.buycraft.plugin.data.responses.Listing;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
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

    public GUIImpl getFirstPage(Category category) {
        if (!categoryMenus.containsKey(category.getId()))
            return null;

        return Iterables.getFirst(categoryMenus.get(category.getId()), null);
    }

    public void update() {
        Listing listing = plugin.getListingUpdateTask().getListing();
        if (listing == null) {
            plugin.getLogger().warning("No listing found, so can't update categories.");
            return;
        }

        List<Integer> foundIds = new ArrayList<>();
        for (Category category : listing.getCategories()) {
            foundIds.add(category.getId());
            if (category.getSubcategories() != null) {
                for (Category category1 : category.getSubcategories()) {
                    foundIds.add(category1.getId());
                }
            }
        }

        Map<Integer, List<GUIImpl>> prune = new HashMap<>(categoryMenus);
        prune.keySet().removeAll(foundIds);
        for (List<GUIImpl> guis : prune.values()) {
            for (GUIImpl gui : guis) {
                gui.closeAll();
                HandlerList.unregisterAll(gui);
            }
        }
        categoryMenus.keySet().retainAll(foundIds);

        for (Category category : listing.getCategories()) {
            doUpdate(null, category);
        }
    }

    private void doUpdate(Category parent, Category category) {
        List<GUIImpl> pages = categoryMenus.get(category.getId());
        if (pages == null) {
            pages = new ArrayList<>();
            categoryMenus.put(category.getId(), pages);
            for (int i = 0; i < calculatePages(category); i++) {
                GUIImpl gui = new GUIImpl(parent != null ? parent.getId() : null, i, category);
                Bukkit.getPluginManager().registerEvents(gui, plugin);
                pages.add(gui);
            }
        } else {
            int allPages = calculatePages(category);
            int toRemove = pages.size() - allPages;
            if (toRemove > 0) {
                List<GUIImpl> prune = pages.subList(pages.size() - toRemove, pages.size());
                for (GUIImpl gui : prune) {
                    gui.closeAll();
                    HandlerList.unregisterAll(gui);
                }
                prune.clear();
            } else if (toRemove < 0) {
                int toAdd = -toRemove;
                for (int i = 0; i < toAdd; i++) {
                    GUIImpl gui = new GUIImpl(parent != null ? parent.getId() : null, pages.size(), category);
                    Bukkit.getPluginManager().registerEvents(gui, plugin);
                    pages.add(gui);
                }
            }

            for (int i = 0; i < pages.size(); i++) {
                GUIImpl gui = pages.get(i);
                if (gui.requiresResize(category)) {
                    HandlerList.unregisterAll(gui);
                    GUIImpl tmpGui = new GUIImpl(parent != null ? parent.getId() : null, i, category);
                    Bukkit.getPluginManager().registerEvents(tmpGui, plugin);
                    pages.set(i, tmpGui);

                    for (HumanEntity entity : ImmutableList.copyOf(gui.inventory.getViewers())) {
                        entity.openInventory(tmpGui.inventory);
                    }
                } else {
                    gui.update(category);
                }
            }
        }

        if (category.getSubcategories() != null) {
            for (Category category1 : category.getSubcategories()) {
                doUpdate(category, category1);
            }
        }
    }

    private static int calculatePages(Category category) {
        int pagesWithSubcats = category.getSubcategories() == null ? 0 :
                (int) Math.ceil(category.getSubcategories().size() / 9D);
        int pagesWithPackages = (int) Math.ceil(category.getPackages().size() / 36D);
        return Math.max(pagesWithSubcats, pagesWithPackages);
    }

    public class GUIImpl implements Listener {
        private final Inventory inventory;
        private final Integer parentId;
        private Category category;
        private final int page;

        private int calculateSize(Category category, int page) {
            // TODO: Calculate this amount based on no of packages
            int needed = 45; // bottom row
            if (category.getSubcategories() != null && !category.getSubcategories().isEmpty()) {
                int pagesWithSubcats = (int) Math.ceil(category.getSubcategories().size() / 9D);
                if (pagesWithSubcats >= page) {
                    // more pages exist
                    needed += 9;
                }
            }

            // if we show subcategories, we can't show as many pages
            return needed;
        }

        public boolean requiresResize(Category category) {
            return calculateSize(category, page) != inventory.getSize();
        }

        private String trimName(String name) {
            if (name.length() <= 32)
                return name;

            return name.substring(0, 29) + "...";
        }

        public void closeAll() {
            for (HumanEntity entity : ImmutableList.copyOf(inventory.getViewers())) {
                entity.closeInventory();
            }
        }

        public void open(Player player) {
            player.openInventory(inventory);
        }

        public GUIImpl(Integer parentId, int page, Category category) {
            this.inventory = Bukkit.createInventory(null, calculateSize(category, page), trimName("Buycraft: " + category.getName()));
            this.parentId = parentId;
            this.page = page;
            update(category);
        }

        public void update(Category category) {
            this.category = category;
            inventory.clear();

            List<List<Category>> subcatPartition;
            if (category.getSubcategories() != null && !category.getSubcategories().isEmpty()) {
                subcatPartition = Lists.partition(category.getSubcategories(), 9);
                if (subcatPartition.size() - 1 >= page) {
                    List<Category> subcats = subcatPartition.get(page);
                    for (int i = 0; i < subcats.size(); i++) {
                        Category subcat = subcats.get(i);
                        inventory.setItem(i, withName(Material.BOOK, ChatColor.YELLOW + subcat.getName()));
                    }
                }
            } else {
                subcatPartition = ImmutableList.of();
            }

            List<List<Package>> packagePartition = Lists.partition(category.getPackages(), 36);
            int base = subcatPartition.isEmpty() ? 0 : 9;

            if (packagePartition.size() - 1 >= page) {
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
                            plugin.getServerInformation().getAccount().getCurrency().getSymbol() +
                            p.getEffectivePrice().toPlainString() + " " +
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

            if (subcatPartition.size() - 1 > page || packagePartition.size() - 1 > page) {
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

            if (event.getClickedInventory().equals(inventory)) {
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
                    for (final Category category1 : category.getSubcategories()) {
                        if (category1.getName().equals(ChatColor.stripColor(displayName))) {
                            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    categoryMenus.get(category1.getId()).get(0).open(player);
                                }
                            });
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
                } else if (displayName.equals(ChatColor.AQUA + "Previous Page")) {
                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            categoryMenus.get(category.getId()).get(page - 1).open(player);
                        }
                    });
                } else if (displayName.equals(ChatColor.AQUA + "Next Page")) {
                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            categoryMenus.get(category.getId()).get(page + 1).open(player);
                        }
                    });
                } else if (stack.getType() == Material.BOOK_AND_QUILL) {
                    if (parentId != null) {
                        Bukkit.getScheduler().runTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                categoryMenus.get(parentId).get(0).open(player);
                            }
                        });
                    } else {
                        Bukkit.getScheduler().runTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                plugin.getViewCategoriesGUI().open(player);
                            }
                        });
                    }
                }
            } else if (event.getView().getTopInventory() == inventory) {
                event.setCancelled(true);
            }
        }
    }
}
