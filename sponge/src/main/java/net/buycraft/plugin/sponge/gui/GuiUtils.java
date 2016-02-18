package net.buycraft.plugin.sponge.gui;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.custom.CustomInventory;
import org.spongepowered.api.item.inventory.type.InventoryRow;

/**
 * Created by meyerzinn on 2/17/16.
 */
public class GuiUtils {

    private static GuiUtils ourInstance = new GuiUtils();

    public static GuiUtils getInstance() {
        return ourInstance;
    }

    private GuiUtils() {
    }

}
