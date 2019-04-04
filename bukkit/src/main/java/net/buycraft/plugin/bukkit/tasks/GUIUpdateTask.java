package net.buycraft.plugin.bukkit.tasks;

import net.buycraft.plugin.bukkit.BuycraftPlugin;

public class GUIUpdateTask implements Runnable {
    private final BuycraftPlugin plugin;

    public GUIUpdateTask(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getViewCategoriesGUI().update();
        plugin.getCategoryViewGUI().update();
    }
}
