package net.buycraft.plugin.bukkit.gui;

import com.google.common.base.Supplier;
import net.buycraft.plugin.data.Category;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class CategoryViewGUI {
    private final Map<Integer, Supplier<GUIImpl>> categoryMenus = new HashMap<>();

    public class GUIImpl implements Listener {
        private final Inventory inventory;
        private final Integer parentId;
        private final int page;

        private int calculateSize(Category category, int page) {
            int needed = 9; // bottom row
            boolean showsSubcats = false;
            int pagesWithSubcats = 0;
            if (!category.getSubcategories().isEmpty()) {
                pagesWithSubcats = (int) Math.ceil(category.getSubcategories().size() / 9);
                if (pagesWithSubcats > page) {
                    // more pages exist
                    needed += 9;
                    showsSubcats = true;
                }
            }

            // if we show subcategories, we can't show as many pages
            int packagesOnPage = showsSubcats ? 45 : 36;
            return needed + packagesOnPage;

            // earlier pages might have subcategories, so we need to account for this when finding where we should
            // start
            /*int start = pag
            if (page >= pagesWithSubcats) {

            }*/
        }

        public GUIImpl(Category category, Integer parentId, int page) {
            this.inventory = Bukkit.createInventory(null, calculateSize(category, page), "Buycraft: " + category.getName());
            this.parentId = parentId;
            this.page = page;
            update(category);
        }

        public void update(Category category) {

        }
    }
}
