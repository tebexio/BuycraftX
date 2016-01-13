package net.buycraft.plugin.bukkit.gui;

import com.google.common.base.Preconditions;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class BaseGUI implements Listener {
    private final Map<Integer, GUIItem> itemMap = new HashMap<>();

    public void setItem(int id, GUIItem item) {
        Preconditions.checkNotNull(item, "item");
    }
}
