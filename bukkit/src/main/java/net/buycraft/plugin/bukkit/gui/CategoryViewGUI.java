package net.buycraft.plugin.bukkit.gui;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.bukkit.tasks.SendCheckoutLink;
import net.buycraft.plugin.bukkit.util.GUIUtil;
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

import java.text.NumberFormat;
import java.util.*;

import static net.buycraft.plugin.bukkit.util.GUIUtil.withName;

@RequiredArgsConstructor
public class CategoryViewGUI {
    private final BuycraftPlugin plugin;
    private final Map<Integer, List<GUIImpl>> categoryMenus = new HashMap<>();

    private static int calculatePages(Category category) {
        int pagesWithSubcats = (int) Math.ceil(category.getSubcategories().size() / 9D);
        int pagesWithPackages = (int) Math.ceil(category.getPackages().size() / 36D);
        return Math.max(pagesWithSubcats, pagesWithPackages);
    }

    public GUIImpl getFirstPage(Category category) {
        List<GUIImpl> guis = categoryMenus.get(category.getId());
        if (guis == null)
            return null;

        return Iterables.getFirst(guis, null);
    }

    public void update() {
        if (plugin.getApiClient() == null || plugin.getServerInformation() == null) {
            plugin.getLogger().warning("No secret key available (or no server information), so can't update inventories.");
            return;
        }

        Listing listing = plugin.getListingUpdateTask().getListing();
        if (listing == null) {
            plugin.getLogger().warning("No listing found, so can't update inventories.");
            return;
        }

        List<Integer> foundIds = new ArrayList<>();
        for (Category category : listing.getCategories()) {
            foundIds.add(category.getId());
            for (Category category1 : category.getSubcategories()) {
                foundIds.add(category1.getId());
            }
        }

        for (Iterator<Map.Entry<Integer, List<GUIImpl>>> it = categoryMenus.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, List<GUIImpl>> next = it.next();
            if (!foundIds.contains(next.getKey())) {
                for (GUIImpl gui : next.getValue()) {
                    gui.destroy();
                }
                it.remove();
            }
        }

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
                plugin.getServer().getPluginManager().registerEvents(gui, plugin);
                pages.add(gui);
            }
        } else {
            int allPages = calculatePages(category);
            int toRemove = pages.size() - allPages;
            if (toRemove > 0) {
                List<GUIImpl> prune = pages.subList(pages.size() - toRemove, pages.size());
                for (GUIImpl gui : prune) {
                    gui.destroy();
                }
                prune.clear();
            } else if (toRemove < 0) {
                int toAdd = -toRemove;
                for (int i = 0; i < toAdd; i++) {
                    GUIImpl gui = new GUIImpl(parent != null ? parent.getId() : null, pages.size(), category);
                    plugin.getServer().getPluginManager().registerEvents(gui, plugin);
                    pages.add(gui);
                }
            }

            for (int i = 0; i < pages.size(); i++) {
                GUIImpl gui = pages.get(i);
                if (gui.requiresResize(category)) {
                    HandlerList.unregisterAll(gui);
                    GUIImpl tmpGui = new GUIImpl(parent != null ? parent.getId() : null, i, category);
                    plugin.getServer().getPluginManager().registerEvents(tmpGui, plugin);
                    pages.set(i, tmpGui);

                    GUIUtil.replaceInventory(gui.inventory, tmpGui.inventory);
                } else {
                    gui.update(category);
                }
            }
        }

        for (Category category1 : category.getSubcategories()) {
            doUpdate(category, category1);
        }
    }

    public class GUIImpl implements Listener {
        private final Inventory inventory;
        private final Integer parentId;
        private final int page;
        private Category category;

        public GUIImpl(Integer parentId, int page, Category category) {
            this.inventory = Bukkit.createInventory(null, calculateSize(category, page), GUIUtil.trimName("Buycraft: " + category.getName()));
            this.parentId = parentId;
            this.page = page;
            update(category);
        }

        private int calculateSize(Category category, int page) {
            // TODO: Calculate this amount based on no of packages
            int needed = 45; // bottom row
            if (!category.getSubcategories().isEmpty()) {
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

        public void destroy() {
            HandlerList.unregisterAll(this);
            closeAll();
        }

        public void closeAll() {
            for (HumanEntity entity : ImmutableList.copyOf(inventory.getViewers())) {
                entity.closeInventory();
            }
        }

        public void open(Player player) {
            player.openInventory(inventory);
        }

        public void update(Category category) {
            this.category = category;
            inventory.clear();

            List<List<Category>> subcatPartition;
            if (!category.getSubcategories().isEmpty()) {
                subcatPartition = Lists.partition(category.getSubcategories(), 9);
                if (subcatPartition.size() - 1 >= page) {
                    List<Category> subcats = subcatPartition.get(page);
                    for (int i = 0; i < subcats.size(); i++) {
                        Category subcat = subcats.get(i);
                        
                        String gui_item = subcats.get(i).getGui_item();

                        Material material = Material.matchMaterial("BOOK");
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
                            material = Material.matchMaterial("BOOK");
                        }

                        inventory.setItem(i, withName(material, ChatColor.YELLOW + subcat.getName(), variant));
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

                    String gui_item = p.getGui_item();

                    Material material = Material.matchMaterial("PAPER");
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
                        material = Material.matchMaterial("PAPER");
                    }

                    ItemStack stack = new ItemStack(material, 1, variant);
                    ItemMeta meta = stack.getItemMeta();
                    meta.setDisplayName(ChatColor.GREEN + p.getName());

                    List<String> lore = new ArrayList<>();
                    // Price
                    NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
                    format.setCurrency(Currency.getInstance(plugin.getServerInformation().getAccount().getCurrency().getIso4217()));

                    String price = ChatColor.GRAY +
                            plugin.getI18n().get("price") +
                            ": " +
                            ChatColor.DARK_GREEN +
                            ChatColor.BOLD +
                            format.format(p.getEffectivePrice());
                    lore.add(price);

                    if (p.getSale() != null && p.getSale().isActive()) {
                        lore.add(ChatColor.RED + plugin.getI18n().get("amount_off", format.format(p.getSale().getDiscount())));
                    }

                    meta.setLore(lore);

                    stack.setItemMeta(meta);
                    inventory.setItem(base + i, stack);
                }
            }

            // Determine if we should draw a previous or next button
            int bottomBase = base + 36;
            if (page > 0) {
                // Definitely draw a previous button
                inventory.setItem(bottomBase + 1, withName(Material.NETHER_STAR, ChatColor.AQUA + plugin.getI18n().get("previous_page")));
            }

            if (subcatPartition.size() - 1 > page || packagePartition.size() - 1 > page) {
                // Definitely draw a next button
                inventory.setItem(bottomBase + 7, withName(Material.NETHER_STAR, ChatColor.AQUA + plugin.getI18n().get("next_page")));
            }

            // Draw a parent or "view all categories" button
            ItemStack parent = new ItemStack(Material.LEGACY_BOOK_AND_QUILL);
            ItemMeta meta = parent.getItemMeta();
            meta.setDisplayName(ChatColor.GRAY + (parentId == null ? plugin.getI18n().get("view_all_categories") : plugin.getI18n().get("back_to_parent")));
            parent.setItemMeta(meta);
            inventory.setItem(bottomBase + 4, parent);
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

                if (category == null)
                    return;

                ItemStack stack = clickedInventory.getItem(event.getSlot());
                if (stack == null) {
                    return;
                }

                String displayName = stack.getItemMeta().getDisplayName();
                if (displayName.startsWith(ChatColor.YELLOW.toString())) {
                    // Subcategory was clicked
                    for (final Category category1 : category.getSubcategories()) {
                        if (category1.getName().equals(ChatColor.stripColor(displayName))) {
                            final GUIImpl gui = getFirstPage(category1);
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
                            return;
                        }
                    }
                } else if (displayName.startsWith(ChatColor.GREEN.toString())) {
                    // Package was clicked
                    for (Package aPackage : category.getPackages()) {
                        if (aPackage.getName().equals(ChatColor.stripColor(displayName))) {
                            GUIUtil.closeInventoryLater(player);
                            if (plugin.getBuyNowSignListener().getSettingUpSigns().containsKey(player.getUniqueId())) {
                                plugin.getBuyNowSignListener().doSignSetup(player, aPackage);
                            } else {
                                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new SendCheckoutLink(plugin, aPackage.getId(), player));
                            }
                            return;
                        }
                    }
                } else if (displayName.equals(ChatColor.AQUA + plugin.getI18n().get("previous_page"))) {
                    plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            categoryMenus.get(category.getId()).get(page - 1).open(player);
                        }
                    });
                } else if (displayName.equals(ChatColor.AQUA + plugin.getI18n().get("next_page"))) {
                    plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            categoryMenus.get(category.getId()).get(page + 1).open(player);
                        }
                    });
                } else if (stack.getType() == Material.LEGACY_BOOK_AND_QUILL) {
                    if (parentId != null) {
                        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                categoryMenus.get(parentId).get(0).open(player);
                            }
                        });
                    } else {
                        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
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
