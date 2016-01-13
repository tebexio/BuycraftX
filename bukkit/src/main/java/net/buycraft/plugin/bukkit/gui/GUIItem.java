package net.buycraft.plugin.bukkit.gui;

import lombok.Value;
import org.bukkit.inventory.ItemStack;

@Value
public class GUIItem {
    private final String name;
    private final int id;
    private final ItemStack look;
    private final Action action;
}
