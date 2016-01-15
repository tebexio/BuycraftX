package net.buycraft.plugin.bukkit.gui;

import lombok.Value;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Value
public class GUIItem {
    private final String name;
    private final ItemStack look;
    private final Action action;

    public ItemStack getFinalStack() {
        ItemStack c = look.clone();
        ItemMeta meta = c.getItemMeta();
        meta.setDisplayName(name);
        c.setItemMeta(meta);
        return c;
    }
}
