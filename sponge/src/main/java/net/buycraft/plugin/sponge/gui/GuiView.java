package net.buycraft.plugin.sponge.gui;

import net.buycraft.plugin.data.Category;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by meyerzinn on 2/15/16.
 */
public class GuiView {

    /*
    * This class makes several assumptions about the way the system works:
    * 1. A view is just a collection of subcategories/packages ordered by their given weight.
    * 2. A view can be of variable size (multiples of 9) but not exceeding 45.
    * 3. The final row (6) is reserved for pagination tools if relevant and an information item.
    * 4. Packages and subcategories are on a "tree", beginning with the root category.
    * In order for assumption 4 to be fully correct, a field, "parent", must be added to the Package and
     */

    private BuycraftPlugin plugin;
    private int currentCategoryId;
    private int parentCategoryId;
    private Player viewer;
    private Inventory inventory;
    private Map<Slot, GuiIcon> iconHandlers = new HashMap<>();

    public GuiView(BuycraftPlugin plugin, int categoryId, Player viewer) {
        this.plugin = plugin;
        this.currentCategoryId = categoryId;
        this.viewer = viewer;
        for (Category c : plugin.getListingUpdateTask().getListing().getCategories()) {

        }
    }

    public void update() {

    }

    private int getPreFooterInventorySize(int max) {
        if (max <= 0) {
            return 9;
        }
        int quotient = (int) Math.ceil(max / 9.0);
        return quotient > 5 ? 45 : quotient * 9;
    }

    private boolean needsResize() {
        Category category = plugin.getListingUpdateTask().findCategory(this.currentCategoryId);
        return ((category.getSubcategories() != null && !category.getSubcategories().isEmpty()) ? category.getSubcategories().size() : 0) + category
                .getPackages().size() ==
                inventory
                        .size();
    }

    @Listener
    public void onChangeInventoryEvent(ChangeInventoryEvent event) {
        if (event.getTargetInventory().parent().equals(inventory)) {
            event.setCancelled(true);
            event.getTransactions().get(0).getSlot();
            if (event.getCause().first(Player.class).isPresent()) {

            }
        }
    }

}
